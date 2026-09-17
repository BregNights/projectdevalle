-- Garante no máximo um endereço de entrega principal por restaurante (RF30.2).
CREATE UNIQUE INDEX uk_restaurant_delivery_addresses_one_primary
    ON restaurant_delivery_addresses(restaurant_id)
    WHERE is_primary;

CREATE INDEX idx_producers_status ON producers(status);
CREATE INDEX idx_producers_user_id ON producers(user_id);
CREATE INDEX idx_restaurants_status ON restaurants(status);
CREATE INDEX idx_restaurants_user_id ON restaurants(user_id);
