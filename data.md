# Database Schema Description

This document describes the database schema for a food delivery or restaurant management system, organized by functional modules.

## 1. User & Customer Module
This section handles user authentication, roles, and addresses.

### Tables

**User**
- **PK**: `user_id`
- **FK**: `user_type_id` (references `User_Type`)
- **Attributes**: `user_first_name`, `user_last_name`, `user_dob`, `user_phone`, `user_email`, `user_password`, `user_gender`, `joined_at`, `is_enabled`

**User_Type**
- **PK**: `user_type_id`
- **Attributes**: `user_type_name`

**Role**
- **PK**: `role_id`
- **Attributes**: `role_name`

**User_Role**
- **PK**: `role_id`, `user_id` (Composite Primary Key)
- **FK**: `role_id` (references `Role`)
- **FK**: `user_id` (references `User`)

**Customer**
- **PK**: `customer_id`
- **FK**: `user_id` (references `User`)
- **FK**: `default_address_id` (references `Customer_Address`)
- **FK**: `preferred_payment_id` (references `payment_type_config` - inferred from Payment section)

**Customer_Address**
- **PK**: `customer_address_id`
- **FK**: `customer_id` (references `Customer`)
- **Attributes**: `lable` (label), `city`, `street`, `building`, `apartment`, `note`

## 2. Cart Module
This section manages shopping carts and their contents.

### Tables

**cart**
- **PK**: `cart_id`
- **FK**: `cart_customer_id` (references `Customer`)
- **Attributes**: `current_rest_id` (references `Restaurant`), `notes`, `is_locked`

**cart_item**
- **PK**: `cart_id`, `menu_item_id` (Composite Primary Key)
- **FK**: `cart_id` (references `cart`)
- **FK**: `menu_item_id` (references `menu_item`)
- **Attributes**: `quantity`, `note`

## 3. Restaurant & Menu Module
This section stores restaurant details, branches, and menu items.

### Tables

**restaurant**
- **PK**: `rest_ID`
- **Attributes**: `rest_name`, `rest_description`

**Restaurant_branch**
- **PK**: `branch_id`
- **FK**: `branch_rest_id` (references `restaurant`)
- **Attributes**: `branch_delivery_fee`, `branch_min_order`, `branch_City`, `branch_open_time`, `branch_close_time`, `branch_phone_number`, `branch_estimated_delivery_time`, `last_modified`, `created_at`, `created_by`, `modified_by`, `admin_id`

**Restaurant_Menu**
- **PK**: `menu_id`
- **FK**: `menu_rest_id` (references `restaurant`)
- **Attributes**: `rest_menu_name`, `created_at`, `last_modified`, `modified_by`, `created_by`

**menu_item**
- **PK**: `menu_item_id`
- **FK**: `rest_men_id` (references `Restaurant_Menu`)
- **FK**: `created_by` (references `User`)
- **FK**: `modified_by` (references `User`)
- **Attributes**: `menu_item_description`, `menu_item_name`, `menu_item_price`, `created_at`, `last_modified`

**category**
- **PK**: `category_id`
- **Attributes**: `category_name`

**restaurant_Category**
- **PK**: `category_id`, `restaurant_id` (Composite Primary Key)
- **FK**: `category_id` (references `category`)
- **FK**: `restaurant_id` (references `restaurant`)

## 4. Order Module
This section handles the order lifecycle, items, and tracking.

### Tables

**orders**
- **PK**: `order_id`
- **FK**: `order_address_id` (references `Customer_Address` or similar address entity)
- **FK**: `order_customer_id` (references `Customer`)
- **FK**: `order_status` (references `Ord_Status`)
- **FK**: `order_restaurant_branch` (references `Restaurant_branch`)
- **FK**: `order_tracking_id` (references `order_tracking`)
- **Attributes**: `order_subtotal`, `order_fee`, `order_total`, `order_date`, `order_note`

**Order_Item**
- **PK**: `order_id`, `menu_item_id` (Composite Primary Key)
- **FK**: `order_id` (references `orders`)
- **FK**: `menu_item_id` (references `menu_item`)
- **Attributes**: `unit_price`, `quantity`, `subtotal`, `note`

**Ord_Status**
- **PK**: `Status`
- **Attributes**: (Likely description/name, though not explicitly listed)

**order_tracking**
- **PK**: `order_tracking_id`
- **Attributes**: `description`, `changed_at`, `estimated_time`

## 5. Payment Module
This section manages transactions and payment configurations.

### Tables

**transaction**
- **PK**: `transaction_id`
- **FK**: `transaction_status` (references `transaction_status`)
- **FK**: `transaction_order_id` (references `orders`)
- **FK**: `transaction_payment_type` (references `payment_integration_type`)
- **FK**: `transaction_customer_id` (references `Customer`)
- **FK**: `transaction_restaurant_id` (references `Restaurant`)
- **Attributes**: `transaction_amount`, `transaction_time`

**transaction_status**
- **PK**: `status`

**payment_integration_type**
- **PK**: (Not explicitly listed, likely `payment_integration_type_name`)
- **Attributes**: `payment_integration_type_name`

**payment_type_config**
- **PK**: `PTC_id`
- **FK**: `payment_integration_type` (references `payment_integration_type`)
- **Attributes**: `config_details`

## 6. System & Audit Module
This section handles system configuration and audit logs.

### Tables

**System_Config**
- **PK**: (Not explicitly listed, likely a config key)
- **Attributes**: `Service_Fee`, `Small_Order_Fee`

**Audit**
- **PK**: `Audit_ID`
- **Attributes**: `audit_type`, `details`, `timestamp`

## Relationship Summary (Inferred from FKs)

1.  **User -> Customer**: One-to-One (User has a customer profile).
2.  **Customer -> Cart**: One-to-Many (Customer can have carts).
3.  **Cart -> Cart_Item**: One-to-Many (Cart contains multiple items).
4.  **Restaurant -> Restaurant_Branch**: One-to-Many.
5.  **Restaurant -> Restaurant_Menu**: One-to-Many.
6.  **Restaurant_Menu -> Menu_Item**: One-to-Many.
7.  **Order -> Order_Item**: One-to-Many.
8.  **Customer -> Orders**: One-to-Many.
9.  **Transaction -> Orders**: One-to-One (or One-to-Many if split payments allowed).
10. **Transaction -> Customer**: One-to-Many.