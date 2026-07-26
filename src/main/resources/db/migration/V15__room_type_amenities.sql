ALTER TABLE hotel_room_types ADD COLUMN IF NOT EXISTS amenities text[] NOT NULL DEFAULT '{}';
ALTER TABLE hotel_room_types ADD COLUMN IF NOT EXISTS images text[] NOT NULL DEFAULT '{}';
