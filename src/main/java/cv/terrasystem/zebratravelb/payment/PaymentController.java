package cv.terrasystem.zebratravelb.payment;

import cv.terrasystem.zebratravelb.booking.Booking;
import cv.terrasystem.zebratravelb.booking.BookingRepository;
import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.hotel.HotelReservation;
import cv.terrasystem.zebratravelb.hotel.HotelReservationRepository;
import cv.terrasystem.zebratravelb.invoice.InvoiceService;
import cv.terrasystem.zebratravelb.order.Order;
import cv.terrasystem.zebratravelb.order.OrderItem;
import cv.terrasystem.zebratravelb.order.OrderRepository;
import cv.terrasystem.zebratravelb.product.Product;
import cv.terrasystem.zebratravelb.product.ProductRepository;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import cv.terrasystem.zebratravelb.voucher.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments/vinti4")
@RequiredArgsConstructor
public class PaymentController {

    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String PURCHASE_TRANSACTION_CODE = "1";

    private final OrderRepository orderRepository;
    private final HotelReservationRepository hotelReservationRepository;
    private final BookingRepository bookingRepository;
    private final ProductRepository productRepository;
    private final Vinti4FingerprintService fingerprintService;
    private final VoucherService voucherService;
    private final InvoiceService invoiceService;

    @Value("${app.vinti4.pos-id}")
    private String posId;
    @Value("${app.vinti4.pos-aut-code}")
    private String posAutCode;
    @Value("${app.vinti4.currency}")
    private String currency;
    @Value("${app.vinti4.gateway-url}")
    private String gatewayUrl;
    @Value("${app.vinti4.callback-url}")
    private String callbackUrl;

    @PostMapping("/fields/{orderId}")
    public Map<String, Object> getPaymentFields(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Encomenda não encontrada: " + orderId));
        if (order.getUser() == null || !order.getUser().getId().equals(principal.getId())) {
            throw new NotFoundException("Encomenda não encontrada: " + orderId);
        }
        if (!Order.ONLINE.equals(order.getPaymentMethod())) {
            throw new BadRequestException("Esta encomenda não usa pagamento online");
        }

        String merchantRef = "R" + order.getId() + "-" + UUID.randomUUID().toString().substring(0, 8);
        String merchantSession = "S" + order.getId() + "-" + UUID.randomUUID().toString().substring(0, 8);

        order.setMerchantRef(merchantRef);
        order.setMerchantSession(merchantSession);
        orderRepository.save(order);

        return buildFields(merchantRef, merchantSession, order.getTotalAmount());
    }

    @PostMapping("/hotel-fields/{reservationId}")
    public Map<String, Object> getHotelPaymentFields(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer reservationId) {
        HotelReservation reservation = hotelReservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada: " + reservationId));
        if (reservation.getUser() == null || !reservation.getUser().getId().equals(principal.getId())) {
            throw new NotFoundException("Reserva não encontrada: " + reservationId);
        }
        if (!HotelReservation.ONLINE.equals(reservation.getPaymentMethod())) {
            throw new BadRequestException("Esta reserva não usa pagamento online");
        }

        String merchantRef = "H" + reservation.getId() + "-" + UUID.randomUUID().toString().substring(0, 8);
        String merchantSession = "SH" + reservation.getId() + "-" + UUID.randomUUID().toString().substring(0, 8);

        reservation.setMerchantRef(merchantRef);
        reservation.setMerchantSession(merchantSession);
        hotelReservationRepository.save(reservation);

        return buildFields(merchantRef, merchantSession, reservation.getTotalAmount());
    }

    @PostMapping("/booking-fields/{bookingId}")
    public Map<String, Object> getBookingPaymentFields(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada: " + bookingId));
        if (booking.getUser() == null || !booking.getUser().getId().equals(principal.getId())) {
            throw new NotFoundException("Reserva não encontrada: " + bookingId);
        }
        if (!Booking.ONLINE.equals(booking.getPaymentMethod())) {
            throw new BadRequestException("Esta reserva não usa pagamento online");
        }

        String merchantRef = "B" + booking.getId() + "-" + UUID.randomUUID().toString().substring(0, 8);
        String merchantSession = "SB" + booking.getId() + "-" + UUID.randomUUID().toString().substring(0, 8);

        booking.setMerchantRef(merchantRef);
        booking.setMerchantSession(merchantSession);
        bookingRepository.save(booking);

        return buildFields(merchantRef, merchantSession, booking.getAmount());
    }

    private Map<String, Object> buildFields(String merchantRef, String merchantSession, BigDecimal amount) {
        String timestamp = LocalDateTime.now().format(TS_FORMAT);
        String fingerprint = fingerprintService.generateRequestFingerprint(
                posAutCode, timestamp, amount,
                merchantRef, merchantSession, posId,
                currency, PURCHASE_TRANSACTION_CODE, null, null
        );

        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("transactionCode", PURCHASE_TRANSACTION_CODE);
        fields.put("posID", posId);
        fields.put("merchantRef", merchantRef);
        fields.put("merchantSession", merchantSession);
        fields.put("amount", amount.toPlainString());
        fields.put("currency", currency);
        fields.put("is3DSec", "1");
        fields.put("urlMerchantResponse", callbackUrl);
        fields.put("languageMessages", "pt");
        fields.put("timeStamp", timestamp);
        fields.put("fingerprintversion", "1");
        fields.put("fingerprint", fingerprint);

        return Map.of("postUrl", gatewayUrl, "fields", fields);
    }

    @PostMapping(value = "/callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> callback(@RequestParam Map<String, String> body,
                                            @Value("${app.frontend-url}") String frontendUrl) {
        String messageType = body.getOrDefault("messageType", "");
        boolean success = Map.of(
                "8", true, "10", true, "M", true, "P", true
        ).getOrDefault(messageType, false);

        String merchantRef = body.get("merchantRespMerchantRef");
        String prefix = merchantRef != null && !merchantRef.isEmpty() ? merchantRef.substring(0, 1) : "";

        String redirectPath;
        Integer entityId = null;
        String finalStatus = "FAILED";

        if ("H".equals(prefix)) {
            HotelReservation reservation = hotelReservationRepository.findByMerchantRef(merchantRef).orElse(null);
            if (reservation != null) {
                if (success) {
                    boolean valid = verifyResponseFingerprint(body, reservation.getTotalAmount());
                    reservation.setStatus(valid ? HotelReservation.CONFIRMED : HotelReservation.FAILED);
                } else {
                    reservation.setStatus("true".equals(body.get("UserCancelled")) ? HotelReservation.CANCELLED : HotelReservation.FAILED);
                }
                hotelReservationRepository.save(reservation);
                if (HotelReservation.CANCELLED.equals(reservation.getStatus()) || HotelReservation.FAILED.equals(reservation.getStatus())) {
                    voucherService.releaseForHotelReservation(reservation.getId());
                } else if (HotelReservation.CONFIRMED.equals(reservation.getStatus())) {
                    invoiceService.issueForHotelReservation(reservation);
                }
                entityId = reservation.getId();
                finalStatus = reservation.getStatus();
            }
            redirectPath = "/hotel/checkout/resultado";
        } else if ("B".equals(prefix)) {
            Booking booking = bookingRepository.findByMerchantRef(merchantRef).orElse(null);
            if (booking != null) {
                if (success) {
                    boolean valid = verifyResponseFingerprint(body, booking.getAmount());
                    booking.setStatus(valid ? Booking.CONFIRMED : Booking.FAILED);
                } else {
                    booking.setStatus("true".equals(body.get("UserCancelled")) ? Booking.CANCELLED : Booking.FAILED);
                }
                bookingRepository.save(booking);
                if (Booking.CANCELLED.equals(booking.getStatus()) || Booking.FAILED.equals(booking.getStatus())) {
                    voucherService.releaseForBooking(booking.getId());
                } else if (Booking.CONFIRMED.equals(booking.getStatus())) {
                    invoiceService.issueForBooking(booking);
                }
                entityId = booking.getId();
                finalStatus = booking.getStatus();
            }
            redirectPath = "/excurcoes/checkout/resultado";
        } else {
            Order order = merchantRef != null ? orderRepository.findByMerchantRef(merchantRef).orElse(null) : null;
            if (order != null) {
                String previousStatus = order.getStatus();
                if (success) {
                    boolean valid = verifyResponseFingerprint(body, order.getTotalAmount());
                    order.setStatus(valid ? Order.PAID : Order.FAILED);
                } else {
                    order.setStatus("true".equals(body.get("UserCancelled")) ? Order.CANCELLED : Order.FAILED);
                }
                boolean paymentDidNotGoThrough = !Order.PAID.equals(order.getStatus());
                if (Order.PENDING_PAYMENT.equals(previousStatus) && paymentDidNotGoThrough) {
                    restoreStock(order);
                    voucherService.releaseForOrder(order.getId());
                }
                orderRepository.save(order);
                if (Order.PAID.equals(order.getStatus())) {
                    invoiceService.issueForOrder(order);
                }
                entityId = order.getId();
                finalStatus = order.getStatus();
            }
            redirectPath = "/checkout/resultado";
        }

        String redirectUrl = frontendUrl + redirectPath + "?orderId=" + (entityId != null ? entityId : "") + "&status=" + finalStatus;

        String html = "<!doctype html><html><head><meta http-equiv=\"refresh\" content=\"0;url=" + redirectUrl + "\"></head>"
                + "<body>A processar o pagamento... <a href=\"" + redirectUrl + "\">Continuar</a>"
                + "<script>window.location.href=" + "'" + redirectUrl + "';</script></body></html>";

        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }
    }


    private boolean verifyResponseFingerprint(Map<String, String> body, BigDecimal amount) {
        String messageType = body.getOrDefault("messageType", "");
        String computed = fingerprintService.generateResponseFingerprint(
                posAutCode,
                messageType,
                body.getOrDefault("merchantRespCP", ""),
                body.getOrDefault("merchantRespTid", ""),
                body.getOrDefault("merchantRespMerchantRef", ""),
                body.getOrDefault("merchantRespMerchantSession", ""),
                amount,
                body.getOrDefault("merchantRespMessageID", ""),
                body.getOrDefault("merchantRespPan", ""),
                body.getOrDefault("merchantResp", ""),
                body.getOrDefault("merchantRespTimeStamp", ""),
                body.get("merchantRespReferenceNumber"),
                body.get("merchantRespEntityCode"),
                body.getOrDefault("merchantRespClientReceipt", ""),
                body.getOrDefault("merchantRespAdditionalErrorMessage", ""),
                body.getOrDefault("merchantRespReloadCode", "")
        );
        return computed.equals(body.get("resultFingerPrint"));
    }
}
