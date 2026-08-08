ALTER TABLE excursion_reviews  ADD COLUMN is_testimonial BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE hotel_room_reviews ADD COLUMN is_testimonial BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE testimonials ADD COLUMN source_review_type VARCHAR(20);
ALTER TABLE testimonials ADD COLUMN source_review_id INTEGER;
ALTER TABLE testimonials ALTER COLUMN image DROP NOT NULL;
ALTER TABLE testimonials ALTER COLUMN designation DROP NOT NULL;
