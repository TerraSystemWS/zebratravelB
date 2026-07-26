-- Adds i18n content for the page headers of Loja, Excursões, Hotel (list), Contacto,
-- Galeria and Equipa, which were still hardcoded in PT. Uses jsonb_set with
-- create_missing=true on each top-level path so existing content is left untouched.

UPDATE site_content
SET content_value =
    jsonb_set(
    jsonb_set(
    jsonb_set(
    jsonb_set(
    jsonb_set(
    jsonb_set(content_value,
        '{loja}', $j$
        {
            "title": { "pt": "Produtos em Destaque", "en": "Featured Products", "fr": "Produits en Vedette" },
            "text": { "pt": "Desde 2014, ajudámos mais de 500.000 pessoas de todas as idades a viver a melhor experiência ao ar livre das suas vidas. Seja para um dia ou umas férias de duas semanas, perto de casa ou num destino internacional.", "en": "Since 2014, we've helped over 500,000 people of all ages experience the best outdoor adventure of their lives. Whether for a day trip or a two-week holiday, close to home or abroad.", "fr": "Depuis 2014, nous avons aidé plus de 500 000 personnes de tous âges à vivre la meilleure expérience en plein air de leur vie. Que ce soit pour une journée ou des vacances de deux semaines, près de chez vous ou à l'étranger." },
            "seeMore": { "pt": "Carregar Mais......", "en": "Load More......", "fr": "Charger Plus......" }
        }
        $j$::jsonb, true),
        '{excursoes}', $j$
        {
            "title": { "pt": "Melhores trilhas para você com a ZebraTravel", "en": "The best trails for you with ZebraTravel", "fr": "Les meilleurs sentiers pour vous avec ZebraTravel" },
            "text": { "pt": "Descubra as trilhas mais incríveis com a ZebraTravel! Conecte-se com a natureza, explore paisagens deslumbrantes e viva aventuras inesquecíveis, seja em uma caminhada de um dia ou em uma expedição prolongada.", "en": "Discover the most amazing trails with ZebraTravel! Connect with nature, explore stunning landscapes and live unforgettable adventures, whether on a day hike or an extended expedition.", "fr": "Découvrez les sentiers les plus incroyables avec ZebraTravel ! Connectez-vous à la nature, explorez des paysages magnifiques et vivez des aventures inoubliables, que ce soit lors d'une randonnée d'une journée ou d'une expédition prolongée." }
        }
        $j$::jsonb, true),
        '{hotelList}', $j$
        {
            "title": { "pt": "Os Nossos Hotéis", "en": "Our Hotels", "fr": "Nos Hôtels" },
            "text": { "pt": "Escolha o hotel ideal para a sua estadia em Cabo Verde e reserve o seu quarto online.", "en": "Choose the ideal hotel for your stay in Cape Verde and book your room online.", "fr": "Choisissez l'hôtel idéal pour votre séjour au Cap-Vert et réservez votre chambre en ligne." },
            "empty": { "pt": "Ainda não há hotéis disponíveis.", "en": "No hotels available yet.", "fr": "Aucun hôtel disponible pour le moment." }
        }
        $j$::jsonb, true),
        '{contact}', $j$
        {
            "formTitle": { "pt": "Como posso ajudar?", "en": "How can I help?", "fr": "Comment puis-je vous aider ?" },
            "formText": { "pt": "Entre em contato conosco e prepare-se para uma melhor experiência de aventura em sua vida. Basta procurar oportunidades de estar com a natureza.", "en": "Get in touch with us and get ready for a better adventure experience in your life. Just look for opportunities to be with nature.", "fr": "Contactez-nous et préparez-vous à vivre une meilleure expérience d'aventure. Il vous suffit de chercher des occasions d'être en contact avec la nature." },
            "findUsTitle": { "pt": "Encontre o nosso escritório no mapa", "en": "Find our office on the map", "fr": "Trouvez notre bureau sur la carte" },
            "findUsText": { "pt": "Contacte-nos e prepare-se para uma melhor experiência de aventura em toda a sua vida. Basta procurar uma oportunidade de estar com a natureza.", "en": "Contact us and get ready for a better adventure experience throughout your life. Just look for an opportunity to be with nature.", "fr": "Contactez-nous et préparez-vous à vivre une meilleure expérience d'aventure. Il vous suffit de chercher une occasion d'être en contact avec la nature." }
        }
        $j$::jsonb, true),
        '{galeria}', $j$
        {
            "title": { "pt": "Veja Nossa Galeria Mais Recente", "en": "See Our Latest Gallery", "fr": "Découvrez Notre Dernière Galerie" },
            "text": { "pt": "Explorando o mundo com conforto", "en": "Exploring the world with comfort", "fr": "Explorer le monde avec confort" }
        }
        $j$::jsonb, true),
        '{team}', $j$
        {
            "title": { "pt": "Conheça os Nossos Guias Turísticos", "en": "Meet Our Tour Guides", "fr": "Découvrez Nos Guides Touristiques" }
        }
        $j$::jsonb, true)
WHERE content_key = 'site';
