CREATE TABLE IF NOT EXISTS orders (
    order_id UUID PRIMARY KEY,
    shopping_cart_id UUID,
    user_name VARCHAR(255) NOT NULL,

    payment_id UUID,
    delivery_id UUID,

    state VARCHAR(50) NOT NULL,

    delivery_weight DOUBLE PRECISION,
    delivery_volume DOUBLE PRECISION,
    fragile BOOLEAN,

    total_price NUMERIC(19,2),
    delivery_price NUMERIC(19,2)
);

-- Индексы для ускорения поиска
CREATE INDEX IF NOT EXISTS idx_orders_user_name ON orders(user_name);
CREATE INDEX IF NOT EXISTS idx_orders_shopping_cart_id ON orders(shopping_cart_id);

CREATE TABLE IF NOT EXISTS order_item_mapping (
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity BIGINT NOT NULL,

    PRIMARY KEY (order_id, product_id),

    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);
