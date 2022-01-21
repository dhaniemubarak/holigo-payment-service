package id.holigo.services.holigopaymentservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import id.holigo.services.common.model.PaymentStatusEnum;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class StatusPaymentServiceImpl implements StatusPaymentService {
    // WAITING_PAYMENT, VERIFYING_PAYMENT, PAYMENT_FAILED, PAID, PAYMENT_CANCELED,
    // PAYMENT_EXPIRED, PROCESS_REFUND,
    // WAITING_REFUND, REFUNDED
    @Autowired
    private final MessageSource messageSource;

    @Override
    public String getStatusMessage(PaymentStatusEnum statusPaymentEnum) {
        String message = null;
        switch (statusPaymentEnum.toString()) {
            case "WAITING_PAYMENT":
                // Transaksi sedang menunggu pembayaran
                message = messageSource.getMessage("payment.waiting_payment", null, LocaleContextHolder.getLocale());
                break;
            case "VERIFYING_PAYMENT":
                // Pembayaran Anda sedang diverifikasi
                message = messageSource.getMessage("payment.verifying_payment", null, LocaleContextHolder.getLocale());
                break;
            case "PAYMENT_FAILED":
                // Pembayaran Anda tidak kami terima
                message = messageSource.getMessage("payment.payment_failed", null, LocaleContextHolder.getLocale());
                break;
            case "PAID":
                // Transaksi sudah di bayar
                message = messageSource.getMessage("payment.paid", null, LocaleContextHolder.getLocale());
                break;
            case "PAYMENT_CANCELED":
                // Transaksi telah dibatalkan
                message = messageSource.getMessage("payment.payment_canceled", null, LocaleContextHolder.getLocale());
                break;
            case "PAYMENT_EXPIRED":
                // Transaksi telah kadaluarsa
                message = messageSource.getMessage("payment.payment_expired", null, LocaleContextHolder.getLocale());
                break;
            case "PROCESS_REFUND":
                // Dana Anda sedang diproses untuk dikembalikan
                message = messageSource.getMessage("payment.process_refund", null, LocaleContextHolder.getLocale());
                break;
            case "WAITING_REFUND":
                // Dana Anda dalam proses pengembalian
                message = messageSource.getMessage("payment.waiting_refund", null, LocaleContextHolder.getLocale());
                break;
            case "REFUNDED":
                // Dana Anda telah dikembalikan.
                message = messageSource.getMessage("payment.refunded", null, LocaleContextHolder.getLocale());
                break;
            default:
                message = "";
                break;
        }
        return message;
    }

}
