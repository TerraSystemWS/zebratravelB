INSERT INTO hotels (name, address, city, description, image) VALUES
  ('Colonial House', 'Alto São Pedro', 'São Filipe - Ilha do Fogo', 'Pousada instalada num dos sobrados mais antigos da cidade, restaurado com charme colonial.', '/images/background/booking-bg.jpg'),
  ('Casa Konig', 'São Filipe', 'São Filipe - Ilha do Fogo', 'Alojamento acolhedor no coração de São Filipe.', '/images/background/booking-bg.jpg');

INSERT INTO hotel_room_types (hotel_id, name, description, base_price, capacity, image)
SELECT id, 'Quarto Standard Duplo', 'Quarto confortável com casa de banho privativa, ideal para casais.', 45.00, 2, '/images/background/booking-bg.jpg'
FROM hotels WHERE name = 'Colonial House';

INSERT INTO hotel_room_types (hotel_id, name, description, base_price, capacity, image)
SELECT id, 'Suite Familiar', 'Suite espaçosa para famílias, até 4 pessoas.', 75.00, 4, '/images/background/booking-bg.jpg'
FROM hotels WHERE name = 'Colonial House';

INSERT INTO hotel_room_types (hotel_id, name, description, base_price, capacity, image)
SELECT id, 'Quarto Standard', 'Quarto simples e confortável.', 35.00, 2, '/images/background/booking-bg.jpg'
FROM hotels WHERE name = 'Casa Konig';

INSERT INTO hotel_rooms (room_type_id, room_number, floor)
SELECT id, '101', '1' FROM hotel_room_types WHERE name = 'Quarto Standard Duplo';
INSERT INTO hotel_rooms (room_type_id, room_number, floor)
SELECT id, '102', '1' FROM hotel_room_types WHERE name = 'Quarto Standard Duplo';
INSERT INTO hotel_rooms (room_type_id, room_number, floor)
SELECT id, '201', '2' FROM hotel_room_types WHERE name = 'Suite Familiar';
INSERT INTO hotel_rooms (room_type_id, room_number, floor)
SELECT id, '01', '0' FROM hotel_room_types WHERE name = 'Quarto Standard';
