CREATE TABLE IF NOT EXISTS payment (
    payment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,

    product_cost NUMERIC(10,2),
    delivery_cost NUMERIC(10,2),
    tax_cost NUMERIC(10,2),
    total_cost NUMERIC(10,2),

    payment_status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    version BIGINT
);

-- Индексы
CREATE INDEX IF NOT EXISTS idx_payment_order_id
ON payment(order_id);

CREATE INDEX IF NOT EXISTS idx_payment_state
ON payment(payment_status);

CREATE INDEX IF NOT EXISTS idx_payment_created_at
ON payment(created_at);