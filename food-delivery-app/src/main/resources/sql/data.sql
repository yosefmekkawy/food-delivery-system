-- SQL Script to populate the Food Delivery System with dummy data

-- 1. Insert a User
INSERT INTO users (user_first_name, user_last_name, user_email, user_password, user_phone, user_gender)
VALUES ('Mohamed', 'Alhoshy', 'mohamed@example.com', 'hashed_password_123', '0123456789', 'Male')
ON CONFLICT (user_email) DO NOTHING;

-- 2. Insert a Customer for the above user
-- Assuming the user ID is 1 (if it's the first one)
INSERT INTO customers (user_id)
SELECT user_id FROM users WHERE user_email = 'mohamed@example.com'
ON CONFLICT DO NOTHING;

-- 3. Insert some Menu Items
INSERT INTO menu_items (menu_item_name, menu_item_description, menu_item_price)
VALUES 
('Margherita Pizza', 'Classic tomato and mozzarella pizza', 12.50),
('Cheese Burger', 'Beef burger with extra cheddar', 8.99),
('Coca Cola', '500ml cold beverage', 2.00)
ON CONFLICT DO NOTHING;

-- 4. Insert a Cart for the Customer (Mohamed)
INSERT INTO carts (cart_customer_id, notes)
SELECT customer_id, 'Deliver to the main gate' FROM customers 
JOIN users ON customers.user_id = users.user_id 
WHERE users.user_email = 'mohamed@example.com'
ON CONFLICT DO NOTHING;

-- 5. Add Items to the Cart
-- Assuming Cart ID 1 and Menu Item IDs 1 and 2
INSERT INTO cart_items (cart_id, menu_item_id, quantity, note)
SELECT c.cart_id, mi.menu_item_id, 2, 'Extra cheese please'
FROM carts c, menu_items mi
WHERE c.cart_id = 1 AND mi.menu_item_name = 'Margherita Pizza'
ON CONFLICT DO NOTHING;

INSERT INTO cart_items (cart_id, menu_item_id, quantity)
SELECT c.cart_id, mi.menu_item_id, 1
FROM carts c, menu_items mi
WHERE c.cart_id = 1 AND mi.menu_item_name = 'Cheese Burger'
ON CONFLICT DO NOTHING;
