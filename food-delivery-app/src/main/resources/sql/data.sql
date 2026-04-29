-- SQL Script to populate the Food Delivery System with dummy data
-- Keeps one active customer cart with two items so cart update/remove/checkout can be exercised immediately.

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
INSERT INTO carts (cart_customer_id, current_rest_id, notes)
SELECT customer_id, 1, 'Deliver to the main gate'
FROM customers
JOIN users ON customers.user_id = users.user_id
WHERE users.user_email = 'mohamed@example.com'
  AND NOT EXISTS (
	  SELECT 1
	  FROM carts existing_cart
	  WHERE existing_cart.cart_customer_id = customers.customer_id
  );

-- 5. Add Items to the Cart
INSERT INTO cart_items (cart_id, menu_item_id, quantity, note)
SELECT c.cart_id, mi.menu_item_id, 2, 'Extra cheese please'
FROM carts c
CROSS JOIN menu_items mi
JOIN customers customer ON customer.customer_id = c.cart_customer_id
JOIN users u ON u.user_id = customer.user_id
WHERE u.user_email = 'mohamed@example.com'
  AND mi.menu_item_name = 'Margherita Pizza'
ON CONFLICT DO NOTHING;

INSERT INTO cart_items (cart_id, menu_item_id, quantity)
SELECT c.cart_id, mi.menu_item_id, 1
FROM carts c
CROSS JOIN menu_items mi
JOIN customers customer ON customer.customer_id = c.cart_customer_id
JOIN users u ON u.user_id = customer.user_id
WHERE u.user_email = 'mohamed@example.com'
  AND mi.menu_item_name = 'Cheese Burger'
ON CONFLICT DO NOTHING;

INSERT INTO payment_integration_type (payment_integration_type_name)
VALUES ('CARD'), ('CASH')
ON CONFLICT DO NOTHING;

INSERT INTO payment_type_config (payment_integration_type, config_details)
SELECT 'CARD', '{"provider":"simulated","mode":"sync"}'
WHERE NOT EXISTS (
    SELECT 1
    FROM payment_type_config
    WHERE payment_integration_type = 'CARD'
);

INSERT INTO payment_type_config (payment_integration_type, config_details)
SELECT 'CASH', '{"provider":"simulated","mode":"sync"}'
WHERE NOT EXISTS (
    SELECT 1
    FROM payment_type_config
    WHERE payment_integration_type = 'CASH'
);

INSERT INTO transaction_status (status)
VALUES ('COMPLETED'), ('FAILED'), ('PENDING')
ON CONFLICT DO NOTHING;

