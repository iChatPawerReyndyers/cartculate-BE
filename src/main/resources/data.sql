-- data.sql
-- Safe, idempotent mock data seed for a NON-PRODUCTION database.
-- Guard: the entire script only runs if `items` is completely empty, so
-- re-running this on app restart (or Spring re-executing data.sql every
-- boot) never duplicates rows or clobbers real data.
--
-- Setup required in application.properties for Spring Boot to auto-run this:
--   spring.sql.init.mode=always
--   spring.jpa.defer-datasource-initialization=true
--   spring.sql.init.separator=@@
-- (defer-datasource-initialization ensures Hibernate creates the tables via
-- ddl-auto=update BEFORE this script's INSERTs run against them. The custom
-- separator is required because this script is one big DO $$ ... $$ block
-- with internal semicolons that Spring's default ";" splitter would
-- otherwise cut through mid-statement - see the "@@" marker at the very
-- end of this file, which is what actually terminates the statement now.)

DO $seed$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM items LIMIT 1) THEN

        -- ── Users ────────────────────────────────────────────────────────
        INSERT INTO users (id, name, email) VALUES
            (1, 'Juan Dela Cruz', 'juan@example.com')
        ON CONFLICT (id) DO NOTHING;

        -- ── Stores ───────────────────────────────────────────────────────
INSERT INTO stores (id, name) VALUES
                                  (1, 'Puregold'),
                                  (2, 'S&R')
    ON CONFLICT (id) DO NOTHING;

-- ── Items (master list) ─────────────────────────────────────────
INSERT INTO items (id, name, category) VALUES
                                           (1, 'Carrots', 'Vegetables'),
                                           (2, 'Bell pepper', 'Vegetables'),
                                           (3, 'Potato', 'Vegetables'),
                                           (4, 'Beef cubes', 'Meat'),
                                           (5, 'Ground pork', 'Meat'),
                                           (6, 'Napkin', 'Toiletries'),
                                           (7, 'Toothpaste', 'Toiletries'),
                                           (8, 'Coca-Cola Light 1.5L', 'Beverages'),
                                           (9, 'Pampers Baby Wipes', 'Toiletries'),
                                           (10, 'Johnson''s Cottonbuds 200s', 'Toiletries'),
                                           (11, 'Johnson''s Baby Powder 200g', 'Toiletries')
    ON CONFLICT (id) DO NOTHING;

-- ── Store prices (per-store pricing matrix) ────────────────────
INSERT INTO store_prices (item_id, store_id, price_amount) VALUES
                                                               (1, 1, 45.00),   -- Carrots @ Puregold
                                                               (2, 1, 30.00),   -- Bell pepper @ Puregold
                                                               (3, 1, 20.00),   -- Potato @ Puregold
                                                               (4, 1, 320.00),  -- Beef cubes @ Puregold
                                                               (5, 1, 180.00),  -- Ground pork @ Puregold
                                                               (6, 1, 25.00),   -- Napkin @ Puregold
                                                               (6, 2, 32.00),   -- Napkin @ S&R
                                                               (7, 2, 89.00),   -- Toothpaste @ S&R
                                                               (8, 1, 44.50),   -- Coca-Cola Light 1.5L @ Puregold
                                                               (9, 1, 65.00),   -- Pampers Baby Wipes @ Puregold
                                                               (10, 1, 55.00),  -- Johnson's Cottonbuds 200s @ Puregold
                                                               (11, 1, 58.00)   -- Johnson's Baby Powder 200g @ Puregold
    ON CONFLICT (item_id, store_id) DO NOTHING;

-- ── Recipes ──────────────────────────────────────────────────────
INSERT INTO recipes (id, recipe_name, user_id) VALUES
                                                   (1, 'Caldereta', 1),
                                                   (2, 'Giniling', 1)
    ON CONFLICT (id) DO NOTHING;

-- ── Recipe ingredients (base x1 quantities) ────────────────────
INSERT INTO recipe_ingredients (id, recipe_id, item_id, base_quantity, unit) VALUES
                                                                                 (1, 1, 1, 2,   NULL),  -- Caldereta: 2 Carrots
                                                                                 (2, 1, 4, 500, 'g'),   -- Caldereta: 500g Beef cubes
                                                                                 (3, 1, 2, 1,   NULL),  -- Caldereta: 1 Bell pepper
                                                                                 (4, 2, 5, 250, 'g'),   -- Giniling: 250g Ground pork
                                                                                 (5, 2, 1, 1,   NULL),  -- Giniling: 1 Carrot
                                                                                 (6, 2, 3, 2,   NULL)   -- Giniling: 2 Potatoes
    ON CONFLICT (id) DO NOTHING;

-- ── User cart items (current active cart) ──────────────────────
-- source_recipe_id NULL = manually added, defaults to "Others" bucket.
INSERT INTO user_cart_items (id, user_id, item_id, store_id, quantity, source_recipe_id) VALUES
                                                                                             (1, 1, 1, 1, 2, 1),    -- 2 Carrots @ Puregold, from Caldereta
                                                                                             (2, 1, 1, 1, 1, 2),    -- 1 Carrot @ Puregold, from Giniling
                                                                                             (3, 1, 6, 1, 2, NULL), -- 2 Napkins @ Puregold, manual ("Others")
                                                                                             (4, 1, 6, 2, 1, NULL), -- 1 Napkin @ S&R, manual ("Others")
                                                                                             (5, 1, 7, 2, 0, NULL)  -- Toothpaste @ S&R, qty 0 = inactive/excluded
    ON CONFLICT (id) DO NOTHING;

-- ── Purchase history (feeds the Insights tab) ──────────────────
INSERT INTO purchase_history (id, user_id, item_id, store_id, quantity_bought, price_per_unit, purchase_date) VALUES
                                                                                                                  (1, 1, 6, 1, 2, 22.00, '2026-04-12 10:00:00'),
                                                                                                                  (2, 1, 6, 1, 2, 23.00, '2026-05-10 10:00:00'),
                                                                                                                  (3, 1, 6, 1, 1, 24.50, '2026-06-08 10:00:00'),
                                                                                                                  (4, 1, 6, 1, 2, 25.00, '2026-07-05 10:00:00'),
                                                                                                                  (5, 1, 1, 1, 3, 45.00, '2026-07-05 10:00:00'),
                                                                                                                  (6, 1, 2, 1, 2, 30.00, '2026-07-12 10:00:00'),
                                                                                                                  (7, 1, 4, 1, 1, 320.00, '2026-07-05 10:00:00'),
                                                                                                                  (8, 1, 5, 1, 1, 180.00, '2026-07-12 10:00:00'),
                                                                                                                  (9, 1, 6, 2, 1, 32.00, '2026-07-15 10:00:00'),
                                                                                                                  (10, 1, 7, 2, 2, 89.00, '2026-07-15 10:00:00')
    ON CONFLICT (id) DO NOTHING;

-- ── Re-sync identity sequences ──────────────────────────────────
-- Explicit IDs above bypass each table's auto-increment sequence,
-- so bump each sequence past the highest seeded ID. Without this,
-- the first row Hibernate inserts after startup would collide with
-- id=1 and throw a duplicate-key error.
PERFORM setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));
        PERFORM setval(pg_get_serial_sequence('stores', 'id'), (SELECT MAX(id) FROM stores));
        PERFORM setval(pg_get_serial_sequence('items', 'id'), (SELECT MAX(id) FROM items));
        PERFORM setval(pg_get_serial_sequence('recipes', 'id'), (SELECT MAX(id) FROM recipes));
        PERFORM setval(pg_get_serial_sequence('recipe_ingredients', 'id'), (SELECT MAX(id) FROM recipe_ingredients));
        PERFORM setval(pg_get_serial_sequence('user_cart_items', 'id'), (SELECT MAX(id) FROM user_cart_items));
        PERFORM setval(pg_get_serial_sequence('purchase_history', 'id'), (SELECT MAX(id) FROM purchase_history));

        RAISE NOTICE 'Cartculate mock data seeded successfully.';
ELSE
        RAISE NOTICE 'Cartculate seed skipped: items table is not empty.';
END IF;
END $seed$;
@@