package cv.terrasystem.zebratravelb.payment;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Port of the fingerprint generation from SISP/Vinti4's official NodeJS_vbv2 sample
 * (GerarFingerPrintEnvio / GerarFingerPrintRespostaBemSucedida). The posAutCode (merchant
 * secret) must never leave the backend - it is only ever used here to compute a hash.
 */
@Service
public class Vinti4FingerprintService {

    private String sha512Base64(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private String amountToInt(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(1000))
                .setScale(0, RoundingMode.HALF_UP)
                .toBigInteger()
                .toString();
    }

    public String generateRequestFingerprint(
            String posAutCode, String timestamp, BigDecimal amount,
            String merchantRef, String merchantSession, String posId,
            String currency, String transactionCode,
            String entityCode, String referenceNumber
    ) {
        StringBuilder toHash = new StringBuilder()
                .append(sha512Base64(posAutCode))
                .append(timestamp)
                .append(amountToInt(amount))
                .append(merchantRef.trim())
                .append(merchantSession.trim())
                .append(posId.trim())
                .append(currency.trim())
                .append(transactionCode.trim());

        if (entityCode != null && !entityCode.isBlank()) {
            toHash.append(Long.parseLong(entityCode.trim()));
        }
        if (referenceNumber != null && !referenceNumber.isBlank()) {
            toHash.append(Long.parseLong(referenceNumber.trim()));
        }

        return sha512Base64(toHash.toString());
    }

    public String generateResponseFingerprint(
            String posAutCode, String messageType, String clearingPeriod,
            String transactionId, String merchantReference, String merchantSession,
            BigDecimal amount, String messageId, String pan,
            String merchantResponse, String timestamp, String reference,
            String entity, String clientReceipt, String additionalErrorMessage,
            String reloadCode
    ) {
        long referenceNum = blankToZero(reference);
        long entityNum = blankToZero(entity);

        StringBuilder toHash = new StringBuilder()
                .append(sha512Base64(posAutCode))
                .append(messageType)
                .append(clearingPeriod)
                .append(transactionId)
                .append(merchantReference)
                .append(merchantSession)
                .append(amountToInt(amount))
                .append(messageId.trim())
                .append(pan.trim())
                .append(merchantResponse.trim())
                .append(timestamp)
                .append(referenceNum)
                .append(entityNum)
                .append(clientReceipt.trim())
                .append(additionalErrorMessage.trim())
                .append(reloadCode.trim());

        return sha512Base64(toHash.toString());
    }

    private long blankToZero(String value) {
        if (value == null || value.isBlank()) return 0L;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
