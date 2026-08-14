-- Vouchers (código, ex: "VERAO20") e promoções fixas (sem código, só para Produtos da
-- Loja) partilham a mesma tabela — a diferença é só requires_code. Ver dev-notes.md.
CREATE TABLE vouchers (
    id                  SERIAL PRIMARY KEY,
    code                TEXT UNIQUE,        -- null quando requires_code = false
    requires_code       BOOLEAN NOT NULL DEFAULT true,
    discount_percent    INTEGER NOT NULL CHECK (discount_percent BETWEEN 0 AND 99),
    scope               TEXT NOT NULL,      -- ALL, EXCURSION, ROOM, PRODUCT
    scope_item_id       INTEGER,            -- null = qualquer item desse scope; preenchido = item específico
    valid_from          DATE,
    valid_until         DATE,
    max_uses            INTEGER,            -- null = sem limite
    max_uses_per_user   INTEGER DEFAULT 1,  -- null = sem limite por utilizador
    active              BOOLEAN NOT NULL DEFAULT true,
    created_by          INTEGER REFERENCES users(id) ON DELETE SET NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_vouchers_scope ON vouchers(scope, scope_item_id);

CREATE TABLE voucher_redemptions (
    id                      SERIAL PRIMARY KEY,
    voucher_id              INTEGER NOT NULL REFERENCES vouchers(id) ON DELETE CASCADE,
    user_id                 INTEGER REFERENCES users(id) ON DELETE SET NULL,
    booking_id              INTEGER REFERENCES bookings(id) ON DELETE SET NULL,
    hotel_reservation_id    INTEGER REFERENCES hotel_reservations(id) ON DELETE SET NULL,
    order_id                INTEGER REFERENCES orders(id) ON DELETE SET NULL,
    discount_amount         NUMERIC(10, 2) NOT NULL,
    -- Fica true se a reserva/encomenda associada for cancelada depois de o voucher ter
    -- sido usado — deixa de contar para max_uses/max_uses_per_user, mas o histórico fica.
    released                BOOLEAN NOT NULL DEFAULT false,
    redeemed_at             TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_voucher_redemptions_voucher ON voucher_redemptions(voucher_id, released);
