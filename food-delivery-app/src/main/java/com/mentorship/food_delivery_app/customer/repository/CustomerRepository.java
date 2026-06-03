package com.mentorship.food_delivery_app.customer.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mentorship.food_delivery_app.customer.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    @Query(value = """
        WITH latest_orders AS (
            SELECT o.order_id
            FROM orders o
            WHERE o.order_customer_id = :customerId
            ORDER BY o.order_date DESC
            LIMIT 10
        )
        SELECT
            o.order_id                         AS orderId,
            os.order_status_name               AS status,
            o.order_date                       AS orderedAt,
            r.restaurant_name                  AS restaurantName,
            o.order_total                      AS total,
            m.menu_item_name                   AS itemName,
            oi.order_item_quantity             AS quantity,
            oi.order_item_unit_price           AS unitPrice
        FROM latest_orders lo
        JOIN orders o                ON o.order_id = lo.order_id
        JOIN order_status os         ON os.order_status_id = o.order_status_id
        JOIN order_item oi           ON oi.order_item_order_id = o.order_id
        JOIN menu_item m             ON m.menu_item_id = oi.order_item_menu_item_id
        JOIN restaurant_branch rb    ON rb.branch_id = o.order_restaurant_branch_id
        JOIN restaurant r            ON r.restaurant_id = rb.branch_rest_id
        ORDER BY o.order_date DESC, oi.order_item_id
        """, nativeQuery = true)
    List<Object[]> findLatest10OrderRowsByCustomerId(@Param("customerId") UUID customerId);
}
