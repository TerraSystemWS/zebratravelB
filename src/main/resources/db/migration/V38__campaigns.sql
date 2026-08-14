CREATE TABLE campaigns (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(255),
    placement VARCHAR(30) NOT NULL,
    voucher_id INTEGER REFERENCES vouchers(id) ON DELETE SET NULL,
    product_id INTEGER REFERENCES products(id) ON DELETE SET NULL,
    excursion_id INTEGER REFERENCES excursions(id) ON DELETE SET NULL,
    room_type_id INTEGER REFERENCES hotel_room_types(id) ON DELETE SET NULL,
    title VARCHAR(255),
    subtitle VARCHAR(255),
    link_url VARCHAR(500),
    start_date DATE,
    end_date DATE,
    priority INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT true,
    click_count BIGINT NOT NULL DEFAULT 0,
    created_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_campaigns_placement ON campaigns(placement);
