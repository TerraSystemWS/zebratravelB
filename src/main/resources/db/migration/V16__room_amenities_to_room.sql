-- Amenities and photo gallery move from HotelRoomType (shared by every physical room of
-- that type) to HotelRoom (per physical room), so rooms of the same type can have
-- different amenities/photos. hotel_rooms.images already existed (V8) but was never
-- populated from the type-level gallery; this backfills it before hotel_room_types loses
-- its own images/amenities columns.

ALTER TABLE hotel_rooms ADD COLUMN IF NOT EXISTS amenities text[] NOT NULL DEFAULT '{}';

UPDATE hotel_rooms r
SET amenities = rt.amenities
FROM hotel_room_types rt
WHERE r.room_type_id = rt.id
  AND (r.amenities IS NULL OR array_length(r.amenities, 1) IS NULL)
  AND rt.amenities IS NOT NULL AND array_length(rt.amenities, 1) IS NOT NULL;

UPDATE hotel_rooms r
SET images = rt.images
FROM hotel_room_types rt
WHERE r.room_type_id = rt.id
  AND (r.images IS NULL OR array_length(r.images, 1) IS NULL)
  AND rt.images IS NOT NULL AND array_length(rt.images, 1) IS NOT NULL;

ALTER TABLE hotel_room_types DROP COLUMN IF EXISTS amenities;
ALTER TABLE hotel_room_types DROP COLUMN IF EXISTS images;
