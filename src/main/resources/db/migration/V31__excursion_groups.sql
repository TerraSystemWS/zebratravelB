-- Um "grupo travel" deixa de viver como um único estado dentro da própria Excursion
-- e passa a ser uma entidade própria: uma excursão confirmada pode receber novas
-- reservas depois de confirmada, e essas reservas devem abrir um novo grupo (nova
-- data a confirmar) em vez de se juntarem ao grupo já confirmado.
CREATE TABLE excursion_groups (
    id SERIAL PRIMARY KEY,
    excursion_id INT NOT NULL REFERENCES excursions(id),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    confirmed_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Backfill: cada excursão que já tinha algum estado de grupo travel (OPEN/CONFIRMED/COMPLETED)
-- vira um único grupo existente, para não perder o histórico já confirmado/aberto.
INSERT INTO excursion_groups (excursion_id, status, confirmed_date, created_at)
SELECT id, group_travel_status, group_travel_confirmed_date, now()
FROM excursions
WHERE group_travel_status <> 'NONE';

ALTER TABLE bookings ADD COLUMN excursion_group_id INT REFERENCES excursion_groups(id);

UPDATE bookings b
SET excursion_group_id = eg.id
FROM excursion_groups eg
WHERE b.excursion_id IS NOT NULL AND b.excursion_id = eg.excursion_id;

ALTER TABLE excursions DROP COLUMN group_travel_status;
ALTER TABLE excursions DROP COLUMN group_travel_confirmed_date;
