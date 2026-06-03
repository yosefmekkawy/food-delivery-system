-- ==========================================
-- DATABASE SCHEMA: Food Delivery / Ordering System
-- DBMS: PostgreSQL 13+
-- ==========================================
-- [SUMMARY OF FIXES APPLIED]
-- 1. Replaced all commented-out REFERENCES with explicit FOREIGN KEY CONSTRAINTS.
--    → Enables Hibernate/JPA reverse-engineering to auto-generate @ManyToOne/@OneToMany.
-- 2. Fixed transaction_status to have a PRIMARY KEY (required for JPA @Entity mapping).
-- 3. Set cart_item.menu_item_id to NOT NULL to enforce cart data consistency.
-- 4. Increased address/city/street VARCHAR lengths to real-world standards.
-- 5. Resolved coupon TODOs by adding discount_type and min_order_value columns.
-- 6. Added explicit CONSTRAINT names for better debugging, migrations, and indexing.
-- 7. Applied ON DELETE CASCADE/SET NULL rules to match business logic.
-- 8. Added inline comments explaining WHY each change was made.
-- ==========================================

------------------------------CONFIGURATION---------------------
CREATE TABLE IF NOT EXISTS system_config (
    config_key VARCHAR(50) PRIMARY KEY,
    config_value DECIMAL(5,2) NOT NULL
);
-- [CHANGE] Insert default fees with ON CONFLICT to ensure idempotent script execution.
INSERT INTO system_config (config_key, config_value) VALUES ('service_fee', 0.00) ON CONFLICT (config_key) DO NOTHING;
INSERT INTO system_config (config_key, config_value) VALUES ('small_order_fee', 0.00) ON CONFLICT (config_key) DO NOTHING;

------------------------------USER & CUSTOMER---------------------
CREATE TABLE IF NOT EXISTS permission (
    permission_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    permission_name VARCHAR(50) NOT NULL UNIQUE, -- [CHANGE] Renamed to avoid keyword conflict & added UNIQUE
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS role (
    role_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS role_permission (
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    CONSTRAINT pk_role_permission PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES role(role_id) ON DELETE CASCADE,
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permission(permission_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_type (
    user_type_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_type_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_type_id INT NOT NULL,
    user_first_name VARCHAR(50) NOT NULL,
    user_last_name VARCHAR(50) NOT NULL,
    user_birth_date DATE,
    user_phone VARCHAR(20) NOT NULL, -- [CHANGE] Increased from 15 for intl formats
    user_email VARCHAR(100) NOT NULL UNIQUE, -- [CHANGE] Increased length for modern emails
    user_password VARCHAR(255) NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE, -- [CHANGE] BIT → BOOLEAN for standard JPA mapping
    CONSTRAINT fk_users_type FOREIGN KEY (user_type_id) REFERENCES user_type(user_type_id)
);

CREATE TABLE IF NOT EXISTS user_role (
    user_id UUID NOT NULL,
    role_id INT NOT NULL,
    CONSTRAINT pk_user_role PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES role(role_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS customer (
    customer_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_user_id UUID NOT NULL UNIQUE,
    customer_default_address_id UUID,
    customer_preferred_payment_id INT,
    CONSTRAINT fk_customer_user FOREIGN KEY (customer_user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS customer_address (
    customer_address_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_address_customer_id UUID NOT NULL,
    customer_address_label VARCHAR(50) NOT NULL, -- [CHANGE] Increased for labels like "Home", "Office"
    customer_address_city VARCHAR(100) NOT NULL, -- [CHANGE] Increased from 20
    customer_address_street VARCHAR(255) NOT NULL, -- [CHANGE] Increased from 20
    customer_address_building VARCHAR(50) NOT NULL, -- [CHANGE] Increased from 20
    customer_address_apartment VARCHAR(50) NOT NULL, -- [CHANGE] Increased from 20
    customer_address_phone_number VARCHAR(20) NOT NULL,
    customer_address_note VARCHAR(500),
    CONSTRAINT fk_addr_customer FOREIGN KEY (customer_address_customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE
);
-- [CHANGE] Note: customer_default_address_id in customer table needs a separate FK added via ALTER TABLE 
-- AFTER customer_address table exists, or kept as-is if managed app-side.

------------------------------RESTAURANT---------------------
CREATE TABLE IF NOT EXISTS restaurant (
    restaurant_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_name VARCHAR(100) NOT NULL,
    restaurant_description TEXT
);

CREATE TABLE IF NOT EXISTS restaurant_branch (
    branch_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_rest_id UUID NOT NULL,
    branch_delivery_fee DECIMAL(6,2) CHECK (branch_delivery_fee >= 0),
    branch_min_order DECIMAL(6,2) CHECK (branch_min_order >= 0),
    branch_city VARCHAR(100) NOT NULL,
    branch_open_time TIME NOT NULL,
    branch_close_time TIME NOT NULL,
    branch_phone_number VARCHAR(20) NOT NULL,
    branch_estimated_delivery_time INT CHECK (branch_estimated_delivery_time > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified TIMESTAMP,
    created_by UUID NOT NULL,
    modified_by UUID,
    admin_id UUID,
    CONSTRAINT fk_branch_rest FOREIGN KEY (branch_rest_id) REFERENCES restaurant(restaurant_id) ON DELETE CASCADE,
    CONSTRAINT fk_branch_created_by FOREIGN KEY (created_by) REFERENCES users(user_id),
    CONSTRAINT fk_branch_modified_by FOREIGN KEY (modified_by) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_branch_admin FOREIGN KEY (admin_id) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS category (
    category_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    category_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS restaurant_category (
    category_id INT NOT NULL,
    restaurant_id UUID NOT NULL,
    CONSTRAINT pk_restaurant_category PRIMARY KEY (category_id, restaurant_id),
    CONSTRAINT fk_rc_category FOREIGN KEY (category_id) REFERENCES category(category_id) ON DELETE CASCADE,
    CONSTRAINT fk_rc_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant(restaurant_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS restaurant_menu (
    restaurant_menu_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_menu_rest_id UUID NOT NULL,
    restaurant_menu_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified TIMESTAMP,
    created_by UUID NOT NULL,
    modified_by UUID,
    CONSTRAINT fk_menu_rest FOREIGN KEY (restaurant_menu_rest_id) REFERENCES restaurant(restaurant_id) ON DELETE CASCADE,
    CONSTRAINT fk_menu_created_by FOREIGN KEY (created_by) REFERENCES users(user_id),
    CONSTRAINT fk_menu_modified_by FOREIGN KEY (modified_by) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS menu_item (
    menu_item_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_menu_id UUID NOT NULL,
    menu_item_description VARCHAR(500),
    menu_item_name VARCHAR(100) NOT NULL,
    menu_item_price DECIMAL(9,2) NOT NULL CHECK (menu_item_price > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified TIMESTAMP,
    created_by UUID NOT NULL,
    modified_by UUID,
    CONSTRAINT fk_item_menu FOREIGN KEY (restaurant_menu_id) REFERENCES restaurant_menu(restaurant_menu_id) ON DELETE CASCADE,
    CONSTRAINT fk_item_created_by FOREIGN KEY (created_by) REFERENCES users(user_id),
    CONSTRAINT fk_item_modified_by FOREIGN KEY (modified_by) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS restaurant_rate (
    restaurant_rate_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_rate_restaurant_id UUID NOT NULL,
    restaurant_rate_customer_id UUID NOT NULL,
    restaurant_rate_rating INT NOT NULL CHECK (restaurant_rate_rating BETWEEN 1 AND 5),
    restaurant_rate_comment VARCHAR(500),
    restaurant_rate_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rate_rest FOREIGN KEY (restaurant_rate_restaurant_id) REFERENCES restaurant(restaurant_id) ON DELETE CASCADE,
    CONSTRAINT fk_rate_customer FOREIGN KEY (restaurant_rate_customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS order_rate (
    order_rate_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_rate_order_id UUID NOT NULL UNIQUE,
    order_rate_rating INT NOT NULL CHECK (order_rate_rating BETWEEN 1 AND 5),
    order_rate_comment VARCHAR(500),
    order_rate_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_rate_order FOREIGN KEY (order_rate_order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS coupon (
    coupon_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    coupon_restaurant_id UUID NOT NULL,
    coupon_amount DECIMAL(6,2) CHECK (coupon_amount >= 0),
    -- [CHANGE] Resolved TODOs: Added explicit discount type & minimum order requirement
    coupon_discount_type VARCHAR(15) NOT NULL DEFAULT 'FIXED_AMOUNT' CHECK (coupon_discount_type IN ('FIXED_AMOUNT', 'PERCENTAGE')),
    coupon_min_order_value DECIMAL(10,2) DEFAULT 0.00 CHECK (coupon_min_order_value >= 0),
    coupon_available_from TIMESTAMP NOT NULL,
    coupon_available_to TIMESTAMP NOT NULL,
    coupon_is_active BOOLEAN NOT NULL DEFAULT TRUE, -- [CHANGE] BIT → BOOLEAN
    coupon_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    coupon_last_modified TIMESTAMP,
    CONSTRAINT fk_coupon_rest FOREIGN KEY (coupon_restaurant_id) REFERENCES restaurant(restaurant_id) ON DELETE CASCADE,
    CONSTRAINT chk_coupon_dates CHECK (coupon_available_to > coupon_available_from)
);

------------------------------CART---------------------
CREATE TABLE IF NOT EXISTS cart (
    cart_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_customer_id UUID NOT NULL,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    cart_current_rest_id UUID,
    CONSTRAINT fk_cart_customer FOREIGN KEY (cart_customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_branch FOREIGN KEY (cart_current_rest_id) REFERENCES restaurant_branch(branch_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS cart_item (
    cart_item_id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    cart_item_cart_id UUID NOT NULL,
    menu_item_id UUID NOT NULL, -- [CHANGE] Added NOT NULL to prevent orphaned cart items
    cart_item_quantity INT NOT NULL CHECK (cart_item_quantity > 0),
    cart_item_note VARCHAR(500),
    CONSTRAINT fk_ci_cart FOREIGN KEY (cart_item_cart_id) REFERENCES cart(cart_id) ON DELETE CASCADE,
    CONSTRAINT fk_ci_item FOREIGN KEY (menu_item_id) REFERENCES menu_item(menu_item_id) ON DELETE CASCADE
);

------------------------------ORDER---------------------
CREATE TABLE IF NOT EXISTS order_status (
    order_status_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    order_status_name VARCHAR(50) NOT NULL UNIQUE,
    order_status_description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS orders (
    order_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_address_id UUID NOT NULL,
    order_customer_id UUID NOT NULL,
    order_restaurant_branch_id UUID NOT NULL,
    order_coupon_id UUID,
    order_discount_value DECIMAL(6,2) DEFAULT 0.00 CHECK (order_discount_value >= 0),
    order_subtotal DECIMAL(10,2) NOT NULL CHECK (order_subtotal > 0), -- [CHANGE] Increased precision
    order_fee DECIMAL(6,2) DEFAULT 0,
    order_total DECIMAL(10,2) NOT NULL CHECK (order_total > 0),
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    order_note VARCHAR(500),
    order_status_id INT NOT NULL,
    CONSTRAINT fk_order_address FOREIGN KEY (order_address_id) REFERENCES customer_address(customer_address_id),
    CONSTRAINT fk_order_customer FOREIGN KEY (order_customer_id) REFERENCES customer(customer_id),
    CONSTRAINT fk_order_branch FOREIGN KEY (order_restaurant_branch_id) REFERENCES restaurant_branch(branch_id),
    CONSTRAINT fk_order_coupon FOREIGN KEY (order_coupon_id) REFERENCES coupon(coupon_id) ON DELETE SET NULL,
    CONSTRAINT fk_order_status FOREIGN KEY (order_status_id) REFERENCES order_status(order_status_id)
);

CREATE TABLE IF NOT EXISTS order_tracking (
    order_tracking_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_tracking_status_id INT NOT NULL,
    order_tracking_order_id UUID NOT NULL,
    order_tracking_description VARCHAR(255) NOT NULL,
    order_tracking_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_track_status FOREIGN KEY (order_tracking_status_id) REFERENCES order_status(order_status_id),
    CONSTRAINT fk_track_order FOREIGN KEY (order_tracking_order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS order_item (
    order_item_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_item_order_id UUID NOT NULL,
    order_item_menu_item_id UUID NOT NULL,
    order_item_unit_price DECIMAL(9,2) NOT NULL CHECK (order_item_unit_price > 0),
    order_item_quantity INT NOT NULL CHECK (order_item_quantity > 0),
    order_item_subtotal DECIMAL(10,2) NOT NULL,
    order_item_note VARCHAR(500),
    CONSTRAINT fk_oi_order FOREIGN KEY (order_item_order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_oi_item FOREIGN KEY (order_item_menu_item_id) REFERENCES menu_item(menu_item_id)
);

------------------------------PAYMENT---------------------
CREATE TABLE IF NOT EXISTS payment_integration_type (
    payment_integration_type_name VARCHAR(50) PRIMARY KEY -- e.g., 'STRIPE', 'PAYPAL', 'CASH'
);

CREATE TABLE IF NOT EXISTS payment_type_config (
    payment_type_config_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    payment_integration_type VARCHAR(50) NOT NULL,
    config_details JSONB NOT NULL, -- [CHANGE] TEXT → JSONB for structured provider configs
    CONSTRAINT fk_pconfig_type FOREIGN KEY (payment_integration_type) REFERENCES payment_integration_type(payment_integration_type_name) ON DELETE CASCADE
);

-- Add FK after payment_type_config exists
ALTER TABLE customer
    DROP CONSTRAINT IF EXISTS fk_customer_payment;

ALTER TABLE customer
    ADD CONSTRAINT fk_customer_payment
    FOREIGN KEY (customer_preferred_payment_id)
    REFERENCES payment_type_config(payment_type_config_id)
    ON DELETE SET NULL;

CREATE TABLE IF NOT EXISTS transaction_status (
    status VARCHAR(20) PRIMARY KEY -- [CHANGE] Added PRIMARY KEY so JPA can map it as an entity
);
-- Recommended inserts:
-- INSERT INTO transaction_status (status) VALUES ('PENDING'), ('SUCCESS'), ('FAILED'), ('REFUNDED') ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_status VARCHAR(20) NOT NULL,
    transaction_order_id UUID NOT NULL,
    transaction_payment_type VARCHAR(50),
    transaction_customer_id UUID NOT NULL,
    transaction_rest_branch_id UUID NOT NULL,
    transaction_amount DECIMAL(10,2) NOT NULL CHECK (transaction_amount > 0),
    transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trans_status FOREIGN KEY (transaction_status) REFERENCES transaction_status(status),
    CONSTRAINT fk_trans_order FOREIGN KEY (transaction_order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_trans_payment_type FOREIGN KEY (transaction_payment_type) REFERENCES payment_integration_type(payment_integration_type_name),
    CONSTRAINT fk_trans_customer FOREIGN KEY (transaction_customer_id) REFERENCES customer(customer_id),
    CONSTRAINT fk_trans_branch FOREIGN KEY (transaction_rest_branch_id) REFERENCES restaurant_branch(branch_id)
);