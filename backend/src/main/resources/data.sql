-- Bizrok Device Buyback Platform Seed Data
-- Initial data for MVP setup

-- Default Settings (Config-Driven System)
INSERT OR IGNORE INTO settings (key, value, data_type, description) VALUES
('MIN_PRICE_PERCENT', '20', 'number', 'Minimum price percentage of base price'),
('MAX_DEDUCTION_PERCENT', '80', 'number', 'Maximum deduction percentage from base price'),
('ENABLE_KYC', 'true', 'boolean', 'Enable KYC verification'),
('ENABLE_EMAIL_OTP', 'true', 'boolean', 'Enable email OTP authentication'),
('ENABLE_FACE_MATCH', 'true', 'boolean', 'Enable face matching with ID'),
('ENABLE_BANK_CHECK', 'true', 'boolean', 'Enable bank details verification'),
('OTP_EXPIRY_MINUTES', '5', 'number', 'OTP expiry time in minutes'),
('MAX_OTP_ATTEMPTS', '3', 'number', 'Maximum OTP verification attempts'),
('DEFAULT_ORDER_STATUS', 'CREATED', 'string', 'Default order status after creation'),
('ENABLE_PINCODE_VALIDATION', 'true', 'boolean', 'Enable pincode service validation');

-- Categories
INSERT OR IGNORE INTO categories (name, slug, description, sort_order) VALUES
('Mobile Phones', 'mobile-phones', 'Smartphones and mobile devices', 1),
('Laptops', 'laptops', 'Laptops and notebooks', 2),
('Tablets', 'tablets', 'Tablet devices', 3),
('Smartwatches', 'smartwatches', 'Wearable smart devices', 4);

-- Brands for Mobile Phones
INSERT OR IGNORE INTO brands (name, slug, category_id, sort_order) VALUES
('Apple', 'apple', 1, 1),
('Samsung', 'samsung', 1, 2),
('Google', 'google', 1, 3),
('OnePlus', 'oneplus', 1, 4),
('Xiaomi', 'xiaomi', 1, 5);

-- Brands for Laptops
INSERT OR IGNORE INTO brands (name, slug, category_id, sort_order) VALUES
('Apple', 'apple', 2, 1),
('Dell', 'dell', 2, 2),
('HP', 'hp', 2, 3),
('Lenovo', 'lenovo', 2, 4),
('Asus', 'asus', 2, 5);

-- Sample Models for Mobile Phones
INSERT OR IGNORE INTO models (name, slug, brand_id, category_id, base_price, variant_info, sort_order) VALUES
('iPhone 14', 'iphone-14', 1, 1, 45000.00, '{"variants": ["128GB", "256GB", "512GB"]}', 1),
('iPhone 13', 'iphone-13', 1, 1, 35000.00, '{"variants": ["128GB", "256GB", "512GB"]}', 2),
('Galaxy S23', 'galaxy-s23', 2, 1, 40000.00, '{"variants": ["128GB", "256GB", "512GB"]}', 1),
('Pixel 7', 'pixel-7', 3, 1, 30000.00, '{"variants": ["128GB", "256GB"]}', 1);

-- Sample Models for Laptops
INSERT OR IGNORE INTO models (name, slug, brand_id, category_id, base_price, variant_info, sort_order) VALUES
('MacBook Air M2', 'macbook-air-m2', 1, 2, 80000.00, '{"variants": ["8GB/256GB", "16GB/512GB"]}', 1),
('XPS 13', 'xps-13', 2, 2, 75000.00, '{"variants": ["8GB/256GB", "16GB/512GB"]}', 1),
('Spectre x360', 'spectre-x360', 3, 2, 70000.00, '{"variants": ["8GB/256GB", "16GB/512GB"]}', 1);

-- Question Groups
INSERT OR IGNORE INTO groups (name, slug, description, logic_type, sort_order) VALUES
('Display', 'display', 'Display condition and issues', 'MAX', 1),
('Body', 'body', 'Physical condition and body damage', 'MAX', 2),
('Battery', 'battery', 'Battery health and performance', 'SUM', 3),
('Functionality', 'functionality', 'Device functionality and features', 'SUM', 4);

-- SubGroups for Display
INSERT OR IGNORE INTO sub_groups (name, slug, group_id, sort_order) VALUES
('Screen Condition', 'screen-condition', 1, 1),
('Touch Issues', 'touch-issues', 1, 2);

-- SubGroups for Body
INSERT OR IGNORE INTO sub_groups (name, slug, group_id, sort_order) VALUES
('Physical Damage', 'physical-damage', 2, 1),
('Ports and Buttons', 'ports-buttons', 2, 2);

-- Questions for Display Group
INSERT OR IGNORE INTO questions (text, slug, group_id, sub_group_id, question_type, sort_order) VALUES
('Is the display working properly?', 'display-working', 1, 1, 'radio', 1),
('Any visible cracks or scratches on screen?', 'screen-damage', 1, 1, 'radio', 2),
('Touch screen responsive?', 'touch-responsive', 1, 2, 'radio', 1);

-- Questions for Body Group
INSERT OR IGNORE INTO questions (text, slug, group_id, sub_group_id, question_type, sort_order) VALUES
('Any dents or bends on the body?', 'body-damage', 2, 1, 'radio', 1),
('Charging port working?', 'charging-port', 2, 2, 'radio', 1),
('Volume buttons working?', 'volume-buttons', 2, 2, 'radio', 2);

-- Questions for Battery Group
INSERT OR IGNORE INTO questions (text, slug, group_id, question_type, sort_order) VALUES
('Battery health above 80%?', 'battery-health', 3, 'radio', 1),
('Fast charging working?', 'fast-charging', 3, 'radio', 2),
('Battery drains quickly?', 'battery-drain', 3, 'radio', 3);

-- Questions for Functionality Group
INSERT OR IGNORE INTO questions (text, slug, group_id, question_type, sort_order) VALUES
('All cameras working?', 'cameras-working', 4, 'radio', 1),
('Fingerprint sensor working?', 'fingerprint-working', 4, 'radio', 2),
('Face unlock working?', 'face-unlock-working', 4, 'radio', 3);

-- Options for Display Questions
INSERT OR IGNORE INTO options (question_id, text, deduction_value, deduction_type, sort_order) VALUES
-- Display Working
(1, 'Yes, working perfectly', 0.00, 'flat', 1),
(1, 'No, display not working', 5000.00, 'flat', 2),
-- Screen Damage
(2, 'No cracks or scratches', 0.00, 'flat', 1),
(2, 'Minor scratches', 500.00, 'flat', 2),
(2, 'Cracks present', 2000.00, 'flat', 3),
-- Touch Responsive
(3, 'Fully responsive', 0.00, 'flat', 1),
(3, 'Partially responsive', 1000.00, 'flat', 2),
(3, 'Not responsive', 3000.00, 'flat', 3);

-- Options for Body Questions
INSERT OR IGNORE INTO options (question_id, text, deduction_value, deduction_type, sort_order) VALUES
-- Body Damage
(4, 'No damage', 0.00, 'flat', 1),
(4, 'Minor dents', 300.00, 'flat', 2),
(4, 'Major bends/damage', 1500.00, 'flat', 3),
-- Charging Port
(5, 'Working perfectly', 0.00, 'flat', 1),
(5, 'Loose connection', 200.00, 'flat', 2),
(5, 'Not working', 500.00, 'flat', 3),
-- Volume Buttons
(6, 'All working', 0.00, 'flat', 1),
(6, 'Some not working', 100.00, 'flat', 2),
(6, 'None working', 300.00, 'flat', 3);

-- Options for Battery Questions
INSERT OR IGNORE INTO options (question_id, text, deduction_value, deduction_type, sort_order) VALUES
-- Battery Health
(7, 'Above 80%', 0.00, 'flat', 1),
(7, '60-80%', 500.00, 'flat', 2),
(7, 'Below 60%', 1500.00, 'flat', 3),
-- Fast Charging
(8, 'Working', 0.00, 'flat', 1),
(8, 'Not working', 300.00, 'flat', 2),
-- Battery Drain
(9, 'Normal usage', 0.00, 'flat', 1),
(9, 'Drains quickly', 800.00, 'flat', 2);

-- Options for Functionality Questions
INSERT OR IGNORE INTO options (question_id, text, deduction_value, deduction_type, sort_order) VALUES
-- Cameras Working
(10, 'All working', 0.00, 'flat', 1),
(10, 'Some not working', 500.00, 'flat', 2),
(10, 'None working', 1500.00, 'flat', 3),
-- Fingerprint Sensor
(11, 'Working', 0.00, 'flat', 1),
(11, 'Not working', 200.00, 'flat', 2),
-- Face Unlock
(12, 'Working', 0.00, 'flat', 1),
(12, 'Not working', 300.00, 'flat', 2);

-- Sample Pincodes
INSERT OR IGNORE INTO pincodes (pincode, city, state, is_active) VALUES
('110001', 'New Delhi', 'Delhi', 1),
('110002', 'New Delhi', 'Delhi', 1),
('110003', 'New Delhi', 'Delhi', 1),
('400001', 'Mumbai', 'Maharashtra', 1),
('400002', 'Mumbai', 'Maharashtra', 1),
('560001', 'Bangalore', 'Karnataka', 1),
('560002', 'Bangalore', 'Karnataka', 1);

-- Sample Admin User
INSERT OR IGNORE INTO users (email, name, role, is_active) VALUES
('admin@bizrok.in', 'Admin User', 'ADMIN', 1),
('partner@bizrok.in', 'Partner User', 'PARTNER', 1),
('field@bizrok.in', 'Field Executive', 'FIELD_EXECUTIVE', 1);