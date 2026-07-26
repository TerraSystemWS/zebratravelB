CREATE TABLE hotels (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    address TEXT,
    city TEXT,
    description TEXT,
    image TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE hotel_room_types (
    id SERIAL PRIMARY KEY,
    hotel_id INTEGER NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    description TEXT,
    base_price NUMERIC(10, 2) NOT NULL,
    capacity INTEGER NOT NULL DEFAULT 2,
    image TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE hotel_rooms (
    id SERIAL PRIMARY KEY,
    room_type_id INTEGER NOT NULL REFERENCES hotel_room_types(id) ON DELETE CASCADE,
    room_number TEXT NOT NULL,
    floor TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',   -- ACTIVE, MAINTENANCE
    UNIQUE (room_type_id, room_number)
);

CREATE TABLE hotel_availability_blocks (
    id SERIAL PRIMARY KEY,
    room_id INTEGER NOT NULL REFERENCES hotel_rooms(id) ON DELETE CASCADE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT
);

CREATE TABLE hotel_reservations (
    id SERIAL PRIMARY KEY,
    hotel_id INTEGER NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    room_id INTEGER REFERENCES hotel_rooms(id) ON DELETE SET NULL,
    user_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    guest_name TEXT,
    guest_email TEXT,
    guest_phone TEXT,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    guests INTEGER NOT NULL DEFAULT 1,
    total_amount NUMERIC(10, 2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,      -- ONLINE, TRANSFER, CASH
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT',  -- PENDING_PAYMENT, AWAITING_TRANSFER, AWAITING_CASH, ON_HOLD, CONFIRMED, PAID, CANCELLED
    merchant_ref VARCHAR(50),
    merchant_session VARCHAR(50),
    created_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CHECK (check_out > check_in)
);
