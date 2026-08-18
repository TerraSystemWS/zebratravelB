package cv.terrasystem.zebratravelb.security.turnstile;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

// Verificação server-side do Cloudflare Turnstile (POST /conta/login, /conta/registo, contacto,
// newsletter, candidatura — ver dev-notes.md). secret-key vazia (dev local sem conta Cloudflare)
// desliga a verificação em vez de bloquear tudo — mesmo padrão de default permissivo em dev que
// já existe para JWT_SECRET/CORS_ALLOWED_ORIGINS.
@Service
@Slf4j
public class TurnstileService {

    private static final String SITEVERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    private final RestClient restClient = RestClient.create();

    @Value("${app.turnstile.secret-key}")
    private String secretKey;

    public void verify(String token) {
        if (secretKey == null || secretKey.isBlank()) {
            log.warn("TURNSTILE_SECRET_KEY não configurado — a saltar verificação Turnstile (só aceitável em dev local)");
            return;
        }
        if (token == null || token.isBlank()) {
            throw new BadRequestException("Falha na verificação de segurança. Tenta novamente.");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", secretKey);
        form.add("response", token);

        TurnstileVerifyResponse response;
        try {
            response = restClient.post()
                    .uri(SITEVERIFY_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(TurnstileVerifyResponse.class);
        } catch (Exception ex) {
            log.error("Falha ao contactar o Cloudflare Turnstile siteverify", ex);
            throw new BadRequestException("Falha na verificação de segurança. Tenta novamente.");
        }

        if (response == null || !response.success()) {
            throw new BadRequestException("Falha na verificação de segurança. Tenta novamente.");
        }
    }
}
