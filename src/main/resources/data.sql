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
        INSERT INTO users (id, name, email, current_mode) VALUES
            (1, 'Juan Dela Cruz', 'juan@example.com', 'HOME')
        ON CONFLICT (id) DO NOTHING;

        -- ── Stores ───────────────────────────────────────────────────────
INSERT INTO stores (id, name) VALUES
                                  (1, 'Puregold'),
                                  (2, 'S&R')
    ON CONFLICT (id) DO NOTHING;

-- ── Items (master list) ─────────────────────────────────────────
-- `unit` feeds Feature 1's Pricing Format rule (itemName (unit) e.g.
-- "Carrots (kg)", "Napkin (pack)") on the frontend's CartItem display.
-- NULL = no unit suffix shown (count-based items with no natural unit).
-- `is_ingredient` is NOT NULL on Item.java (defaults to false in Java, but
-- that default only applies to objects built in code - explicit inserts
-- still need a real value) - flags which items appear in the New Recipe
-- modal's ingredient picker (see mockItemData.ts's isIngredient column for
-- the values mirrored here: produce/meat are ingredients, toiletries/drinks
-- are not).
INSERT INTO items (id, name, category, unit, is_ingredient) VALUES
                                                                (1, 'Carrots', 'Vegetables', 'kg', TRUE),
                                                                (2, 'Bell pepper', 'Vegetables', NULL, TRUE),
                                                                (3, 'Potato', 'Vegetables', 'kg', TRUE),
                                                                (4, 'Beef cubes', 'Meat', 'kg', TRUE),
                                                                (5, 'Ground pork', 'Meat', 'kg', TRUE),
                                                                (6, 'Napkin', 'Toiletries', 'pack', FALSE),
                                                                (7, 'Toothpaste', 'Toiletries', NULL, FALSE),
                                                                (8, 'Coca-Cola Light 1.5L', 'Beverages', NULL, FALSE),
                                                                (9, 'Pampers Baby Wipes', 'Toiletries', 'pack', FALSE),
                                                                (10, 'Johnson''s Cottonbuds 200s', 'Toiletries', 'pack', FALSE),
                                                                (11, 'Johnson''s Baby Powder 200g', 'Toiletries', NULL, FALSE)
    ON CONFLICT (id) DO NOTHING;

-- ── Store prices (per-store pricing matrix) ────────────────────
INSERT INTO store_prices (item_id, store_id, price_amount) VALUES
                                                               (1, 1, 45.00),
                                                               (2, 1, 30.00),
                                                               (3, 1, 20.00),
                                                               (4, 1, 320.00),
                                                               (5, 1, 180.00),
                                                               (6, 1, 25.00),
                                                               (6, 2, 32.00),
                                                               (7, 2, 89.00),
                                                               (8, 1, 44.50),
                                                               (9, 1, 65.00),
                                                               (10, 1, 55.00),
                                                               (11, 1, 58.00)
    ON CONFLICT (item_id, store_id) DO NOTHING;

-- ── Recipes ──────────────────────────────────────────────────────
INSERT INTO recipes (id, recipe_name, user_id, current_multiplier) VALUES
                                                                       (1, 'Caldereta', 1, 1),
                                                                       (2, 'Giniling', 1, 1)
    ON CONFLICT (id) DO NOTHING;

-- ── Recipe ingredients (composite PK: recipe_id + item_id) ─────
-- target_store_id NULL = fall back to cheapest-price store (default routing).
-- `is_optional` is NOT NULL on RecipeIngredient.java (defaults to false in
-- Java, same caveat as is_ingredient above) - none of the seed ingredients
-- are optional, so all FALSE.
INSERT INTO recipe_ingredients (recipe_id, item_id, base_quantity, unit, target_store_id, is_optional) VALUES
                                                                                                           (1, 1, 2,   NULL, NULL, FALSE),  -- Caldereta: 2 Carrots
                                                                                                           (1, 4, 500, 'g',  NULL, FALSE), -- Caldereta: 500g Beef cubes
                                                                                                           (1, 2, 1,   NULL, NULL, FALSE),  -- Caldereta: 1 Bell pepper
                                                                                                           (2, 5, 250, 'g',  NULL, FALSE), -- Giniling: 250g Ground pork
                                                                                                           (2, 1, 1,   NULL, NULL, FALSE),  -- Giniling: 1 Carrot
                                                                                                           (2, 3, 2,   NULL, NULL, FALSE)   -- Giniling: 2 Potatoes
    ON CONFLICT (recipe_id, item_id) DO NOTHING;

-- ── User cart items (current active cart / "Inventory Mitigation Engine") ──
-- source_recipe_id NULL = manually added, defaults to "Others" bucket.
-- override_pantry_qty > 0 means some of the needed quantity is already
-- at home (see "Home Mode"), reducing what's actually needed to buy.
INSERT INTO user_cart_item (id, user_id, item_id, store_id, source_recipe_id, quantity, override_pantry_qty, override_reason, is_checked_checkout) VALUES
                                                                                                                                                       (1, 1, 1, 1, 1, 2, 0, NULL, FALSE),                        -- 2 Carrots @ Puregold, from Caldereta
                                                                                                                                                       (2, 1, 1, 1, 2, 1, 0, NULL, FALSE),                        -- 1 Carrot @ Puregold, from Giniling
                                                                                                                                                       (3, 1, 6, 1, NULL, 2, 0, NULL, FALSE),                     -- 2 Napkins @ Puregold, manual ("Others")
                                                                                                                                                       (4, 1, 6, 2, NULL, 1, 1, 'Pantry Stock', FALSE),           -- 1 Napkin @ S&R, 1 already at home
                                                                                                                                                       (5, 1, 7, 2, NULL, 0, 0, NULL, FALSE)                      -- Toothpaste @ S&R, qty 0 = inactive/excluded
    ON CONFLICT (id) DO NOTHING;

-- ── Purchase history (receipt-level, feeds Insights + audit trail) ──
INSERT INTO purchase_history (id, user_id, store_id, total_receipt_spent, purchase_date, item_manifest_json) VALUES
                                                                                                                 (1, 1, 1, 862.50, '2026-07-05 10:00:00',
                                                                                                                  ('[{"itemId":1,"itemName":"Carrots","category":"Vegetables","quantity":3,"pricePerUnit":45.00},' ||
                                                                                                                   '{"itemId":4,"itemName":"Beef cubes","category":"Meat","quantity":1,"pricePerUnit":320.00},' ||
                                                                                                                   '{"itemId":2,"itemName":"Bell pepper","category":"Vegetables","quantity":2,"pricePerUnit":30.00}]')::jsonb),
                                                                                                                 (2, 1, 1, 205.00, '2026-07-12 10:00:00',
                                                                                                                  ('[{"itemId":5,"itemName":"Ground pork","category":"Meat","quantity":1,"pricePerUnit":180.00},' ||
                                                                                                                   '{"itemId":6,"itemName":"Napkin","category":"Toiletries","quantity":1,"pricePerUnit":25.00}]')::jsonb),
                                                                                                                 (3, 1, 2, 153.00, '2026-07-15 10:00:00',
                                                                                                                  ('[{"itemId":6,"itemName":"Napkin","category":"Toiletries","quantity":1,"pricePerUnit":32.00},' ||
                                                                                                                   '{"itemId":7,"itemName":"Toothpaste","category":"Toiletries","quantity":2,"pricePerUnit":89.00}]')::jsonb)
    ON CONFLICT (id) DO NOTHING;

-- ── Re-sync identity sequences ──────────────────────────────────
-- Explicit IDs above bypass each table's auto-increment sequence,
-- so bump each sequence past the highest seeded ID. Without this,
-- the first row Hibernate inserts after startup would collide with
-- id=1 and throw a duplicate-key error.
-- Note: recipe_ingredients has no sequence to bump (composite PK, no surrogate id).
PERFORM setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));
        PERFORM setval(pg_get_serial_sequence('stores', 'id'), (SELECT MAX(id) FROM stores));
        PERFORM setval(pg_get_serial_sequence('items', 'id'), (SELECT MAX(id) FROM items));
        PERFORM setval(pg_get_serial_sequence('recipes', 'id'), (SELECT MAX(id) FROM recipes));
        PERFORM setval(pg_get_serial_sequence('user_cart_item', 'id'), (SELECT MAX(id) FROM user_cart_item));
        PERFORM setval(pg_get_serial_sequence('purchase_history', 'id'), (SELECT MAX(id) FROM purchase_history));

        RAISE NOTICE 'Cartculate mock data seeded successfully.';
ELSE
        RAISE NOTICE 'Cartculate seed skipped: items table is not empty.';
END IF;
END $seed$;
@@