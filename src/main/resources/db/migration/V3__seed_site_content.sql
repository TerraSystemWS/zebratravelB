UPDATE site_content SET content_value = $json$
{
    "home": {
        "banner": {
            "subtitle": "Nunca Para de",
            "title": "Explorar",
            "text": "A ZebraTravel oferece uma experiência única em turismo e viagens na Ilha do Fogo. Explore nossa agência de turismo, a pousada Colonial House, excurções e muito mais. Conecte-se com a beleza e a cultura local!",
            "buttonText": "Ver todos os passeios"
        },
        "about": {
            "subtitle": "Sobre a ZebraTravel",
            "title": "Agência de Turismo e Viagens",
            "text": "A ZebraTravel - Turismo e Viagens, localizada na Ilha do Fogo, oferece experiências únicas para quem deseja explorar as belezas naturais e culturais da região. Desde 2014, nossa missão é proporcionar aos nossos clientes momentos inesquecíveis através de passeios, excursões e atividades como pesca desportiva e mergulho.",
            "items": [
                "Acomodações na charmosa Colonial House e Casa Konig.",
                "Atividades de Excurções a volta da Ilha.",
                "Transferes e excursões personalizadas."
            ]
        }
    },
    "footer": {
        "companyInfo": {
            "text": "A ZebraTravel - Turismo e Viagens, Lda. é uma agência de Turismo e Viagens com sede em Alto S.Pedro, São Filipe - Ilha do Fogo. O seu escritório funciona no rés-do-chão de um dos sobrados mais antigos da cidade, que foi restaurado e transformado numa pousada que demos o nome COLONIAL HOUSE."
        },
        "copyright": "Copyright © 2025 ZebraTravel. Todos os direitos reservados."
    },
    "faq": [
        {
            "label": "Todos",
            "faqs": [
                {
                    "question": "Como pagar uma reserva de viagem online?",
                    "answer": "Especificará os danos que uma das partes estará obrigada a fornecer à outra em caso de falha do produto."
                },
                {
                    "question": "Que moeda aceitamos na sua agência?",
                    "answer": "Aceitamos USD, EUR e a moeda local."
                }
            ]
        }
    ],
    "terms": [
        {
            "title": "Isenção de Responsabilidade",
            "text": "A nossa loja compromete-se a fornecer produtos de alta qualidade e garantir que todas as informações fornecidas sejam precisas e completas..."
        },
        {
            "title": "Termos de Pagamento",
            "text": "Os pagamentos pelos produtos e serviços adquiridos devem ser efetuados de acordo com as opções de pagamento oferecidas em nosso site..."
        }
    ]
}
$json$::jsonb
WHERE content_key = 'site';
