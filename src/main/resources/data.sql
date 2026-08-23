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