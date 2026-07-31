-- The room "code" (room_number) only had to be unique within its own room type,
-- so two room types of the same hotel could accidentally share a code (e.g. two
-- "R22"s), which is confusing now that the code is the room's whole visual identity
-- ("R22 - Quarto Casal") shown across the site/dashboard/calendar. Denormalizes
-- hotel_id onto hotel_rooms (same pattern as hotel_reservations.hotel_id) so a plain
-- UNIQUE constraint — which can't reach across the room_type_id join — can enforce
-- "unique per hotel" instead.

ALTER TABLE hotel_rooms ADD COLUMN IF NOT EXISTS hotel_id INTEGER REFERENCES hotels(id);

UPDATE hotel_rooms hr
SET hotel_id = rt.hotel_id
FROM hotel_room_types rt
WHERE hr.room_type_id = rt.id
  AND hr.hotel_id IS NULL;

ALTER TABLE hotel_rooms ALTER COLUMN hotel_id SET NOT NULL;

ALTER TABLE hotel_rooms DROP CONSTRAINT IF EXISTS hotel_rooms_room_type_id_room_number_key;
ALTER TABLE hotel_rooms DROP CONSTRAINT IF EXISTS hotel_rooms_hotel_id_room_number_key;
ALTER TABLE hotel_rooms ADD CONSTRAINT hotel_rooms_hotel_id_room_number_key UNIQUE (hotel_id, room_number);
