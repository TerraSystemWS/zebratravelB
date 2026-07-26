-- Adds i18n content for the homepage sections and the FAQ page header that were still
-- hardcoded in PT. Uses jsonb_set with create_missing=true on each path so the existing
-- home.banner / home.about / footer / faq / terms content (V9) is left untouched.

UPDATE site_content
SET content_value =
    jsonb_set(
    jsonb_set(
    jsonb_set(
    jsonb_set(
    jsonb_set(
    jsonb_set(
    jsonb_set(
    jsonb_set(
    jsonb_set(content_value,
        '{home,wwedo}', $j$
        {
            "subtitle": { "pt": "Somos incríveis", "en": "We are amazing", "fr": "Nous sommes incroyables" },
            "title": { "pt": "Porque escolher a ZebraTravel", "en": "Why Choose ZebraTravel", "fr": "Pourquoi Choisir ZebraTravel" },
            "items": [
                {
                    "title": { "pt": "Destinos Diversificados", "en": "Diverse Destinations", "fr": "Destinations Diversifiées" },
                    "text": { "pt": "Oferecemos uma variedade de destinos, desde paisagens deslumbrantes a alojamento de luxo.", "en": "We offer a variety of destinations, from stunning landscapes to luxury accommodation.", "fr": "Nous proposons une variété de destinations, des paysages magnifiques à l'hébergement de luxe." }
                },
                {
                    "title": { "pt": "Excelente Custo-Benefício", "en": "Excellent Value for Money", "fr": "Excellent Rapport Qualité-Prix" },
                    "text": { "pt": "Oferecemos serviços de viagem, hotéis e restaurantes com qualidade a preços acessíveis.", "en": "We offer quality travel, hotel and restaurant services at affordable prices.", "fr": "Nous proposons des services de voyage, d'hôtellerie et de restauration de qualité à des prix abordables." }
                },
                {
                    "title": { "pt": "Lugares Maravilhosos", "en": "Wonderful Places", "fr": "Endroits Merveilleux" },
                    "text": { "pt": "Explore locais incríveis e experiências inesquecíveis com a nossa agência de viagens.", "en": "Explore amazing places and unforgettable experiences with our travel agency.", "fr": "Découvrez des lieux incroyables et des expériences inoubliables avec notre agence de voyages." }
                },
                {
                    "title": { "pt": "Reserva Rápida", "en": "Quick Booking", "fr": "Réservation Rapide" },
                    "text": { "pt": "Facilitamos a sua experiência de reserva com um sistema simples e rápido.", "en": "We make your booking experience easy with a simple and fast system.", "fr": "Nous facilitons votre expérience de réservation avec un système simple et rapide." }
                },
                {
                    "title": { "pt": "Equipa de Suporte", "en": "Support Team", "fr": "Équipe de Support" },
                    "text": { "pt": "Temos uma equipa dedicada para apoiar os nossos clientes em todas as etapas da viagem.", "en": "We have a dedicated team to support our customers at every stage of the trip.", "fr": "Nous avons une équipe dédiée pour accompagner nos clients à chaque étape du voyage." }
                },
                {
                    "title": { "pt": "Viagens com Paixão", "en": "Travel with Passion", "fr": "Voyager avec Passion" },
                    "text": { "pt": "Com a ZebraTravel, as viagens são feitas com paixão e dedicação para garantir a sua satisfação.", "en": "With ZebraTravel, trips are made with passion and dedication to ensure your satisfaction.", "fr": "Avec ZebraTravel, les voyages sont réalisés avec passion et dévouement pour garantir votre satisfaction." }
                }
            ]
        }
        $j$::jsonb, true),
        '{home,topdestinos}', $j$
        {
            "subtitle": { "pt": "Destinos em Alta", "en": "Trending Now", "fr": "Tendance du Moment" },
            "title": { "pt": "Destinos em Tendência", "en": "Trending Destinations", "fr": "Destinations en Vogue" }
        }
        $j$::jsonb, true),
        '{home,products}', $j$
        {
            "subtitle": { "pt": "Comprar agora", "en": "Shop Now", "fr": "Achetez Maintenant" },
            "title": { "pt": "Produtos em destaques", "en": "Featured Products", "fr": "Produits en Vedette" },
            "seeMore": { "pt": "Ver todos os produtos", "en": "View all products", "fr": "Voir tous les produits" }
        }
        $j$::jsonb, true),
        '{home,groupTravel}', $j$
        {
            "seeDetails": { "pt": "Ver Detalhes", "en": "View Details", "fr": "Voir Détails" }
        }
        $j$::jsonb, true),
        '{home,testimonials}', $j$
        {
            "subtitle": { "pt": "Revisão e Depoimento", "en": "Reviews & Testimonials", "fr": "Avis et Témoignages" },
            "title": { "pt": "Principais avaliações sobre ZebraTravel", "en": "Top reviews about ZebraTravel", "fr": "Meilleurs avis sur ZebraTravel" }
        }
        $j$::jsonb, true),
        '{home,newsSection}', $j$
        {
            "subtitle": { "pt": "ZebraTravel Top News", "en": "ZebraTravel Top News", "fr": "Actualités ZebraTravel" },
            "title": { "pt": "Ultimas Novidades", "en": "Latest News", "fr": "Dernières Actualités" }
        }
        $j$::jsonb, true),
        '{home,subscribe}', $j$
        {
            "subtitle": { "pt": "Newsletter", "en": "Newsletter", "fr": "Newsletter" },
            "title": { "pt": "SUBSCREVA AGORA", "en": "SUBSCRIBE NOW", "fr": "ABONNEZ-VOUS MAINTENANT" },
            "text": { "pt": "Fique por dentro das novidades! Inscreva-se na nossa newsletter e receba as últimas atualizações sobre nossas promoções, eventos e muito mais diretamente no seu e-mail!", "en": "Stay up to date! Sign up for our newsletter and get the latest news on our promotions, events and much more straight to your inbox!", "fr": "Restez informé ! Inscrivez-vous à notre newsletter et recevez les dernières actualités sur nos promotions, événements et bien plus encore directement dans votre boîte mail !" },
            "placeholder": { "pt": "Endereço de E-mail", "en": "Email Address", "fr": "Adresse E-mail" },
            "button": { "pt": "inscrever", "en": "subscribe", "fr": "s'inscrire" },
            "successMessage": { "pt": "Obrigado Por Subscrever!", "en": "Thank You For Subscribing!", "fr": "Merci de Vous Être Abonné !" },
            "errorMessage": { "pt": "Por Favor, entre com um email valido.", "en": "Please enter a valid email.", "fr": "Veuillez saisir un e-mail valide." }
        }
        $j$::jsonb, true),
        '{home,tourspop}', $j$
        {
            "subtitle": { "pt": "Explore as belezas da Ilha do Fogo", "en": "Explore the beauty of Fogo Island", "fr": "Découvrez la beauté de l'île de Fogo" },
            "title": { "pt": "Excursões Mais Populares", "en": "Most Popular Excursions", "fr": "Excursions les Plus Populaires" },
            "daysLabel": { "pt": "dias", "en": "days", "fr": "jours" },
            "reviewsLabel": { "pt": "Avaliações", "en": "Reviews", "fr": "Avis" },
            "seeDetails": { "pt": "Ver Detalhes", "en": "View Details", "fr": "Voir Détails" }
        }
        $j$::jsonb, true),
        '{faqHeader}', $j$
        {
            "title": { "pt": "Perguntas Frequentes", "en": "Frequently Asked Questions", "fr": "Questions Fréquentes" },
            "text": { "pt": "Desde 2014, ajudámos mais de 500.000 pessoas de todas as idades a desfrutar da melhor experiência ao ar livre das suas vidas. Seja para um dia ou para umas férias de duas semanas, perto de casa ou num país estrangeiro.", "en": "Since 2014, we've helped over 500,000 people of all ages enjoy the best outdoor experience of their lives. Whether for a day trip or a two-week holiday, close to home or abroad.", "fr": "Depuis 2014, nous avons aidé plus de 500 000 personnes de tous âges à vivre la meilleure expérience en plein air de leur vie. Que ce soit pour une journée ou des vacances de deux semaines, près de chez vous ou à l'étranger." }
        }
        $j$::jsonb, true)
WHERE content_key = 'site';
