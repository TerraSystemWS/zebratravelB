package cv.terrasystem.zebratravelb.invoice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;

// Assina cada fatura com HMAC-SHA256 usando uma chave que só existe no servidor
// (app.invoice.signing-secret) — o código impresso no PDF e o QR code que aponta para a página
// pública de verificação só podem ser produzidos por quem tiver esta chave. Nem um ADMIN/AGENTE
// com acesso total ao painel consegue forjar uma fatura válida "à mão", nem alterar os valores
// de uma já emitida sem a assinatura deixar de bater certo — mesma ideia de "chave que só o
// servidor conhece" já usada para os tokens JWT (app.jwt.secret).
@Service
public class InvoiceSigningService {

    private final SecretKeySpec key;

    public InvoiceSigningService(@Value("${app.invoice.signing-secret}") String secret) {
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    // Payload determinístico a partir de dados que nunca mudam depois de a fatura ser emitida:
    // número do documento (já único por si só), origem, total, moeda e cliente. Propositadamente
    // sem createdAt — evita qualquer risco de desvio de milissegundos entre o valor usado para
    // assinar e o valor que o @PrePersist realmente grava.
    public String sign(Invoice invoice) {
        String payload = String.join("|",
                invoice.documentNumber(),
                invoice.getSourceType(),
                String.valueOf(invoice.getSourceId()),
                invoice.getTotalAmount().toPlainString(),
                invoice.getCurrency(),
                invoice.getCustomerName(),
                invoice.getCustomerNif() != null ? invoice.getCustomerNif() : "");
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(raw);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Erro ao assinar a fatura", e);
        }
    }

    public boolean matches(Invoice invoice, String signature) {
        return signature != null && sign(invoice).equalsIgnoreCase(signature);
    }
}
