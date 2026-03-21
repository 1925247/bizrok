-- Bizrok Device Buyback Platform Database Schema
-- SQLite Database Schema for Config-Driven Architecture

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255),
    phone VARCHAR(20),
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT 1,
    kyc_verified BOOLEAN DEFAULT 0,
    kyc_name VARCHAR(255),
    kyc_address TEXT
);

-- Email OTP Table
CREATE TABLE IF NOT EXISTS email_otp (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email VARCHAR(255) NOT NULL,
    otp VARCHAR(10) NOT NULL,
    expiry_time TIMESTAMP NOT NULL,
    is_verified BOOLEAN DEFAULT 0,
    attempts INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email_otp_email (email),
    INDEX idx_email_otp_expiry (expiry_time)
);

-- Categories Table
CREATE TABLE IF NOT EXISTS categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    is_active BOOLEAN DEFAULT 1,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Brands Table
CREATE TABLE IF NOT EXISTS brands (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    category_id INTEGER,
    image_url VARCHAR(500),
    is_active BOOLEAN DEFAULT 1,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Models Table
CREATE TABLE IF NOT EXISTS models (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(200) UNIQUE NOT NULL,
    brand_id INTEGER,
    category_id INTEGER,
    base_price DECIMAL(10,2) NOT NULL,
    variant_info TEXT, -- JSON field for variants like 64GB, 128GB, etc.
    image_url VARCHAR(500),
    is_active BOOLEAN DEFAULT 1,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (brand_id) REFERENCES brands(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Groups Table (Question Groups for Pricing Logic)
CREATE TABLE IF NOT EXISTS groups (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    logic_type VARCHAR(20) NOT NULL DEFAULT 'MAX', -- MAX or SUM
    is_active BOOLEAN DEFAULT 1,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- SubGroups Table (Optional sub-grouping within groups)
CREATE TABLE IF NOT EXISTS sub_groups (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    group_id INTEGER,
    is_active BOOLEAN DEFAULT 1,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES groups(id)
);

-- Questions Table
CREATE TABLE IF NOT EXISTS questions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    text VARCHAR(500) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    group_id INTEGER,
    sub_group_id INTEGER,
    question_type VARCHAR(50) NOT NULL DEFAULT 'radio', -- radio, checkbox, image
    is_required BOOLEAN DEFAULT 1,
    is_active BOOLEAN DEFAULT 1,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES groups(id),
    FOREIGN KEY (sub_group_id) REFERENCES sub_groups(id)
);

-- Options Table (Question Options with Deductions)
CREATE TABLE IF NOT EXISTS options (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    question_id INTEGER NOT NULL,
    text VARCHAR(300) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    deduction_value DECIMAL(10,2) DEFAULT 0,
    deduction_type VARCHAR(20) NOT NULL DEFAULT 'flat', -- flat or percent
    image_url VARCHAR(500),
    is_active BOOLEAN DEFAULT 1,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES questions(id)
);

-- Settings Table (Config-Driven System Settings)
CREATE TABLE IF NOT EXISTS settings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    key VARCHAR(100) UNIQUE NOT NULL,
    value TEXT NOT NULL,
    data_type VARCHAR(50) DEFAULT 'string', -- string, number, boolean
    description TEXT,
    is_active BOOLEAN DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Orders Table
CREATE TABLE IF NOT EXISTS orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    user_id INTEGER,
    model_id INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED', -- CREATED, ASSIGNED, IN_PROGRESS, QC_DONE, COMPLETED, REJECTED
    assigned_to INTEGER, -- Partner or Field Executive ID
    pickup_address TEXT,
    pickup_pincode VARCHAR(10),
    pickup_date DATE,
    pickup_time VARCHAR(50),
    bank_account_number VARCHAR(50),
    bank_ifsc VARCHAR(20),
    bank_account_name VARCHAR(255),
    final_price DECIMAL(10,2),
    base_price DECIMAL(10,2),
    total_deductions DECIMAL(10,2),
    kyc_verified BOOLEAN DEFAULT 0,
    face_match_verified BOOLEAN DEFAULT 0,
    bank_details_verified BOOLEAN DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (model_id) REFERENCES models(id),
    FOREIGN KEY (assigned_to) REFERENCES users(id)
);

-- Order Answers Table (User Responses to Questions)
CREATE TABLE IF NOT EXISTS order_answers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    question_id INTEGER NOT NULL,
    option_id INTEGER,
    answer_text TEXT,
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (question_id) REFERENCES questions(id),
    FOREIGN KEY (option_id) REFERENCES options(id)
);

-- Price Snapshots Table (Historical Price Calculations)
CREATE TABLE IF NOT EXISTS price_snapshots (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    model_id INTEGER NOT NULL,
    base_price DECIMAL(10,2),
    group_deductions TEXT, -- JSON field storing deductions per group
    total_deductions DECIMAL(10,2),
    final_price DECIMAL(10,2),
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (model_id) REFERENCES models(id)
);

-- Pincodes Table (Serviceable Areas)
CREATE TABLE IF NOT EXISTS pincodes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    pincode VARCHAR(10) UNIQUE NOT NULL,
    city VARCHAR(100),
    state VARCHAR(100),
    is_active BOOLEAN DEFAULT 1,
    partner_id INTEGER, -- Assigned partner for this pincode
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (partner_id) REFERENCES users(id)
);

-- Indexes for Performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_brands_category ON brands(category_id);
CREATE INDEX IF NOT EXISTS idx_models_brand ON models(brand_id);
CREATE INDEX IF NOT EXISTS idx_models_category ON models(category_id);
CREATE INDEX IF NOT EXISTS idx_questions_group ON questions(group_id);
CREATE INDEX IF NOT EXISTS idx_options_question ON options(question_id);
CREATE INDEX IF NOT EXISTS idx_orders_user ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_order_answers_order ON order_answers(order_id);
CREATE INDEX IF NOT EXISTS idx_price_snapshots_order ON price_snapshots(order_id);

-- Triggers for Auto-updates
CREATE TRIGGER IF NOT EXISTS update_users_updated_at 
AFTER UPDATE ON users 
FOR EACH ROW 
BEGIN
    UPDATE users SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

CREATE TRIGGER IF NOT EXISTS update_orders_updated_at 
AFTER UPDATE ON orders 
FOR EACH ROW 
BEGIN
    UPDATE orders SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

CREATE TRIGGER IF NOT EXISTS update_settings_updated_at 
AFTER UPDATE ON settings 
FOR EACH ROW 
BEGIN
    UPDATE settings SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;