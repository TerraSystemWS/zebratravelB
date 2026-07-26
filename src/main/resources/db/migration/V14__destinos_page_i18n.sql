-- Adds i18n content for the "Destinos Populares" header on the public /destinos page.

UPDATE site_content
SET content_value =
    jsonb_set(content_value,
        '{destinosPage}', $j$
        {
            "title": { "pt": "Destinos Populares", "en": "Popular Destinations", "fr": "Destinations Populaires" },
            "text": { "pt": "Explore uma seleção dos melhores destinos ao redor do mundo. De praias paradisíacas a montanhas deslumbrantes, temos opções para todos os estilos de viagem. Prepare-se para criar memórias inesquecíveis em sua próxima aventura!", "en": "Explore a selection of the best destinations around the world. From paradise beaches to stunning mountains, we have options for every travel style. Get ready to create unforgettable memories on your next adventure!", "fr": "Découvrez une sélection des meilleures destinations dans le monde. Des plages paradisiaques aux montagnes magnifiques, nous avons des options pour tous les styles de voyage. Préparez-vous à créer des souvenirs inoubliables lors de votre prochaine aventure !" }
        }
        $j$::jsonb, true)
WHERE content_key = 'site';
