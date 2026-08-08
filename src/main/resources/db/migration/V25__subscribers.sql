CREATE TABLE subscribers (
    id            SERIAL PRIMARY KEY,
    email         TEXT NOT NULL UNIQUE,
    subscribed_at TIMESTAMP NOT NULL DEFAULT now(),
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);

INSERT INTO subscribers (email, subscribed_at)
SELECT email, created_at FROM users
ON CONFLICT (email) DO NOTHING;
