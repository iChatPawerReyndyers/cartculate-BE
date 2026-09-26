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
        -- Demo login: username "juan", password "password123" (bcrypt hash
        -- below is real, generated for that exact password - see AuthService).
        INSERT INTO users (id, name, email, username, password_hash, current_mode) VALUES
            (1, 'Juan Dela Cruz', 'juan@example.com', 'juan', '$2b$10$/aKGlzrGHwVZ3ehBDTMj2Osq0.lyXTzPoT6rWOm3mQ39i4KLdWTFC', 'HOME')
        ON CONFLICT (id) DO NOTHING;

        -- ── Stores ───────────────────────────────────────────────────────
INSERT INTO stores (id, name) VALUES
                                  (1, 'Puregold'),
                                  (2, 'S&R BGC'),
                                  (3, 'SM Aura'),
                                  (4, 'Super 8 Elisco'),
                                  (5, 'Market Market')
    ON CONFLICT (id) DO NOTHING;

-- ── Items (master list) ─────────────────────────────────────────
-- `unit` feeds Feature 1's Pricing Format rule (itemName (unit) e.g.
-- "Carrots (kg)", "Napkin (pack)") on the frontend's CartItem display.
-- NULL = no unit suffix shown (count-based items with no natural unit).
-- `is_ingredient` is NOT NULL on Item.java (defaults to false in Java, but
-- that default only applies to objects built in code - explicit inserts
-- still need a real value) - flags which items appear in the New Recipe
-- modal's ingredient picker (see mockItemData.ts's isIngredient column for
-- the values mirrored here).
INSERT INTO items (id, name, category, unit, is_ingredient) VALUES
                                                                -- SM Aura Additions
                                                                (12, 'Kara Coconut Cream', 'Condiments', '50g', TRUE),
                                                                (13, 'Lee Kum Kee Sweet & Sour Spare Ribs Sauce', 'Condiments', '80g', TRUE),
                                                                (14, 'Lee Kum Kee Seafood Soup Base', 'Condiments', '50g', TRUE),
                                                                (15, 'Lee Kum Kee Pork Bone Soup Base', 'Condiments', '50g', TRUE),
                                                                (16, 'Chnkang Ramen', 'Noodles', '200g', TRUE),
                                                                (17, 'Chnkang Udon', 'Noodles', '280g', TRUE),
                                                                (18, 'Cebu Best Dried Mango', 'Snacks', '200g', TRUE),
                                                                (19, 'Chnkang Multi Grain', 'Packaged Foods', '280g', TRUE),
                                                                (20, 'Fudgee Barr Macapuno', 'Snacks', '10pc', TRUE),
                                                                (21, 'Suncrest Fudge Barr Dark Chocolate', 'Snacks', '38g', TRUE),
                                                                (22, 'Suncrest Fudgee Barr Combo', 'Snacks', '10pc', TRUE),
                                                                (23, 'Payless Xtra Big Chilimansi', 'Noodles', '125g', TRUE),

                                                                -- Super 8 Elisco Additions
                                                                (24, 'Del Monte Juice Pineapple with ACE', 'Beverages', '1L', TRUE),
                                                                (25, 'Del Monte Pineapple Tidbits', 'Packaged Foods', '200g', TRUE),
                                                                (26, 'Jufran Spray Deodorant Body Spray Bluewater', 'Toiletries', '60ml', FALSE), -- Non-food
                                                                (27, 'Lucky Me Supreme Cup Mini Sotanghon', 'Noodles', '28g', TRUE),
                                                                (28, 'Nestea Iced Tea Powder Lemon Cucumber', 'Beverages', '19g', TRUE),
                                                                (29, 'Nestea Iced Tea Powder Cranberry', 'Beverages', '19g', TRUE),
                                                                (30, 'Nestea Iced Tea Powder Honey Blend', 'Beverages', '19g', TRUE),
                                                                (31, 'Nissin Cup Mini Sotanghon Chicken', 'Noodles', '30g', TRUE),
                                                                (32, 'Piña Fish Sauce Pouch', 'Condiments', '150ml', TRUE),
                                                                (33, 'Smirnoff Mule', 'Beverages', '330ml', TRUE),
                                                                (34, 'Dr. S. Wong''s Sulfur Soap Moisturizing', 'Toiletries', '135g', FALSE), -- Non-food
                                                                (35, 'Tanduay Ice Vodka Lemonade', 'Beverages', '330ml', TRUE),
                                                                (36, 'UFC Banana Ketchup', 'Condiments', '1kg', TRUE),
                                                                (37, 'Yum Yum Snack Choco', 'Snacks', '30g', TRUE),
                                                                (38, 'Yum Yum Snack Milk', 'Snacks', '30g', TRUE),
                                                                (39, 'Yum Yum Snack Strawberry', 'Snacks', '30g', TRUE),
                                                                (40, 'Zim Scrub Sponge', 'Household', '1pc', FALSE), -- Non-food

                                                                -- Market Market Additions (Batch 1)
                                                                (41, 'Coca-Cola Regular', 'Beverages', '500ml', TRUE),
                                                                (42, 'Nescafé RTD Cappuccino', 'Beverages', '180ml', TRUE),
                                                                (43, 'Neubake White Bread', 'Bakery', '450g', TRUE),
                                                                (44, 'Minute Maid Fresh Orange', 'Beverages', '800ml', TRUE),
                                                                (45, 'Tiffany Hotdog Roll', 'Bakery', '8pc', TRUE),
                                                                (46, 'Nature''s Call Bathroom Deodorizer', 'Household', '1pc', FALSE), -- Non-food
                                                                (47, 'Zim Cleaning Pads Scrub Jr.', 'Household', '1pc', FALSE), -- Non-food
                                                                (48, 'Zim Scouring Pad with Sponge', 'Household', '1pc', FALSE), -- Non-food
                                                                (49, 'Payless Payless Xtra Big Chilimansi', 'Noodles', '128g', TRUE),
                                                                (50, 'Palawan Honey Queen', 'Condiments', '375ml', TRUE),
                                                                (51, 'Nestea Honey Blend Powder', 'Beverages', '19g', TRUE),
                                                                (52, 'Tang Mixed Berries Juice Powder', 'Beverages', '20g', TRUE),
                                                                (53, 'Tang Dalandan Litro Pack Powder', 'Beverages', '20g', TRUE),
                                                                (54, 'Tang Pineapple Litro Pack Powder', 'Beverages', '20g', TRUE),
                                                                (55, 'Cebu Dried Mango Slices', 'Snacks', '200g', TRUE),
                                                                (56, 'Ajinomoto Powder Seasoning Mix', 'Condiments', '100g', TRUE),
                                                                (57, 'Trolli Kiss', 'Snacks', '40g', TRUE),
                                                                (58, 'Sunbest Coconut Cream Powder', 'Condiments', '50g', TRUE),
                                                                (59, 'Imaba Yellowfin Tuna Pudding', 'Packaged Foods', '1pc', TRUE),
                                                                (60, 'CDO Idol Cheesedog Jumbo', 'Meat', '500g', TRUE),
                                                                (61, 'Selecta Fortified Milk', 'Dairy', '1L', TRUE),
                                                                (62, 'Rafael Salgado Extra Virgin Olive Oil', 'Condiments', '250ml', TRUE),
                                                                (63, 'Lucky Me! Supreme Mini Cup La Paz Batchoy', 'Noodles', '40g', TRUE),
                                                                (64, 'Lucky Me! Supreme Mini Cup Bulalo', 'Noodles', '40g', TRUE),
                                                                (65, 'Joy Heavy Duty Liquid Kalamansi', 'Household', '780ml', FALSE), -- Non-food

                                                                -- S&R BGC Additions
                                                                (66, 'Ribeye Steak', 'Meat', '1kg', TRUE),
                                                                (67, 'Frabelle Ground Beef', 'Meat', '1pc', TRUE),
                                                                (68, 'Yakult', 'Dairy', '5 x 80ml', TRUE),
                                                                (69, 'Purefoods Tender Juicy Hotdog Jumbo', 'Meat', '1kg', TRUE),
                                                                (70, 'Chicken Neckless', 'Meat', '1kg', TRUE),
                                                                (71, 'Lotte Milk Ice Cream', 'Dairy', '625ml', TRUE),
                                                                (72, 'Cowhead Regular Milk', 'Dairy', '1L', TRUE),
                                                                (73, 'Arla Full Cream Milk', 'Dairy', '1L', TRUE),
                                                                (74, 'Arla Low Fat Milk', 'Dairy', '1L', TRUE),
                                                                (75, 'Meiji Low Fat Yogurt', 'Dairy', '500g', TRUE),
                                                                (76, 'Del Monte Pineapple Juice', 'Beverages', '2 x 46oz', TRUE),
                                                                (77, 'S&R Generic Ecobag', 'Household', '1pc', FALSE), -- Non-food
                                                                (78, 'Minute Maid Pitcher', 'Beverages', '400ml', TRUE),
                                                                (79, 'Minute Maid Blue', 'Beverages', '400ml', TRUE),
                                                                (80, 'Rotisserie Chicken', 'Packaged Foods', '1pc', TRUE),
                                                                (81, 'Spam Less Sodium', 'Packaged Foods', '340g', TRUE),
                                                                (82, 'Honey Ginger Tea', 'Beverages', '1kg', TRUE),
                                                                (83, 'KitKat Matcha', 'Snacks', '113g', TRUE),
                                                                (84, 'Fanta Grape', 'Beverages', '500ml', TRUE),
                                                                (85, 'Fanta Orange', 'Beverages', '500ml', TRUE),
                                                                (86, 'Bounty Fresh Chicken Wings / Mix Bag', 'Meat', '333g', TRUE),
                                                                (87, 'Sunlly Cola', 'Beverages', '480ml', TRUE),
                                                                (88, 'Pringles Cheddar Cheese', 'Snacks', '158g', TRUE),
                                                                (89, 'Coca-Cola Zero Sugar', 'Beverages', '1L', TRUE),
                                                                (90, 'Tostitos Chunky Salsa', 'Condiments', '1pc', TRUE),
                                                                (91, 'HBAF Honey Butter Almond', 'Snacks', '1pc', TRUE),
                                                                (92, 'Dan D Pak Popcorn Kernels', 'Snacks', '1pc', TRUE),
                                                                (93, 'Coca-Cola', 'Beverages', '1.5L', TRUE),
                                                                (94, 'Samyang Mushroom Ramen', 'Noodles', '105g', TRUE),
                                                                (95, 'Cheez Whiz Pimiento', 'Dairy', '440g', TRUE),
                                                                (96, 'Indomie Mi Goreng', 'Noodles', '10pc', TRUE),
                                                                (97, 'Nongshim Shin Ramyun', 'Noodles', '120g', TRUE),
                                                                (98, 'S&R Heat Seal', 'Household', '1pc', FALSE), -- Non-food
                                                                (99, 'Chocolate Strawberry Marble Ring Cake', 'Bakery', '1pc', TRUE),
                                                                (100, 'Chocolate Marble Ring Cake', 'Bakery', '1pc', TRUE),

                                                                -- Market Market Additions (Batch 2)
                                                                (101, 'Dutch Mill Yoghurt Drink Mixed Berries', 'Beverages', '4-pack', TRUE),
                                                                (102, 'Magnolia Buttercup', 'Dairy', '200g', TRUE),
                                                                (103, 'CF Wonton Wrapper', 'Packaged Foods', '100pc', TRUE),
                                                                (104, 'Marby Hungarian Sausage', 'Meat', '600g', TRUE),
                                                                (105, 'Guava Candy', 'Snacks', '50g', TRUE),
                                                                (106, 'Seasoned Seaweed Laver (Dongwon)', 'Snacks', '3pc', TRUE),
                                                                (107, 'Butterkist', 'Snacks', '1pc', TRUE),
                                                                (108, 'Mang Tomas Lechon Sauce Hot', 'Condiments', '12oz', TRUE),
                                                                (109, 'Lee Kum Kee Hoisin Sauce', 'Condiments', '1pc', TRUE),
                                                                (110, 'Bega Peanut Butter Crunchy', 'Condiments', '375g', TRUE),
                                                                (111, 'S&B Golden Curry Hot', 'Condiments', '220g', TRUE),
                                                                (112, 'Payless Xtra Big Multipack', 'Noodles', '1pc', TRUE),
                                                                (113, 'KWP Mayo Mayonnaise Japanese Style', 'Condiments', '1pc', TRUE),
                                                                (114, 'Nestea Lemon Cucumber Iced Tea', 'Beverages', '19g', TRUE),
                                                                (115, 'Del Monte Original Blend Ketchup', 'Condiments', '1pc', TRUE),
                                                                (116, 'Del Monte Tomato Paste Super', 'Condiments', '150g', TRUE),
                                                                (117, 'Heinz Tomato Pouch', 'Condiments', '120g', TRUE),
                                                                (118, 'Mang Tomas Pack', 'Condiments', '1pc', TRUE),
                                                                (119, 'Cheez Whiz Regular', 'Dairy', '160g', TRUE),
                                                                (120, 'Hanmei Snow Crab Ramen', 'Noodles', '118g', TRUE),
                                                                (121, 'Hanmei RAMYEON Black Pork Ramen', 'Noodles', '118g', TRUE),
                                                                (122, 'Philippine Dried Mango Chips', 'Snacks', '100g', TRUE)
    ON CONFLICT (id) DO NOTHING;

-- ── Store prices (per-store pricing matrix) ────────────────────
INSERT INTO store_prices (item_id, store_id, price_amount) VALUES
                                                               -- Prices linked to SM Aura (store_id = 3)
                                                               (12, 3, 41.50),
                                                               (13, 3, 65.00),
                                                               (14, 3, 65.00),
                                                               (15, 3, 65.00),
                                                               (16, 3, 39.50),
                                                               (17, 3, 39.50),
                                                               (18, 3, 249.50),
                                                               (19, 3, 99.50),
                                                               (20, 3, 99.50),
                                                               (21, 3, 99.50),
                                                               (22, 3, 96.50),
                                                               (23, 3, 18.25),

                                                               -- Prices linked to Super 8 Elisco (store_id = 4)
                                                               (24, 4, 106.70),
                                                               (25, 4, 30.75),
                                                               (26, 4, 90.95),
                                                               (27, 4, 25.50),
                                                               (28, 4, 19.95),
                                                               (29, 4, 22.00),
                                                               (30, 4, 22.50),
                                                               (31, 4, 25.25),
                                                               (32, 4, 15.10),
                                                               (33, 4, 47.05),
                                                               (34, 4, 53.80),
                                                               (35, 4, 39.00),
                                                               (36, 4, 76.30),
                                                               (37, 4, 16.50),
                                                               (38, 4, 16.80),
                                                               (39, 4, 17.85),
                                                               (40, 4, 17.40),

                                                               -- Prices linked to Market Market (store_id = 5)
                                                               (41, 5, 38.25),
                                                               (42, 5, 38.25),
                                                               (43, 5, 30.00),
                                                               (44, 5, 59.25),
                                                               (45, 5, 54.00),
                                                               (46, 5, 57.50),
                                                               (47, 5, 22.50),
                                                               (48, 5, 30.50),
                                                               (49, 5, 18.25),
                                                               (50, 5, 243.25),
                                                               (51, 5, 21.95),
                                                               (52, 5, 21.45),
                                                               (53, 5, 19.25),
                                                               (54, 5, 19.25),
                                                               (55, 5, 259.75),
                                                               (56, 5, 52.00),
                                                               (57, 5, 32.00),
                                                               (58, 5, 41.50),
                                                               (59, 5, 59.00),
                                                               (60, 5, 113.00),
                                                               (61, 5, 173.40),
                                                               (62, 5, 310.45),
                                                               (63, 5, 24.25),
                                                               (64, 5, 24.25),
                                                               (65, 5, 175.00),
                                                               -- Market Market Batch 2 Entries
                                                               (101, 5, 49.50),
                                                               (102, 5, 49.00),
                                                               (103, 5, 57.50),
                                                               (104, 5, 242.00),
                                                               (105, 5, 37.50),
                                                               (106, 5, 99.00),
                                                               (107, 5, 44.50),
                                                               (108, 5, 40.25),
                                                               (109, 5, 157.90),
                                                               (110, 5, 268.00),
                                                               (111, 5, 246.75),
                                                               (112, 5, 73.00),
                                                               (113, 5, 89.00),
                                                               (114, 5, 21.50),
                                                               (115, 5, 32.50),
                                                               (116, 5, 34.50),
                                                               (117, 5, 23.50),
                                                               (118, 5, 13.00),
                                                               (119, 5, 45.45),
                                                               (120, 5, 88.00),
                                                               (121, 5, 72.50),
                                                               (122, 5, 76.00),

                                                               -- Prices linked to S&R BGC (store_id = 2)
                                                               (66, 2, 899.00),
                                                               (67, 2, 349.00),
                                                               (68, 2, 48.50),
                                                               (69, 2, 197.00),
                                                               (70, 2, 180.00),
                                                               (71, 2, 149.00),
                                                               (72, 2, 97.50),
                                                               (73, 2, 96.00),
                                                               (74, 2, 98.00),
                                                               (75, 2, 221.00),
                                                               (76, 2, 250.00),
                                                               (77, 2, 59.00),
                                                               (78, 2, 59.00),
                                                               (79, 2, 59.00),
                                                               (80, 2, 268.00),
                                                               (81, 2, 224.00),
                                                               (82, 2, 369.00),
                                                               (83, 2, 229.00),
                                                               (84, 2, 73.00),
                                                               (85, 2, 79.00),
                                                               (86, 2, 269.00),
                                                               (87, 2, 139.00),
                                                               (88, 2, 99.00),
                                                               (89, 2, 189.00),
                                                               (90, 2, 196.00),
                                                               (91, 2, 329.00),
                                                               (92, 2, 149.00),
                                                               (93, 2, 64.00),
                                                               (94, 2, 99.00),
                                                               (95, 2, 197.00),
                                                               (96, 2, 160.00),
                                                               (97, 2, 299.00),
                                                               (98, 2, 10.00),
                                                               (99, 2, 379.00),
                                                               (100, 2, 379.00)
    ON CONFLICT (item_id, store_id) DO NOTHING;

END IF;
END $seed$;
@@

-- ════════════════════════════════════════════════════════════════════
-- Pasig market catalog (added later) - shared products + prices for EVERY
-- user, from the "Pasig Mega Market" price list.
--
-- Unlike the block above (which only runs into an EMPTY database), this
-- one is meant to run on an EXISTING database too, so it:
--   * re-syncs the id counters first (the block above inserts explicit ids,
--     which leaves them behind and would make the inserts below collide),
--   * reuses an item that already exists by name (ignoring capitals) instead
--     of duplicating it - it only adds this store's price if missing,
--   * records itself in data_seed_log so it runs ONCE; products deleted
--     later are not re-added on the next restart.
-- To change the store name, edit v_store_name BEFORE the first run.
-- To run it again, delete its row from data_seed_log.
-- ════════════════════════════════════════════════════════════════════
DO $pasig$
DECLARE
v_seed_key   CONSTANT TEXT := 'pasig-market-catalog-v1';
    v_store_name CONSTANT TEXT := 'Pasig Mega Market';
    v_store_id   BIGINT;
    v_item_id    BIGINT;
    r            RECORD;
BEGIN
CREATE TABLE IF NOT EXISTS data_seed_log (
                                             seed_key   VARCHAR(100) PRIMARY KEY,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

IF EXISTS (SELECT 1 FROM data_seed_log WHERE seed_key = v_seed_key) THEN
        RETURN;
END IF;

    PERFORM setval(pg_get_serial_sequence('stores', 'id'), COALESCE((SELECT MAX(id) FROM stores), 0) + 1, false);
    PERFORM setval(pg_get_serial_sequence('items', 'id'), COALESCE((SELECT MAX(id) FROM items), 0) + 1, false);

SELECT id INTO v_store_id FROM stores WHERE lower(name) = lower(v_store_name) LIMIT 1;
IF v_store_id IS NULL THEN
        INSERT INTO stores (name) VALUES (v_store_name) RETURNING id INTO v_store_id;
END IF;

FOR r IN
SELECT * FROM (VALUES
                   ('Pork Kasim', 'Meat', 'kg', 240.00),
                   ('Pork Tenga', 'Meat', 'kg', 170.00),
                   ('Pork Pigue', 'Meat', 'kg', 200.00),
                   ('Pork Pisngi / Maskara', 'Meat', 'kg', 235.00),
                   ('Pork Liempo', 'Meat', 'kg', 320.00),
                   ('Pork Lomo', 'Meat', 'kg', 345.00),
                   ('Whole Chicken', 'Meat', 'kg', 190.00),
                   ('Chicken Choice Cuts', 'Meat', 'kg', 215.00),
                   ('Chicken Drumsticks/Wings', 'Meat', 'kg', 225.00),
                   ('Chicken Liver & Gizzard', 'Meat', 'kg', 220.00),
                   ('Frozen Beef Balls/Patties', 'Refrigerated/Frozen Goods', 'pack', 170.00),
                   ('Frozen Nuggets/Hotdogs', 'Refrigerated/Frozen Goods', 'pack', 195.00),
                   ('Tilapia', 'Seafood', 'kg', 145.00),
                   ('Bangus', 'Seafood', 'kg', 200.00),
                   ('Galunggong', 'Seafood', 'kg', 230.00),
                   ('Shrimp', 'Seafood', 'kg', 415.00),
                   ('Tahong', 'Seafood', 'kg', 115.00),
                   ('Hito', 'Seafood', 'kg', 200.00),
                   ('Tinapa / Daing na Biya', 'Seafood', 'pack', 60.00),
                   ('Green Munggo Beans', 'Dry Goods', 'kg', 88.00),
                   ('Chicharon Bits / Skin', 'Dry Goods', 'pack', 45.00),
                   ('Dahon ng Sili', 'Vegetables', 'bundle', 15.00),
                   ('Malunggay Leaves', 'Vegetables', 'bundle', 15.00),
                   ('Talbos ng Kamote', 'Vegetables', 'bundle', 20.00),
                   ('Dahon ng Kangkong', 'Vegetables', 'bundle', 15.00),
                   ('Native Pechay', 'Vegetables', 'bundle', 20.00),
                   ('Alugbati', 'Vegetables', 'bundle', 18.00),
                   ('Saluyot', 'Vegetables', 'bundle', 15.00),
                   ('Pako', 'Vegetables', 'bundle', 33.00),
                   ('Mustasa', 'Vegetables', 'bundle', 20.00),
                   ('Chayote', 'Vegetables', 'kg', 95.00),
                   ('Green Papaya', 'Vegetables', 'kg', 50.00),
                   ('Ampalaya', 'Vegetables', 'kg', 85.00),
                   ('Sitaw', 'Vegetables', 'bundle', 30.00),
                   ('Kalabasa', 'Vegetables', 'kg', 50.00),
                   ('Eggplant', 'Vegetables', 'kg', 75.00),
                   ('Okra', 'Vegetables', 'pack', 20.00),
                   ('Repolyo', 'Vegetables', 'kg', 100.00),
                   ('Carrots', 'Vegetables', 'pc', 25.00),
                   ('Potato', 'Vegetables', 'pc', 25.00),
                   ('Cauliflower', 'Vegetables', 'kg', 260.00),
                   ('Broccoli', 'Vegetables', 'kg', 240.00),
                   ('Chicharo', 'Vegetables', 'pack', 65.00),
                   ('Young Corn', 'Vegetables', 'pack', 40.00),
                   ('Bell Pepper', 'Vegetables', 'pc', 20.00),
                   ('Quail Eggs', 'Pantry', 'tray', 80.00),
                   ('Garlic', 'Vegetables', 'pack', 20.00),
                   ('Onion', 'Vegetables', 'pack', 20.00),
                   ('Ginger', 'Vegetables', 'kg', 120.00),
                   ('Tomatoes', 'Vegetables', 'pack', 20.00),
                   ('Bagoong Alamang', 'Condiments', 'pc', 70.00),
                   ('Siling Haba', 'Vegetables', 'kg', 125.00),
                   ('Laing', 'Vegetables', 'pack', 50.00),
                   ('Rambutan', 'Fruits', 'kg', 115.00),
                   ('Avocado', 'Fruits', 'kg', 150.00),
                   ('Pineapple', 'Fruits', 'pc', 80.00),
                   ('Orange', 'Fruits', 'pc', 20.00),
                   ('Lakatan Banana', 'Fruits', 'kg', 90.00),
                   ('Latundan Banana', 'Fruits', 'kg', 70.00),
                   ('Saba Banana (kg)', 'Fruits', 'kg', 60.00),
                   ('Saba Banana (pc)', 'Fruits', 'pc', 4.00)
              ) AS t (name, category, unit, price)
    LOOP
SELECT id INTO v_item_id FROM items WHERE lower(name) = lower(r.name) ORDER BY id LIMIT 1;
IF v_item_id IS NULL THEN
            INSERT INTO items (name, category, unit, is_ingredient, include_in_cart, default_store_id)
            VALUES (r.name, r.category, r.unit, TRUE, TRUE, v_store_id)
            RETURNING id INTO v_item_id;
END IF;

INSERT INTO store_prices (item_id, store_id, price_amount, price_source)
VALUES (v_item_id, v_store_id, r.price, 'MANUAL')
    ON CONFLICT (item_id, store_id) DO NOTHING;
END LOOP;

INSERT INTO data_seed_log (seed_key) VALUES (v_seed_key);
END
$pasig$;
@@

-- ════════════════════════════════════════════════════════════════════
-- Ingredient flags: products that should NOT show in the recipe
-- ingredient picker (drinks, snacks, desserts, dairy treats, ready-to-eat
-- foods, cup noodles, and the fruits eaten as-is). Everything else keeps
-- its current is_ingredient value.
--
-- Runs ONCE (recorded in data_seed_log) so that if a product is later
-- ticked back to "Ingredient" in the Pricing tab it is not switched off
-- again on the next restart. To run it again, delete its row from
-- data_seed_log. Names are matched ignoring capitals; the LIKE patterns
-- cover "Nestle in general" (Nestea / Nescafe / Nestle), "any vodka" and
-- "any Tang".
-- ════════════════════════════════════════════════════════════════════
DO $ingredientflags$
DECLARE
v_seed_key CONSTANT TEXT := 'ingredient-flags-v1';
BEGIN
CREATE TABLE IF NOT EXISTS data_seed_log (
                                             seed_key   VARCHAR(100) PRIMARY KEY,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

IF EXISTS (SELECT 1 FROM data_seed_log WHERE seed_key = v_seed_key) THEN
        RETURN;
END IF;

UPDATE items
SET is_ingredient = FALSE
WHERE is_ingredient = TRUE
  AND (
    lower(name) IN (
                    'arla full cream milk',
                    'arla low fat milk',
                    'butterkist',
                    'cebu dried mango slices',
                    'chocolate marble ring cake',
                    'chocolate strawberry marble ring cake',
                    'coca-cola',
                    'coca-cola regular',
                    'coca-cola zero sugar',
                    'cowhead regular milk',
                    'dan d pak popcorn kernels',
                    'del monte pineapple juice',
                    'fanta grape',
                    'fanta orange',
                    'guava candy',
                    'hbaf honey butter almond',
                    'imaba yellowfin tuna pudding',
                    'indomie mi goreng',
                    'kitkat matcha',
                    'lakatan banana',
                    'latundan banana',
                    'lotte milk ice cream',
                    'lucky me! supreme mini cup la paz batchoy',
                    'lucky me! supreme mini cup bulalo',
                    'minute maid blue',
                    'minute maid fresh orange',
                    'minute maid pitcher',
                    'neubake white bread',
                    'nissin cup mini sotanghon chicken',
                    'orange',
                    'philippine dried mango chips',
                    'rambutan',
                    'saba banana (kg)',
                    'selecta fortified milk',
                    'smirnoff mule',
                    'suncrest fudge barr dark chocolate',
                    'sunlly cola',
                    'tostitos chunky salsa',
                    'trolli kiss',
                    'yakult'
        )
        OR lower(name) LIKE 'nestea%'
        OR lower(name) LIKE 'nescaf%'
        OR lower(name) LIKE 'nestle%'
        OR lower(name) LIKE '%vodka%'
        OR lower(name) LIKE 'tang %'
        OR lower(name) LIKE 'hair color%'
    );

INSERT INTO data_seed_log (seed_key) VALUES (v_seed_key);
END
$ingredientflags$;
@@

-- ════════════════════════════════════════════════════════════════════
-- Default store per category (the Pricing tab's "Category defaults"):
--   * Fruits, Vegetables, Meat, Seafood, Dry Goods -> the Pasig store
--   * every other category                          -> Puregold
-- These are the defaults the "Select ingredient" / "Add product" forms
-- pre-select for a NEW product, and they never change existing products.
--
-- Covers the app's built-in category list plus every category already used
-- by a product. A category someone has ALREADY configured keeps its own
-- store (only a missing one is filled in). Runs ONCE (recorded in
-- data_seed_log). If the Pasig or Puregold store can't be found it does
-- nothing and simply tries again on the next start.
-- ════════════════════════════════════════════════════════════════════
DO $categorydefaults$
DECLARE
v_seed_key CONSTANT TEXT := 'category-default-stores-v1';
    v_puregold BIGINT;
    v_pasig    BIGINT;
BEGIN
CREATE TABLE IF NOT EXISTS data_seed_log (
                                             seed_key   VARCHAR(100) PRIMARY KEY,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

IF EXISTS (SELECT 1 FROM data_seed_log WHERE seed_key = v_seed_key) THEN
        RETURN;
END IF;

SELECT id INTO v_puregold FROM stores WHERE lower(name) = 'puregold' LIMIT 1;
SELECT id INTO v_pasig    FROM stores WHERE lower(name) LIKE 'pasig%' ORDER BY id LIMIT 1;
IF v_puregold IS NULL OR v_pasig IS NULL THEN
        RAISE NOTICE 'category defaults: Puregold or Pasig store not found yet, skipping for now';
        RETURN;
END IF;

INSERT INTO category_defaults (category, default_store_id, default_is_ingredient)
SELECT c.category,
       CASE WHEN c.category IN ('Fruits', 'Vegetables', 'Meat', 'Seafood', 'Dry Goods')
                THEN v_pasig ELSE v_puregold END,
       FALSE
FROM (
         SELECT unnest(ARRAY['Fruits', 'Vegetables', 'Refrigerated/Frozen Goods', 'Condiments/Sauces', 'Spices', 'Canned Goods', 'Noodles', 'Dairy', 'Meat', 'Seafood', 'Drinks', 'Snacks', 'Pets', 'Personal Care', 'Medicine', 'Cleaning', 'Office/School Supplies', 'Others']) AS category
         UNION
         SELECT DISTINCT category FROM items WHERE category IS NOT NULL AND category <> ''
     ) c
    ON CONFLICT (category) DO UPDATE
                                  SET default_store_id = COALESCE(category_defaults.default_store_id, EXCLUDED.default_store_id);

INSERT INTO data_seed_log (seed_key) VALUES (v_seed_key);
END
$categorydefaults$;
@@

-- ════════════════════════════════════════════════════════════════════
-- Category defaults become PER-USER: copies whatever is already in the
-- old, app-wide "category_defaults" table (set by earlier blocks above,
-- e.g. Puregold/Pasig per category, plus anything configured by hand in
-- the Pricing tab) into the new "user_category_defaults" table, scoped to
-- the first/only user. From here on the app reads and writes only the
-- per-user table (see CategoryDefaultService.java); the old table is left
-- in place, untouched, purely as a historical record.
--
-- Depends on Hibernate having already created "user_category_defaults"
-- (ddl-auto=update runs before this file - see application.properties).
-- If that table isn't there yet for some reason, this simply does nothing
-- and tries again on the next start. Runs ONCE (recorded in
-- data_seed_log), so a category a user later clears is not restored.
-- ════════════════════════════════════════════════════════════════════
DO $categorydefaultsperuser$
DECLARE
v_seed_key CONSTANT TEXT := 'category-defaults-per-user-v1';
    v_user_id  BIGINT;
BEGIN
CREATE TABLE IF NOT EXISTS data_seed_log (
                                             seed_key   VARCHAR(100) PRIMARY KEY,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

IF EXISTS (SELECT 1 FROM data_seed_log WHERE seed_key = v_seed_key) THEN
        RETURN;
END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'user_category_defaults') THEN
        RAISE NOTICE 'category defaults per-user: user_category_defaults table not created yet, skipping for now';
        RETURN;
END IF;

SELECT id INTO v_user_id FROM users ORDER BY id LIMIT 1;
IF v_user_id IS NULL THEN
        RETURN;
END IF;

INSERT INTO user_category_defaults (user_id, category, default_store_id, default_is_ingredient)
SELECT v_user_id, cd.category, cd.default_store_id, cd.default_is_ingredient
FROM category_defaults cd
WHERE NOT EXISTS (
    SELECT 1 FROM user_category_defaults ucd
    WHERE ucd.user_id = v_user_id AND ucd.category = cd.category
);

INSERT INTO data_seed_log (seed_key) VALUES (v_seed_key);
END
$categorydefaultsperuser$;
@@

-- ════════════════════════════════════════════════════════════════════
-- Alternate units for costing recipes (Item.altUnit / altUnitQuantity -
-- see Item.java's javadoc). These items are priced per PIECE in the
-- catalog, but a recipe might record the quantity in KG instead (e.g.
-- "0.5 kg carrots"); this tells the app how to convert that into a piece
-- count so the recipe's cost is calculated correctly rather than treating
-- "0.5" as 0.5 of a piece. The numbers below are rough kitchen averages,
-- not measured - adjust per product any time from the Pricing tab (once
-- that editor ships) or by hand in this table; this migration only ever
-- fills in a currently-empty alt_unit, so a value someone sets afterward
-- is never overwritten by a later deploy.
-- ════════════════════════════════════════════════════════════════════
DO $itemaltunits$
DECLARE
v_seed_key CONSTANT TEXT := 'item-alt-units-v1';
    r RECORD;
BEGIN
CREATE TABLE IF NOT EXISTS data_seed_log (
                                             seed_key   VARCHAR(100) PRIMARY KEY,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

IF EXISTS (SELECT 1 FROM data_seed_log WHERE seed_key = v_seed_key) THEN
        RETURN;
END IF;

FOR r IN
SELECT * FROM (VALUES
                   ('Carrots', 'kg', 0.10),          -- 1 pc ≈ 100g average
                   ('Potato', 'kg', 0.15),           -- 1 pc ≈ 150g average
                   ('Bell Pepper', 'kg', 0.15),      -- 1 pc ≈ 150g average
                   ('Pineapple', 'kg', 1.20),        -- 1 pc ≈ 1.2kg average
                   ('Orange', 'kg', 0.20),           -- 1 pc ≈ 200g average
                   ('Saba Banana (pc)', 'kg', 0.10)  -- 1 pc ≈ 100g average
              ) AS t (name, alt_unit, alt_unit_quantity)
    LOOP
UPDATE items
SET alt_unit = r.alt_unit,
    alt_unit_quantity = r.alt_unit_quantity
WHERE lower(name) = lower(r.name)
  AND alt_unit IS NULL;
END LOOP;

INSERT INTO data_seed_log (seed_key) VALUES (v_seed_key);
END
$itemaltunits$;
@@

-- ════════════════════════════════════════════════════════════════════
-- Fix: category defaults were coming up "None set" for everyone (the
-- earlier migrations only ever filled in an EMPTY default, and something
-- upstream of them - the old shared category_defaults table, or the
-- per-user copy - never actually got populated, so there was nothing to
-- fill in from). This sets it directly and definitively this time, for
-- every user that exists right now:
--   * Fresh goods (Fruits, Vegetables, Meat, Seafood, Dairy,
--     Refrigerated/Frozen Goods) -> the Pasig store
--   * Every other category                                -> Puregold
--
-- Unlike the earlier "only fill in a blank" migrations, this one
-- OVERWRITES whatever store is currently set for these categories (since
-- the reported problem is that it's wrong/blank for everyone right now).
-- Runs ONCE per user (guarded by data_seed_log, one key per user so a
-- user created later still gets it applied), so a store you change
-- afterward in the Pricing tab is not reset back by a future deploy.
-- ════════════════════════════════════════════════════════════════════
DO $freshgoodscategorydefaults$
DECLARE
v_puregold BIGINT;
    v_pasig    BIGINT;
    v_user     RECORD;
    v_seed_key TEXT;
BEGIN
CREATE TABLE IF NOT EXISTS data_seed_log (
                                             seed_key   VARCHAR(100) PRIMARY KEY,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

SELECT id INTO v_puregold FROM stores WHERE lower(name) = 'puregold' LIMIT 1;
SELECT id INTO v_pasig    FROM stores WHERE lower(name) LIKE 'pasig%' ORDER BY id LIMIT 1;
IF v_puregold IS NULL OR v_pasig IS NULL THEN
        RAISE NOTICE 'fresh goods category defaults: Puregold or Pasig store not found yet, skipping for now';
        RETURN;
END IF;

FOR v_user IN SELECT id FROM users LOOP
    v_seed_key := 'fresh-goods-category-defaults-v1:' || v_user.id;
IF EXISTS (SELECT 1 FROM data_seed_log WHERE seed_key = v_seed_key) THEN
            CONTINUE;
END IF;

INSERT INTO user_category_defaults (user_id, category, default_store_id, default_is_ingredient)
SELECT v_user.id, c.category,
       CASE WHEN c.category IN ('Fruits', 'Vegetables', 'Meat', 'Seafood', 'Dairy', 'Refrigerated/Frozen Goods') THEN v_pasig ELSE v_puregold END,
       FALSE
FROM (
         SELECT unnest(ARRAY['Fruits', 'Vegetables', 'Meat', 'Seafood', 'Dairy', 'Refrigerated/Frozen Goods', 'Bakery', 'Beverages', 'Condiments', 'Household', 'Noodles', 'Packaged Foods', 'Snacks', 'Toiletries', 'Dry Goods', 'Pantry']) AS category
         UNION
         SELECT DISTINCT category FROM items WHERE category IS NOT NULL AND category <> ''
     ) c
    ON CONFLICT (user_id, category) DO UPDATE
                                           SET default_store_id = CASE WHEN EXCLUDED.category IN ('Fruits', 'Vegetables', 'Meat', 'Seafood', 'Dairy', 'Refrigerated/Frozen Goods') THEN v_pasig ELSE v_puregold END;

INSERT INTO data_seed_log (seed_key) VALUES (v_seed_key);
END LOOP;
END
$freshgoodscategorydefaults$;
@@

-- ════════════════════════════════════════════════════════════════════
-- Restores a sensible "Default to Ingredient" toggle per category (the
-- OTHER half of category defaults, alongside the store fix above): a
-- product created in one of these categories starts with its "Ingredient"
-- toggle already on, since that's virtually always true for fresh
-- produce, meat, seafood, dairy, and pantry/cooking staples. Everything
-- else (Bakery, Beverages, Household, Noodles is the exception - kept
-- true since noodles are a cooking ingredient too, Packaged Foods, Snacks,
-- Toiletries) defaults to off, matching how those are typically bought
-- ready-to-eat/use rather than cooked into a recipe.
--
-- Same reasoning and same "definitively set, once" approach as the fresh
-- goods store migration above: this OVERWRITES the current value for
-- these categories (since it was reset to false for everyone during the
-- earlier per-user migration), but runs ONCE per user, so anyone who
-- changes a category's toggle afterward in the Pricing tab keeps their
-- own choice - a later deploy never touches it again.
-- ════════════════════════════════════════════════════════════════════
DO $ingredientdefaultcategorydefaults$
DECLARE
v_user     RECORD;
    v_seed_key TEXT;
BEGIN
CREATE TABLE IF NOT EXISTS data_seed_log (
                                             seed_key   VARCHAR(100) PRIMARY KEY,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

FOR v_user IN SELECT id FROM users LOOP
    v_seed_key := 'ingredient-default-category-defaults-v1:' || v_user.id;
IF EXISTS (SELECT 1 FROM data_seed_log WHERE seed_key = v_seed_key) THEN
            CONTINUE;
END IF;

INSERT INTO user_category_defaults (user_id, category, default_store_id, default_is_ingredient)
SELECT v_user.id, c.category, NULL, (c.category IN ('Fruits', 'Vegetables', 'Meat', 'Seafood', 'Dairy', 'Refrigerated/Frozen Goods', 'Dry Goods', 'Pantry', 'Condiments', 'Noodles'))
FROM (
         SELECT DISTINCT category FROM items WHERE category IS NOT NULL AND category <> ''
     ) c
    ON CONFLICT (user_id, category) DO UPDATE
                                           SET default_is_ingredient = (EXCLUDED.category IN ('Fruits', 'Vegetables', 'Meat', 'Seafood', 'Dairy', 'Refrigerated/Frozen Goods', 'Dry Goods', 'Pantry', 'Condiments', 'Noodles'));

INSERT INTO data_seed_log (seed_key) VALUES (v_seed_key);
END LOOP;
END
$ingredientdefaultcategorydefaults$;
@@