-- SQL Script to populate the Food Delivery System with dummy data
-- Matches the current schema.sql (PostgreSQL)

-- Overwrite existing data (order matters due to FK constraints)
TRUNCATE TABLE
  transactions,
  order_item,
  orders,
  order_tracking,
  order_status,
  cart_item,
  cart,
  restaurant_rate,
  coupon,
  menu_item,
  restaurant_menu,
  restaurant_category,
  category,
  restaurant_branch,
  restaurant,
  customer_address,
  customer,
  user_role,
  role_permission,
  role,
  permission,
  users,
  user_type,
  payment_type_config,
  payment_integration_type,
  transaction_status,
  system_config
RESTART IDENTITY CASCADE;

-- 1. User types
INSERT INTO user_type (user_type_name)
VALUES ('CUSTOMER')
ON CONFLICT (user_type_name) DO NOTHING;

-- 2. Users
WITH target_type AS (
  SELECT user_type_id FROM user_type WHERE user_type_name = 'CUSTOMER'
),
inserted AS (
  INSERT INTO users (user_type_id, user_first_name, user_last_name, user_birth_date, user_phone, user_email, user_password)
  SELECT user_type_id, 'Mohamed', 'Alhoshy', DATE '1995-05-01', '0123456789', 'mohamed@example.com', 'hashed_password_123'
  FROM target_type
  ON CONFLICT (user_email) DO NOTHING
  RETURNING user_id
)
SELECT user_id FROM inserted
UNION
SELECT user_id FROM users WHERE user_email = 'mohamed@example.com';

WITH target_type AS (
  SELECT user_type_id FROM user_type WHERE user_type_name = 'CUSTOMER'
),
inserted AS (
  INSERT INTO users (user_type_id, user_first_name, user_last_name, user_birth_date, user_phone, user_email, user_password)
  SELECT user_type_id, 'John', 'Doe', DATE '1990-01-15', '0987654321', 'john.doe@example.com', 'hashed_password_456'
  FROM target_type
  ON CONFLICT (user_email) DO NOTHING
  RETURNING user_id
)
SELECT user_id FROM inserted
UNION
SELECT user_id FROM users WHERE user_email = 'john.doe@example.com';

-- 3. Payment types & configs
INSERT INTO payment_integration_type (payment_integration_type_name)
VALUES ('CARD'), ('CASH')
ON CONFLICT (payment_integration_type_name) DO NOTHING;

INSERT INTO payment_type_config (payment_integration_type, config_details)
SELECT 'CARD', '{"provider":"simulated","mode":"sync"}'::jsonb
WHERE NOT EXISTS (SELECT 1 FROM payment_type_config WHERE payment_integration_type = 'CARD');

INSERT INTO payment_type_config (payment_integration_type, config_details)
SELECT 'CASH', '{"provider":"simulated","mode":"sync"}'::jsonb
WHERE NOT EXISTS (SELECT 1 FROM payment_type_config WHERE payment_integration_type = 'CASH');

-- 4. Customer profiles
INSERT INTO customer (customer_user_id)
SELECT u.user_id
FROM users u
WHERE u.user_email IN ('mohamed@example.com', 'john.doe@example.com')
ON CONFLICT (customer_user_id) DO NOTHING;

-- 5. Restaurant
INSERT INTO restaurant (restaurant_name, restaurant_description)
SELECT 'Demo Restaurant', 'Demo restaurant for seed data'
WHERE NOT EXISTS (SELECT 1 FROM restaurant WHERE restaurant_name = 'Demo Restaurant');

-- 6. Restaurant branch
WITH rest AS (
  SELECT restaurant_id FROM restaurant WHERE restaurant_name = 'Demo Restaurant'
),
admin_user AS (
  SELECT user_id FROM users WHERE user_email = 'mohamed@example.com'
)
INSERT INTO restaurant_branch (
  branch_rest_id,
  branch_delivery_fee,
  branch_min_order,
  branch_city,
  branch_open_time,
  branch_close_time,
  branch_phone_number,
  branch_estimated_delivery_time,
  created_by,
  admin_id
)
SELECT rest.restaurant_id, 0.00, 0.00, 'Cairo', TIME '09:00', TIME '23:00', '0123456789', 30, admin_user.user_id, admin_user.user_id
FROM rest, admin_user
WHERE NOT EXISTS (
  SELECT 1 FROM restaurant_branch rb
  WHERE rb.branch_rest_id = rest.restaurant_id AND rb.branch_phone_number = '0123456789'
);

-- 7. Restaurant menu and items
WITH rest AS (
  SELECT restaurant_id FROM restaurant WHERE restaurant_name = 'Demo Restaurant'
),
admin_user AS (
  SELECT user_id FROM users WHERE user_email = 'mohamed@example.com'
),
menu AS (
  INSERT INTO restaurant_menu (restaurant_menu_rest_id, restaurant_menu_name, created_by)
  SELECT rest.restaurant_id, 'Main Menu', admin_user.user_id
  FROM rest, admin_user
  WHERE NOT EXISTS (
    SELECT 1 FROM restaurant_menu rm
    WHERE rm.restaurant_menu_rest_id = rest.restaurant_id AND rm.restaurant_menu_name = 'Main Menu'
  )
  RETURNING restaurant_menu_id
)
SELECT restaurant_menu_id FROM menu
UNION
SELECT restaurant_menu_id FROM restaurant_menu WHERE restaurant_menu_name = 'Main Menu';

WITH menu AS (
  SELECT restaurant_menu_id FROM restaurant_menu WHERE restaurant_menu_name = 'Main Menu'
),
admin_user AS (
  SELECT user_id FROM users WHERE user_email = 'mohamed@example.com'
)
INSERT INTO menu_item (restaurant_menu_id, menu_item_name, menu_item_description, menu_item_price, created_by)
SELECT menu.restaurant_menu_id, 'Margherita Pizza', 'Classic tomato and mozzarella pizza', 12.50, admin_user.user_id
FROM menu, admin_user
WHERE NOT EXISTS (
  SELECT 1 FROM menu_item mi
  WHERE mi.restaurant_menu_id = menu.restaurant_menu_id AND mi.menu_item_name = 'Margherita Pizza'
);

WITH menu AS (
  SELECT restaurant_menu_id FROM restaurant_menu WHERE restaurant_menu_name = 'Main Menu'
),
admin_user AS (
  SELECT user_id FROM users WHERE user_email = 'mohamed@example.com'
)
INSERT INTO menu_item (restaurant_menu_id, menu_item_name, menu_item_description, menu_item_price, created_by)
SELECT menu.restaurant_menu_id, 'Cheese Burger', 'Beef burger with extra cheddar', 8.99, admin_user.user_id
FROM menu, admin_user
WHERE NOT EXISTS (
  SELECT 1 FROM menu_item mi
  WHERE mi.restaurant_menu_id = menu.restaurant_menu_id AND mi.menu_item_name = 'Cheese Burger'
);

-- 8. Customer addresses
WITH cust AS (
  SELECT customer_id FROM customer c
  JOIN users u ON c.customer_user_id = u.user_id
  WHERE u.user_email = 'mohamed@example.com'
)
INSERT INTO customer_address (
  customer_address_customer_id,
  customer_address_label,
  customer_address_city,
  customer_address_street,
  customer_address_building,
  customer_address_apartment,
  customer_address_phone_number,
  customer_address_note
)
SELECT cust.customer_id, 'Home', 'Cairo', 'Main St', 'Building 1', 'Apt 10', '0123456789', 'Leave at the gate'
FROM cust
WHERE NOT EXISTS (
  SELECT 1 FROM customer_address ca
  WHERE ca.customer_address_customer_id = cust.customer_id AND ca.customer_address_label = 'Home'
);

-- 9. Set default address
WITH cust AS (
  SELECT customer_id FROM customer c
  JOIN users u ON c.customer_user_id = u.user_id
  WHERE u.user_email = 'mohamed@example.com'
),
addr AS (
  SELECT customer_address_id FROM customer_address ca
  JOIN cust ON ca.customer_address_customer_id = cust.customer_id
  WHERE ca.customer_address_label = 'Home'
)
UPDATE customer
SET customer_default_address_id = addr.customer_address_id
FROM cust, addr
WHERE customer.customer_id = cust.customer_id
  AND (customer.customer_default_address_id IS NULL OR customer.customer_default_address_id <> addr.customer_address_id);

-- 10. Cart and cart items
WITH cust AS (
  SELECT customer_id FROM customer c
  JOIN users u ON c.customer_user_id = u.user_id
  WHERE u.user_email = 'mohamed@example.com'
),
branch AS (
  SELECT branch_id FROM restaurant_branch rb
  JOIN restaurant r ON rb.branch_rest_id = r.restaurant_id
  WHERE r.restaurant_name = 'Demo Restaurant'
  LIMIT 1
)
INSERT INTO cart (cart_customer_id, cart_current_rest_id)
SELECT cust.customer_id, branch.branch_id
FROM cust, branch
WHERE NOT EXISTS (SELECT 1 FROM cart c WHERE c.cart_customer_id = cust.customer_id);

WITH cart_row AS (
  SELECT c.cart_id FROM cart c
  JOIN customer cust ON c.cart_customer_id = cust.customer_id
  JOIN users u ON cust.customer_user_id = u.user_id
  WHERE u.user_email = 'mohamed@example.com'
),
item AS (
  SELECT menu_item_id FROM menu_item WHERE menu_item_name = 'Margherita Pizza' LIMIT 1
)
INSERT INTO cart_item (cart_item_cart_id, menu_item_id, cart_item_quantity, cart_item_note)
SELECT cart_row.cart_id, item.menu_item_id, 2, 'Extra cheese please'
FROM cart_row, item
WHERE NOT EXISTS (
  SELECT 1 FROM cart_item ci
  WHERE ci.cart_item_cart_id = cart_row.cart_id AND ci.menu_item_id = item.menu_item_id
);

-- 11. Order status
INSERT INTO order_status (order_status_name, order_status_description)
VALUES
  ('PLACED', 'Order placed'),
  ('DELIVERED', 'Order delivered to customer'),
  ('RETURN_REQUESTED', 'Customer requested to return the order'),
  ('RETURNED', 'Order returned and confirmed by the restaurant'),
  ('CANCELLED', 'Order cancelled'),
  ('FAILED', 'Order failed')
ON CONFLICT (order_status_name) DO NOTHING;

-- 12. Orders
WITH cust AS (
  SELECT customer_id, customer_default_address_id FROM customer c
  JOIN users u ON c.customer_user_id = u.user_id
  WHERE u.user_email = 'mohamed@example.com'
),
branch AS (
  SELECT branch_id FROM restaurant_branch rb
  JOIN restaurant r ON rb.branch_rest_id = r.restaurant_id
  WHERE r.restaurant_name = 'Demo Restaurant'
  LIMIT 1
),
status AS (
  SELECT order_status_id FROM order_status WHERE order_status_name = 'PLACED'
)
INSERT INTO orders (
  order_address_id,
  order_customer_id,
  order_restaurant_branch_id,
  order_discount_value,
  order_subtotal,
  order_fee,
  order_total,
  order_note,
  order_status_id
)
SELECT cust.customer_default_address_id, cust.customer_id, branch.branch_id, 0.00, 33.99, 0.00, 33.99, 'Test order for Mohamed', status.order_status_id
FROM cust, branch, status
WHERE NOT EXISTS (
  SELECT 1 FROM orders o WHERE o.order_customer_id = cust.customer_id
);

-- 13. Order items
WITH ord AS (
  SELECT order_id FROM orders o
  JOIN customer c ON o.order_customer_id = c.customer_id
  JOIN users u ON c.customer_user_id = u.user_id
  WHERE u.user_email = 'mohamed@example.com'
  LIMIT 1
),
item AS (
  SELECT menu_item_id, menu_item_price FROM menu_item WHERE menu_item_name = 'Margherita Pizza' LIMIT 1
)
INSERT INTO order_item (
  order_item_order_id,
  order_item_menu_item_id,
  order_item_unit_price,
  order_item_quantity,
  order_item_subtotal,
  order_item_note
)
SELECT ord.order_id, item.menu_item_id, item.menu_item_price, 2, item.menu_item_price * 2, 'Extra cheese please'
FROM ord, item
WHERE NOT EXISTS (
  SELECT 1 FROM order_item oi WHERE oi.order_item_order_id = ord.order_id AND oi.order_item_menu_item_id = item.menu_item_id
);

-- 14. Transaction statuses
INSERT INTO transaction_status (status)
VALUES ('COMPLETED'), ('FAILED'), ('PENDING')
ON CONFLICT (status) DO NOTHING;

-- 15. Transactions
WITH ord AS (
  SELECT order_id, order_customer_id, order_restaurant_branch_id, order_total FROM orders o
  JOIN customer c ON o.order_customer_id = c.customer_id
  JOIN users u ON c.customer_user_id = u.user_id
  WHERE u.user_email = 'mohamed@example.com'
  LIMIT 1
)
INSERT INTO transactions (
  transaction_status,
  transaction_order_id,
  transaction_payment_type,
  transaction_customer_id,
  transaction_rest_branch_id,
  transaction_amount
)
SELECT 'COMPLETED', ord.order_id, 'CARD', ord.order_customer_id, ord.order_restaurant_branch_id, ord.order_total
FROM ord
WHERE NOT EXISTS (SELECT 1 FROM transactions t WHERE t.transaction_order_id = ord.order_id);

