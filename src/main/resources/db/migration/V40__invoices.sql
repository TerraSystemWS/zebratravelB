ALTER TABLE orders ADD COLUMN guest_name VARCHAR(255);
ALTER TABLE orders ADD COLUMN guest_email VARCHAR(255);
ALTER TABLE orders ADD COLUMN customer_nif VARCHAR(50);
ALTER TABLE bookings ADD COLUMN customer_nif VARCHAR(50);
ALTER TABLE hotel_reservations ADD COLUMN customer_nif VARCHAR(50);

CREATE TABLE invoice_sequences (
    id SERIAL PRIMARY KEY,
    series VARCHAR(10) NOT NULL,
    year INTEGER NOT NULL,
    next_number INTEGER NOT NULL DEFAULT 1,
    UNIQUE (series, year)
);

CREATE TABLE invoices (
    id SERIAL PRIMARY KEY,
    series VARCHAR(10) NOT NULL,
    number INTEGER NOT NULL,
    year INTEGER NOT NULL,
    document_type VARCHAR(20) NOT NULL,
    source_type VARCHAR(20) NOT NULL,
    source_id INTEGER NOT NULL,
    user_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255),
    customer_nif VARCHAR(50),
    currency VARCHAR(3) NOT NULL DEFAULT 'CVE',
    subtotal NUMERIC(12,2) NOT NULL,
    total_amount NUMERIC(12,2) NOT NULL,
    pdf_stored_filename VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ISSUED',
    created_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (series, year, number)
);

CREATE TABLE invoice_lines (
    id SERIAL PRIMARY KEY,
    invoice_id INTEGER NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    description VARCHAR(500) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    line_total NUMERIC(12,2) NOT NULL
);

CREATE INDEX idx_invoices_source ON invoices(source_type, source_id);
CREATE INDEX idx_invoices_user ON invoices(user_id);
