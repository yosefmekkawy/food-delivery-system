-- SQL Script to create the database schema for the Food Delivery System
-- Matches the JPA Entities and the data.md schema

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    user_first_name VARCHAR(255) NOT NULL,
    user_last_name VARCHAR(255) NOT NULL,
    user_dob DATE,
    user_phone VARCHAR(20),
    user_email VARCHAR(255) NOT NULL UNIQUE,
    user_password VARCHAR(255) NOT NULL,
    user_gender VARCHAR(10),
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_enabled BOOLEAN DEFAULT TRUE
);

-- 2. Customers Table
CREATE TABLE IF NOT EXISTS customers (
    customer_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(user_id) UNIQUE
);

-- 3. Menu Items Table
CREATE TABLE IF NOT EXISTS menu_items (
    menu_item_id SERIAL PRIMARY KEY,
    menu_item_name VARCHAR(255) NOT NULL,
    menu_item_description TEXT,
    menu_item_price DECIMAL(10, 2) NOT NULL
);

-- 4. Carts Table
CREATE TABLE IF NOT EXISTS carts (
    cart_id SERIAL PRIMARY KEY,
    cart_customer_id INTEGER NOT NULL REFERENCES customers(customer_id),
    current_rest_id INTEGER,
    notes TEXT,
    is_locked BOOLEAN DEFAULT FALSE
);

-- 5. Cart Items Table (Composite PK: cart_id + menu_item_id)
CREATE TABLE IF NOT EXISTS cart_items (
    cart_id INTEGER NOT NULL REFERENCES carts(cart_id) ON DELETE CASCADE,
    menu_item_id INTEGER NOT NULL REFERENCES menu_items(menu_item_id),
    quantity INTEGER NOT NULL,
    note TEXT,
    PRIMARY KEY (cart_id, menu_item_id)
);
