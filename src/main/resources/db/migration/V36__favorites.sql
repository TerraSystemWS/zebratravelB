CREATE TABLE favorites (
    id          SERIAL PRIMARY KEY,
    user_id     INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    item_type   VARCHAR(20) NOT NULL, -- ROOM, PRODUCT, EXCURSION, TOUR
    item_id     INTEGER NOT NULL,
    date_added  DATE NOT NULL DEFAULT CURRENT_DATE,
    UNIQUE (user_id, item_type, item_id)
);

-- Migra os favoritos de produto já existentes (única entidade favoritável até agora)
-- para a nova tabela genérica, antes de apagar a antiga.
INSERT INTO favorites (user_id, item_type, item_id, date_added)
SELECT user_id, 'PRODUCT', product_id, date_added FROM favorite_products;

DROP TABLE favorite_products;
