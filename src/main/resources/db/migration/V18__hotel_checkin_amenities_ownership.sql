-- Check-in/check-out timestamps on reservations, ownership (createdBy) on room types/rooms
-- (so AGENTE can create/delete their own, like Excursion/Tour/Post/Product), and a dynamic
-- amenities catalog to replace the hardcoded list in zebradash/zebratravel lib/amenities.ts.

ALTER TABLE hotel_reservations ADD COLUMN IF NOT EXISTS checked_in_at TIMESTAMP;
ALTER TABLE hotel_reservations ADD COLUMN IF NOT EXISTS checked_out_at TIMESTAMP;

ALTER TABLE hotel_room_types ADD COLUMN IF NOT EXISTS created_by INTEGER REFERENCES users(id);
ALTER TABLE hotel_rooms ADD COLUMN IF NOT EXISTS created_by INTEGER REFERENCES users(id);

CREATE TABLE IF NOT EXISTS hotel_amenities (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    label VARCHAR(100) NOT NULL,
    icon VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO hotel_amenities (code, label, icon) VALUES
    ('AC', 'Ar Condicionado', 'fa-snowflake'),
    ('WIFI', 'Wi-Fi', 'fa-wifi'),
    ('TV', 'TV', 'fa-tv'),
    ('SAFE', 'Cofre', 'fa-lock'),
    ('KITCHEN', 'Cozinha Equipada', 'fa-utensils'),
    ('FIREPLACE', 'Lareira', 'fa-fire'),
    ('BATHROOM_ESSENTIALS', 'Kit de Banho', 'fa-bath'),
    ('BBQ', 'Churrasqueira', 'fa-fire-burner'),
    ('PARKING', 'Estacionamento', 'fa-parking'),
    ('BREAKFAST', 'Pequeno-Almoço Incluído', 'fa-mug-hot'),
    ('PET_FRIENDLY', 'Aceita Animais', 'fa-paw'),
    ('WORKSPACE', 'Espaço de Trabalho', 'fa-briefcase')
ON CONFLICT (code) DO NOTHING;
