CREATE TABLE IF NOT EXISTS delivery (
    delivery_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL UNIQUE,
    delivery_state VARCHAR(20) NOT NULL CHECK (delivery_state IN ('CREATED', 'IN_PROGRESS', 'DELIVERED', 'FAILED', 'CANCELLED')),

    from_country VARCHAR(100),
    from_city VARCHAR(100),
    from_street VARCHAR(255),
    from_house VARCHAR(50),
    from_flat VARCHAR(50),

    to_country VARCHAR(100),
    to_city VARCHAR(100),
    to_street VARCHAR(255),
    to_house VARCHAR(50),
    to_flat VARCHAR(50),

    total_weight DOUBLE PRECISION,
    total_volume DOUBLE PRECISION,
    fragile BOOLEAN DEFAULT FALSE,
    delivery_cost DECIMAL(10,2),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_delivery_order_id ON delivery(order_id);
CREATE INDEX IF NOT EXISTS idx_delivery_state ON delivery(delivery_state);
CREATE INDEX IF NOT EXISTS idx_delivery_created_at ON delivery(created_at);