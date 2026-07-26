-- Zebra Travel - initial schema
-- Adapted from f_db.sql, extended with bookings, site_content and app_settings
-- needed to back the admin (zebradash) and client (zebratravel) frontends.

-- Usuarios e papeis ------------------------------------------------------

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL -- 'ADMIN', 'CLIENT'
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    full_name VARCHAR(255),
    phone VARCHAR(50),
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Tours (destinos), categorias e ligacao muitos para muitos --------------

CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name TEXT UNIQUE NOT NULL
);

CREATE TABLE tours (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    image TEXT NOT NULL,
    tours INTEGER NOT NULL DEFAULT 0,
    description TEXT NOT NULL,
    created_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE tour_categories (
    tour_id INTEGER REFERENCES tours(id) ON DELETE CASCADE,
    category_id INTEGER REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (tour_id, category_id)
);

-- Excursions ----------------------------------------------------------

CREATE TABLE excursions (
    id SERIAL PRIMARY KEY,
    slug TEXT NOT NULL UNIQUE,
    title TEXT NOT NULL,
    image TEXT,
    price NUMERIC(10, 2) NOT NULL,
    duration TEXT,
    location TEXT,
    rating NUMERIC(2, 1) DEFAULT 0,
    reviews INTEGER DEFAULT 0,
    description TEXT,
    categories TEXT[],
    created_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Produtos e categorias ---------------------------------------------------

CREATE TABLE product_categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    image_url TEXT NOT NULL,
    link TEXT,
    category_id INTEGER REFERENCES product_categories(id) ON DELETE SET NULL,
    created_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE favorite_products (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id INTEGER NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    date_added DATE NOT NULL DEFAULT CURRENT_DATE,
    UNIQUE (user_id, product_id)
);

-- Carrinho de compras -----------------------------------------------------

CREATE TABLE cart_items (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id INTEGER REFERENCES products(id),
    name TEXT NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    image_url TEXT NOT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Reservas (bookings) ------------------------------------------------------

CREATE TABLE bookings (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    excursion_id INTEGER REFERENCES excursions(id) ON DELETE SET NULL,
    tour_id INTEGER REFERENCES tours(id) ON DELETE SET NULL,
    item_name TEXT NOT NULL,
    booking_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, CONFIRMED, CANCELLED
    amount NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Blog posts ---------------------------------------------------------------

CREATE TABLE post_categories (
    id SERIAL PRIMARY KEY,
    name TEXT UNIQUE NOT NULL
);

CREATE TABLE post_layouts (
    id SERIAL PRIMARY KEY,
    layout TEXT UNIQUE CHECK (layout IN ('top', 'bottom'))
);

CREATE TABLE authors (
    id SERIAL PRIMARY KEY,
    user_id INTEGER UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    bio TEXT,
    profile_image TEXT
);

CREATE TABLE posts (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    slug TEXT UNIQUE,
    link TEXT,
    date DATE NOT NULL,
    image TEXT,
    description TEXT,
    content TEXT,
    author_id INTEGER REFERENCES authors(id) ON DELETE SET NULL,
    post_category_id INTEGER REFERENCES post_categories(id) ON DELETE SET NULL,
    layout_id INTEGER REFERENCES post_layouts(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- FAQ ----------------------------------------------------------------------

CREATE TABLE faq_tabs (
    id SERIAL PRIMARY KEY,
    label TEXT NOT NULL UNIQUE
);

CREATE TABLE faqs (
    id SERIAL PRIMARY KEY,
    faq_tab_id INTEGER NOT NULL REFERENCES faq_tabs(id) ON DELETE CASCADE,
    question TEXT NOT NULL,
    answer TEXT NOT NULL
);

-- Galeria de imagens e categorias ------------------------------------------

CREATE TABLE gallery_items (
    id SERIAL PRIMARY KEY,
    img_src TEXT NOT NULL
);

CREATE TABLE gallery_categories (
    id SERIAL PRIMARY KEY,
    name TEXT UNIQUE NOT NULL
);

CREATE TABLE gallery_item_categories (
    gallery_item_id INTEGER NOT NULL REFERENCES gallery_items(id) ON DELETE CASCADE,
    category_id INTEGER NOT NULL REFERENCES gallery_categories(id) ON DELETE CASCADE,
    PRIMARY KEY (gallery_item_id, category_id)
);

-- Sponsors, Team members e Testimonials ------------------------------------

CREATE TABLE sponsors (
    id SERIAL PRIMARY KEY,
    image TEXT NOT NULL,
    link TEXT NOT NULL
);

CREATE TABLE team_members (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    designation TEXT NOT NULL,
    image TEXT NOT NULL
);

CREATE TABLE testimonials (
    id SERIAL PRIMARY KEY,
    image TEXT NOT NULL,
    text TEXT NOT NULL,
    name TEXT NOT NULL,
    designation TEXT NOT NULL,
    rating NUMERIC(2, 1) NOT NULL,
    background_image TEXT,
    link TEXT
);

-- Travel Packages -----------------------------------------------------------

CREATE TABLE travel_packages (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    duration TEXT NOT NULL,
    location TEXT NOT NULL,
    price TEXT NOT NULL,
    description TEXT NOT NULL,
    link TEXT NOT NULL,
    image_url TEXT NOT NULL
);

-- Site content (editor de conteudo) e settings -------------------------------

CREATE TABLE site_content (
    content_key VARCHAR(100) PRIMARY KEY,
    content_value JSONB NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE app_settings (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value TEXT NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
