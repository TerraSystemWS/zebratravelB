-- Seed data for local development / demo

INSERT INTO roles (name) VALUES ('ADMIN'), ('CLIENT');

-- Admin user: terra.systemltd@gmail.com / terrasystem1 (matches previous fake-login credentials)
INSERT INTO users (username, email, password_hash, full_name, role_id, status)
VALUES ('admin', 'terra.systemltd@gmail.com', '$2b$10$rY8kLQas2ar5oQLVzKJV7uNHIETtoWzdJKQzdMdyAIjfV4ZdP5hru', 'Terra System Admin', (SELECT id FROM roles WHERE name = 'ADMIN'), 'ACTIVE');

-- Demo client user: cliente@example.com / cliente123
INSERT INTO users (username, email, password_hash, full_name, role_id, status)
VALUES ('cliente', 'cliente@example.com', '$2b$10$VJ/FAx19Azlo2V.axwchFuBR.XYcHclpqvA5cGLznGe9GSAaSV/Ce', 'Cliente Demo', (SELECT id FROM roles WHERE name = 'CLIENT'), 'ACTIVE');

INSERT INTO categories (name) VALUES
  ('asia'), ('india'), ('europe'), ('n-america'), ('s-america'), ('africa'), ('australia');

INSERT INTO tours (title, image, tours, description) VALUES
  ('Rajasthan', '/images/resource/tour-1.jpg', 6, 'Experience the royal heritage of Rajasthan, from palaces to deserts.'),
  ('Indonesia', '/images/resource/tour-2.jpg', 8, 'Explore the beautiful islands and rich culture of Indonesia.'),
  ('Helsingfors', '/images/resource/tour-3.jpg', 5, 'Discover the modern architecture and vibrant culture of Helsinki.'),
  ('Tavastehus', '/images/resource/tour-4.jpg', 4, 'Enjoy the natural beauty and historical sites in Tavastehus, Finland.'),
  ('Grankulla', '/images/resource/tour-5.jpg', 7, 'Visit Grankulla for its charming coastal views and parks.'),
  ('Tuscany', '/images/resource/tour-6.jpg', 10, 'Savor the wines and landscapes of beautiful Tuscany in Italy.'),
  ('Grand Canyon', '/images/resource/tour-7.jpg', 15, 'Experience the breathtaking views and hiking trails of the Grand Canyon.'),
  ('Machu Picchu', '/images/resource/tour-8.jpg', 12, 'Explore the ancient Incan ruins and stunning scenery of Machu Picchu.'),
  ('Cape Town', '/images/resource/tour-9.jpg', 9, 'Discover the vibrant culture and natural beauty of Cape Town, South Africa.'),
  ('Sydney', '/images/resource/tour-10.jpg', 11, 'Experience the iconic Sydney Opera House and stunning harbor views.');

INSERT INTO tour_categories (tour_id, category_id)
SELECT t.id, c.id FROM tours t JOIN categories c ON
  (t.title = 'Rajasthan' AND c.name IN ('asia', 'india')) OR
  (t.title = 'Indonesia' AND c.name IN ('asia', 'india')) OR
  (t.title = 'Helsingfors' AND c.name = 'europe') OR
  (t.title = 'Tavastehus' AND c.name = 'europe') OR
  (t.title = 'Grankulla' AND c.name = 'europe') OR
  (t.title = 'Tuscany' AND c.name = 'europe') OR
  (t.title = 'Grand Canyon' AND c.name = 'n-america') OR
  (t.title = 'Machu Picchu' AND c.name = 's-america') OR
  (t.title = 'Cape Town' AND c.name = 'africa') OR
  (t.title = 'Sydney' AND c.name = 'australia');

INSERT INTO excursions (slug, title, image, price, duration, location, rating, reviews, description, categories) VALUES
  ('moscow-red-city-land', 'Moscow Red City Land', '/images/resource/f-image-1.jpg', 120, '5 days', '259P, Himalaya Ag', 4, 5, 'Richly varied landscapes, luxurious accommodation and a wide range of wild experiences await you.', ARRAY['easy-m', 'popular']),
  ('yellowstone-mt-rushmore', 'Yellowstone & Mt Rushm', '/images/resource/f-image-2.jpg', 120, '5 days', '259P, Himalaya Ag', 4, 5, 'Richly varied landscapes, luxurious accommodation and a wide range of wild experiences await you.', ARRAY['moderate', 'wild']),
  ('los-angeles-san-francisco', 'Los Angeles to San Franc', '/images/resource/f-image-3.jpg', 120, '5 days', '259P, Himalaya Ag', 4, 5, 'Richly varied landscapes, luxurious accommodation and a wide range of wild experiences await you.', ARRAY['m-difficult', 'urban']),
  ('best-of-switzerland', 'Best Of Switzerland', '/images/resource/f-image-4.jpg', 120, '5 days', '259P, Himalaya Ag', 4, 5, 'Richly varied landscapes, luxurious accommodation and a wide range of wild experiences await you.', ARRAY['difficult', 'mountains']),
  ('italian-dolomites', 'Italian Dolomites', '/images/resource/f-image-5.jpg', 120, '5 days', '259P, Himalaya Ag', 4, 5, 'Richly varied landscapes, luxurious accommodation and a wide range of wild experiences await you.', ARRAY['easy-m', 'hiking']);

INSERT INTO product_categories (name) VALUES
  ('Viagem'), ('Tecnologia'), ('Acessórios'), ('Terra'), ('System');

INSERT INTO products (title, price, image_url, link, category_id) VALUES
  ('Mala de Viagem Smart Air Bag', 225.00, '/images/resource/shop/prod-1.jpg', 'product-single.html', (SELECT id FROM product_categories WHERE name = 'Viagem')),
  ('Mala de Viagem com Roda', 225.00, '/images/resource/shop/prod-2.jpg', 'product-single.html', (SELECT id FROM product_categories WHERE name = 'Viagem')),
  ('Mala de Viagem Leve', 225.00, '/images/resource/shop/prod-3.jpg', 'product-single.html', (SELECT id FROM product_categories WHERE name = 'Viagem')),
  ('Gadgets Smart para Viagem', 225.00, '/images/resource/shop/prod-4.jpg', 'product-single.html', (SELECT id FROM product_categories WHERE name = 'Tecnologia')),
  ('Conjunto de Almofadas de Seda Pretas', 225.00, '/images/resource/shop/prod-5.jpg', 'product-single.html', (SELECT id FROM product_categories WHERE name = 'Acessórios')),
  ('Suporte para Dormir durante a Viagem', 225.00, '/images/resource/shop/prod-6.jpg', 'product-single.html', (SELECT id FROM product_categories WHERE name = 'Viagem')),
  ('Mala de Viagem Preta', 225.00, '/images/resource/shop/prod-7.jpg', 'product-single.html', (SELECT id FROM product_categories WHERE name = 'Viagem')),
  ('Copo de Viagem para Café', 225.00, '/images/resource/shop/prod-8.jpg', 'product-single.html', (SELECT id FROM product_categories WHERE name = 'Acessórios'));

INSERT INTO post_categories (name) VALUES ('Trekking');
INSERT INTO post_layouts (layout) VALUES ('top'), ('bottom');
INSERT INTO authors (name, bio, profile_image) VALUES ('Sword Joy', NULL, NULL);

INSERT INTO posts (title, slug, date, image, description, content, author_id, post_category_id, layout_id) VALUES
  ('THE UPCOMING WBC CHAMP SHIPS 2022 in Feb', 'sdf-3rr-erwfw3-sfsd-565dfs-1', '2022-03-20', '/images/resource/news-1.jpg', 'Fight School has specialized in martial arts since 1986 and has one of the most innovative.', 'sadsdaasas', (SELECT id FROM authors LIMIT 1), (SELECT id FROM post_categories WHERE name = 'Trekking'), (SELECT id FROM post_layouts WHERE layout = 'top')),
  ('THE BEST BOXERS IN THEIR WEIGHT CATEGORY', 'sdf-3rr-erwfw3-sfsd-565dfs-2', '2022-03-20', '/images/resource/news-2.jpg', 'Fight School has specialized in martial arts since 1986 and has one of the most innovative.', 'sadsdaasas', (SELECT id FROM authors LIMIT 1), (SELECT id FROM post_categories WHERE name = 'Trekking'), (SELECT id FROM post_layouts WHERE layout = 'top'));

INSERT INTO bookings (user_id, excursion_id, item_name, booking_date, status, amount) VALUES
  ((SELECT id FROM users WHERE username = 'cliente'), (SELECT id FROM excursions WHERE slug = 'moscow-red-city-land'), 'Moscow Red City Land', '2023-11-15', 'CONFIRMED', 1200),
  ((SELECT id FROM users WHERE username = 'cliente'), (SELECT id FROM excursions WHERE slug = 'best-of-switzerland'), 'Best Of Switzerland', '2023-12-01', 'PENDING', 850);

INSERT INTO app_settings (setting_key, setting_value) VALUES ('maintenance_mode', '0');

INSERT INTO site_content (content_key, content_value) VALUES ('site', '{}'::jsonb);
