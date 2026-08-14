CREATE TABLE hotel_reservation_guests (
    id                SERIAL PRIMARY KEY,
    reservation_id    INTEGER NOT NULL REFERENCES hotel_reservations(id) ON DELETE CASCADE,
    full_name         TEXT NOT NULL,
    date_of_birth     DATE,
    nationality       TEXT,
    passport_number   TEXT,
    is_primary        BOOLEAN NOT NULL DEFAULT false,
    created_at        TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_reservation_guests_reservation ON hotel_reservation_guests(reservation_id);

CREATE TABLE hotel_reservation_guest_documents (
    id                  SERIAL PRIMARY KEY,
    guest_id            INTEGER NOT NULL REFERENCES hotel_reservation_guests(id) ON DELETE CASCADE,
    stored_filename     TEXT NOT NULL,
    original_filename   TEXT NOT NULL,
    content_type        TEXT NOT NULL,
    size_bytes          BIGINT NOT NULL,
    uploaded_at         TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_reservation_guest_documents_guest ON hotel_reservation_guest_documents(guest_id);
