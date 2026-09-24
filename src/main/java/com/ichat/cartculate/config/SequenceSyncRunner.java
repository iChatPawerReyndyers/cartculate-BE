package com.ichat.cartculate.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

/**
 * Re-syncs the auto-increment (IDENTITY) sequences of the tables that
 * data.sql seeds with EXPLICIT ids.
 *
 * WHY THIS EXISTS: data.sql inserts rows such as items (12..122), stores
 * (1..5) and users (1) with hand-picked ids. Postgres does not advance an
 * IDENTITY sequence when an id is supplied manually, so the sequence is
 * still sitting at 1. The first few POST /api/items calls happen to work
 * (ids 1..11 are free), but the next insert is handed id 12, which already
 * exists -> "duplicate key value violates unique constraint items_pkey"
 * -> HTTP 500 -> the app's "Could not add product" error.
 *
 * Runs once on every startup, AFTER Hibernate has created the schema and
 * data.sql has run. It only ever moves a sequence forward to MAX(id) + 1,
 * so it is safe to run repeatedly and never touches any row data.
 */
/** @Order(1): must run BEFORE any runner that inserts rows (e.g. PasigMarketSeedRunner), so the id counters are already correct. */
@Component
@Order(1)
public class SequenceSyncRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SequenceSyncRunner.class);

    /** Tables seeded by data.sql with explicit ids. All use an "id" IDENTITY column. */
    private static final List<String> TABLES = List.of("users", "stores", "items");

    private final DataSource dataSource;

    public SequenceSyncRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection connection = dataSource.getConnection()) {
            String product = connection.getMetaData().getDatabaseProductName();
            if (product == null || !product.toLowerCase().contains("postgres")) {
                log.info("SequenceSyncRunner skipped: database is {}, not PostgreSQL.", product);
                return;
            }
            for (String table : TABLES) {
                // One table failing must never stop the app from booting.
                try (Statement statement = connection.createStatement()) {
                    statement.execute(
                            "SELECT setval(pg_get_serial_sequence('" + table + "', 'id'), "
                                    + "COALESCE((SELECT MAX(id) FROM " + table + "), 0) + 1, false)"
                    );
                    log.info("SequenceSyncRunner: synced id sequence for '{}'.", table);
                } catch (Exception e) {
                    log.warn("SequenceSyncRunner: could not sync id sequence for '{}': {}", table, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("SequenceSyncRunner: could not open a database connection: {}", e.getMessage());
        }
    }
}
