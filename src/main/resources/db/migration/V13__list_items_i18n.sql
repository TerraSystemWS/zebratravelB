-- Adds i18n content for the items inside Testimonials, News and Group Travel Package
-- sections, which were still hardcoded arrays in the frontend. Nests them under the
-- existing home.testimonials / home.newsSection / home.groupTravel objects (added in
-- V10) via jsonb_set with create_missing=true, so subtitle/title/seeDetails are kept.

UPDATE site_content
SET content_value =
    jsonb_set(
    jsonb_set(
    jsonb_set(content_value,
        '{home,testimonials,items}', $j$
        [
            {
                "image": "/images/resource/testi-thumb-1.jpg",
                "name": "Randall Schwartz",
                "designation": { "pt": "Cliente Fidelizado", "en": "Returning Customer", "fr": "Client Fidèle" },
                "text": { "pt": "A ZebraTravel organizou tudo ao pormenor — desde a excursão ao vulcão até à estadia na Colonial House. Uma experiência inesquecível.", "en": "ZebraTravel organized everything down to the last detail — from the volcano excursion to the stay at Colonial House. An unforgettable experience.", "fr": "ZebraTravel a tout organisé dans les moindres détails — de l'excursion au volcan au séjour à la Colonial House. Une expérience inoubliable." },
                "rating": 5
            },
            {
                "image": "/images/resource/testi-thumb-2.jpg",
                "name": "Andru Smith",
                "designation": { "pt": "Turista Aventureiro", "en": "Adventure Traveler", "fr": "Voyageur Aventurier" },
                "text": { "pt": "A equipa foi extremamente atenciosa e conhecedora da ilha. Recomendo a todos que queiram conhecer o Fogo em segurança.", "en": "The team was extremely attentive and knowledgeable about the island. I recommend it to anyone who wants to explore Fogo safely.", "fr": "L'équipe a été extrêmement attentionnée et connaissait parfaitement l'île. Je le recommande à tous ceux qui souhaitent découvrir Fogo en toute sécurité." },
                "rating": 5
            },
            {
                "image": "/images/resource/testi-thumb-2.jpg",
                "name": "Andru Smith",
                "designation": { "pt": "Turista Aventureiro", "en": "Adventure Traveler", "fr": "Voyageur Aventurier" },
                "text": { "pt": "Já viajei com várias agências, mas a ZebraTravel destaca-se pelo cuidado com os detalhes e pelo atendimento personalizado.", "en": "I've traveled with several agencies, but ZebraTravel stands out for its attention to detail and personalized service.", "fr": "J'ai voyagé avec plusieurs agences, mais ZebraTravel se distingue par son souci du détail et son service personnalisé." },
                "rating": 5
            }
        ]
        $j$::jsonb, true),
        '{home,newsSection,items}', $j$
        [
            {
                "category": { "pt": "Excursões", "en": "Excursions", "fr": "Excursions" },
                "date": "20 Março 2026",
                "author": "Equipa ZebraTravel",
                "image": "images/resource/news-1.jpg",
                "layout": "top",
                "title": { "pt": "Novas rotas de trekking abertas na Ilha do Fogo", "en": "New trekking routes now open on Fogo Island", "fr": "Nouveaux itinéraires de randonnée ouverts sur l'île de Fogo" },
                "description": { "pt": "A ZebraTravel apresenta novos percursos guiados pelo Parque Natural do Fogo, com paisagens vulcânicas únicas.", "en": "ZebraTravel introduces new guided routes through Fogo Natural Park, with unique volcanic landscapes.", "fr": "ZebraTravel présente de nouveaux parcours guidés à travers le Parc Naturel de Fogo, avec des paysages volcaniques uniques." }
            },
            {
                "category": { "pt": "Hotel", "en": "Hotel", "fr": "Hôtel" },
                "date": "20 Março 2026",
                "author": "Equipa ZebraTravel",
                "image": "images/resource/news-2.jpg",
                "layout": "bottom",
                "title": { "pt": "Colonial House renova os seus quartos para 2026", "en": "Colonial House renovates its rooms for 2026", "fr": "La Colonial House rénove ses chambres pour 2026" },
                "description": { "pt": "A nossa pousada histórica ganhou novos quartos remodelados, mantendo o charme colonial original.", "en": "Our historic guesthouse has new remodeled rooms, keeping its original colonial charm.", "fr": "Notre pension historique dispose de nouvelles chambres rénovées, tout en conservant son charme colonial d'origine." }
            },
            {
                "category": { "pt": "Excursões", "en": "Excursions", "fr": "Excursions" },
                "date": "20 Março 2026",
                "author": "Equipa ZebraTravel",
                "image": "images/resource/news-1.jpg",
                "layout": "top",
                "title": { "pt": "Época alta de mergulho começa em Abril", "en": "Diving high season starts in April", "fr": "La haute saison de plongée débute en avril" },
                "description": { "pt": "Prepare-se para explorar os fundos marinhos de Cabo Verde com as nossas excursões de mergulho e pesca desportiva.", "en": "Get ready to explore Cape Verde's seabed with our diving and sport fishing excursions.", "fr": "Préparez-vous à explorer les fonds marins du Cap-Vert avec nos excursions de plongée et de pêche sportive." }
            }
        ]
        $j$::jsonb, true),
        '{home,groupTravel,items}', $j$
        [
            {
                "duration": "5 days",
                "location": "Pico Maior, Vulcão",
                "price": "$75",
                "link": "#",
                "imageUrl": "/images/background/image-2.jpg",
                "title": { "pt": "Grupo para Vulcão", "en": "Volcano Group Trip", "fr": "Groupe pour le Volcan" },
                "description": { "pt": "Uma jornada cansativa e difícil, com muitos obstáculos geológicos e um sol escaldante.", "en": "A tiring and challenging journey, with many geological obstacles and scorching sun.", "fr": "Un voyage épuisant et difficile, avec de nombreux obstacles géologiques et un soleil brûlant." }
            },
            {
                "duration": "5 days",
                "location": "Dentro Salinas",
                "price": "$70",
                "link": "#",
                "imageUrl": "/images/background/image-2.jpg",
                "title": { "pt": "Grupo para Salinas", "en": "Salinas Group Trip", "fr": "Groupe pour Salinas" },
                "description": { "pt": "Flora e fauna endémica da salina, e a mini floresta das áreas mais verdes do Fogo.", "en": "Endemic flora and fauna of the salt flats, and the mini forest in the greenest areas of Fogo.", "fr": "Flore et faune endémiques du marais salant, et la mini-forêt des zones les plus vertes de Fogo." }
            }
        ]
        $j$::jsonb, true)
WHERE content_key = 'site';
