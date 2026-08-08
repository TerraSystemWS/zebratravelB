CREATE TABLE job_applications (
    id                  SERIAL PRIMARY KEY,
    name                TEXT NOT NULL,
    phone               TEXT NOT NULL,
    email               TEXT NOT NULL,
    area                TEXT NOT NULL,
    description         TEXT NOT NULL,
    cv_stored_filename  TEXT NOT NULL,
    cv_original_filename TEXT NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT now()
);
