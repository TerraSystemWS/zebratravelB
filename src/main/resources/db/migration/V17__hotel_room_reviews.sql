CREATE TABLE IF NOT EXISTS hotel_room_reviews (
    id SERIAL PRIMARY KEY,
    room_id INTEGER NOT NULL REFERENCES hotel_rooms(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (room_id, user_id)
);
