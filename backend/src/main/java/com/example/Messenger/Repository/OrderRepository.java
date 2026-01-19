package com.example.Messenger.Repository;

import com.example.Messenger.Entity.Order;
import com.example.Messenger.Record.Orther.ChurnRisk;
import com.example.Messenger.Record.DTO.DashboardMetricsDTO;
import com.example.Messenger.Record.View.DashboardMetricsView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,String> {
    List<Order> findByUserId(String userId);
    List<Order> findByCustomerEmail(String customerEmail);
    @Query(
            value = """
WITH avg_import_price AS (
    SELECT
        si.product_id,
        SUM(si.quantity * si.import_price)::NUMERIC
        / SUM(si.quantity) AS avg_import_price
    FROM stock_import si
    GROUP BY si.product_id
)
SELECT
    /* 1. Total revenue */
    COALESCE(
        SUM(oi.quantity * oi.sell_price)::NUMERIC,
        0::NUMERIC
    ) AS total_revenue,

    /* 2. Total orders */
    COUNT(DISTINCT o.id) AS total_orders,

    /* 3. Total customers */
    COUNT(DISTINCT o.customer_email) AS total_customers,

    /* 4. Order frequency */
    CASE
        WHEN COUNT(DISTINCT o.customer_email) = 0
            THEN 0::NUMERIC
        ELSE
            COUNT(DISTINCT o.id)::NUMERIC
            / COUNT(DISTINCT o.customer_email)
    END AS order_frequency,

    /* 5. Average order value */
    CASE
        WHEN COUNT(DISTINCT o.id) = 0
            THEN 0::NUMERIC
        ELSE
            SUM(oi.quantity * oi.sell_price)::NUMERIC
            / COUNT(DISTINCT o.id)
    END AS avg_order_value,

    /* 6. Total cost */
    COALESCE(
        SUM(
            oi.quantity * COALESCE(aip.avg_import_price, 0::NUMERIC)
        ),
        0::NUMERIC
    ) AS total_cost,

    /* 7. Profit margin */
    CASE
        WHEN SUM(oi.quantity * oi.sell_price) = 0
            THEN 0::NUMERIC
        ELSE
            (
                SUM(oi.quantity * oi.sell_price)::NUMERIC
                - SUM(
                    oi.quantity * COALESCE(aip.avg_import_price, 0::NUMERIC)
                )
            )
            / SUM(oi.quantity * oi.sell_price)::NUMERIC
    END AS profit_margin
FROM orders o
JOIN order_item oi ON oi.order_id = o.id
JOIN product p ON p.id = oi.product_id
LEFT JOIN avg_import_price aip ON aip.product_id = p.id
WHERE o.status = 'CONFIRMED';
        """,
            nativeQuery = true
    )
    Optional<DashboardMetricsView> findWarehousesWithEnoughStock();
    @Query(nativeQuery = true,value = """
             WITH order_stats AS (
                SELECT
                    o.customer_email,
                    MAX(o.created_at)                                   AS last_order_at,
                    COUNT(*) FILTER (
                        WHERE o.created_at >= NOW() - INTERVAL '30 days'
                    )                                                    AS orders_last_30_days
                FROM orders o
                WHERE o.status = 'CONFIRMED'
                GROUP BY o.customer_email
            ),
            scores AS (
                SELECT
                    customer_email,
                    last_order_at,
                    orders_last_30_days,
            
                    -- RECENCY SCORE (0 → 1)
                    LEAST(
                        EXTRACT(DAY FROM (NOW() - last_order_at)) / 30.0,
                        1
                    ) AS recency_score,
            
                    -- FREQUENCY SCORE (0 → 1)
                    LEAST(
                        orders_last_30_days / 10.0,
                        1
                    ) AS frequency_score
                FROM order_stats
            )
            SELECT
                customer_email,
                last_order_at,
                orders_last_30_days,
            
                ROUND(recency_score, 2)   AS recency_score,
                ROUND(frequency_score, 2) AS frequency_score,
            
                ROUND(
                    LEAST(
                        0.7 * recency_score +
                        0.3 * (1 - frequency_score),
                        1
                    ),
                    2
                ) AS churn_risk,
            
                CASE
                    WHEN
                        LEAST(
                            0.7 * recency_score +
                            0.3 * (1 - frequency_score),
                            1
                        ) <= 0.3 THEN 'HEALTHY'
                    WHEN
                        LEAST(
                            0.7 * recency_score +
                            0.3 * (1 - frequency_score),
                            1
                        ) <= 0.6 THEN 'AT_RISK'
                    ELSE 'HIGH_RISK'
                END AS churn_label
            FROM scores
            ORDER BY churn_risk DESC;
            """)
    Optional<ChurnRisk> calculateChurnRisk();
}