-- SQL Script to populate the Food Delivery System with dummy data
-- Ensures all tables have data for testing and learning purposes

-- 1. Insert Users
INSERT INTO users (user_first_name, user_last_name, user_email, user_password, user_phone, user_gender)
VALUES 
('Mohamed', 'Alhoshy', 'mohamed@example.com', 'hashed_password_123', '0123456789', 'Male'),
('John', 'Doe', 'john.doe@example.com', 'hashed_password_456', '0987654321', 'Male')
ON CONFLICT (user_email) DO NOTHING;

-- 2. Insert Customers
INSERT INTO customers (user_id)
SELECT user_id FROM users WHERE user_email IN ('mohamed@example.com', 'john.doe@example.com')
ON CONFLICT DO NOTHING;

-- 3. Insert Menu Items
INSERT INTO menu_items (menu_item_name, menu_item_description, menu_item_price)
VALUES 
('Margherita Pizza', 'Classic tomato and mozzarella pizza', 12.50),
('Cheese Burger', 'Beef burger with extra cheddar', 8.99),
('Coca Cola', '500ml cold beverage', 2.00),
('Fries', 'Crispy golden fries', 3.50)
ON CONFLICT DO NOTHING;

-- 4. Insert Carts
INSERT INTO carts (cart_customer_id, current_rest_id, notes)
SELECT customer_id, 1, 'Deliver to the main gate'
FROM customers
WHERE customer_id IN (
    SELECT customer_id FROM customers
)
ON CONFLICT DO NOTHING;

-- 5. Insert Cart Items
INSERT INTO cart_items (cart_id, menu_item_id, quantity, note)
SELECT c.cart_id, mi.menu_item_id, 2, 'Extra cheese please'
FROM carts c
CROSS JOIN menu_items mi
WHERE mi.menu_item_name = 'Margherita Pizza'
ON CONFLICT DO NOTHING;

INSERT INTO cart_items (cart_id, menu_item_id, quantity)
SELECT c.cart_id, mi.menu_item_id, 1
FROM carts c
CROSS JOIN menu_items mi
WHERE mi.menu_item_name = 'Cheese Burger'
ON CONFLICT DO NOTHING;

-- 6. Insert Orders
INSERT INTO orders (order_customer_id, order_subtotal, order_fee, order_total, order_note, order_status, order_date)
SELECT c.customer_id, 33.99, 0.00, 33.99, 'Test order for Mohamed', 'PLACED', NOW()
FROM customers c
JOIN users u ON c.user_id = u.user_id
WHERE u.user_email = 'mohamed@example.com'
  AND NOT EXISTS (
    SELECT 1 FROM orders o WHERE o.order_customer_id = c.customer_id
  );

-- 7. Insert Order Items
INSERT INTO order_items (order_id, menu_item_id, unit_price, quantity, subtotal, note)
SELECT o.order_id, mi.menu_item_id, mi.menu_item_price, ci.quantity, (mi.menu_item_price * ci.quantity), ci.note
FROM orders o
JOIN customers c ON o.order_customer_id = c.customer_id
JOIN users u ON c.user_id = u.user_id
JOIN cart_items ci ON ci.cart_id = (
    SELECT cart_id FROM carts WHERE cart_customer_id = c.customer_id LIMIT 1
  )
JOIN menu_items mi ON mi.menu_item_id = ci.menu_item_id
WHERE u.user_email = 'mohamed@example.com'
  AND NOT EXISTS (
    SELECT 1 FROM order_items oi WHERE oi.order_id = o.order_id AND oi.menu_item_id = mi.menu_item_id
  );

-- 8. Insert Payment Integration Types
INSERT INTO payment_integration_type (payment_integration_type_name)
VALUES ('CARD'), ('CASH')
ON CONFLICT DO NOTHING;

-- 9. Insert Payment Type Configurations
INSERT INTO payment_type_config (payment_integration_type, config_details)
VALUES 
('CARD', '{"provider":"simulated","mode":"sync"}'),
('CASH', '{"provider":"simulated","mode":"sync"}')
ON CONFLICT DO NOTHING;

-- 10. Insert Transaction Statuses
INSERT INTO transaction_status (status)
VALUES ('COMPLETED'), ('FAILED'), ('PENDING')
ON CONFLICT DO NOTHING;

