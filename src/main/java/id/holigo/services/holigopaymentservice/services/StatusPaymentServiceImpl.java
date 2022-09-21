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
        return switch (statusPaymentEnum) {
            case SELECTING_PAYMENT ->
                    messageSource.getMessage("payment.selecting_payment", null, LocaleContextHolder.getLocale());
            case WAITING_PAYMENT ->
                    messageSource.getMessage("payment.waiting_payment", null, LocaleContextHolder.getLocale());
            case VERIFYING_PAYMENT ->
                    messageSource.getMessage("payment.verifying_payment", null, LocaleContextHolder.getLocale());
            case PAYMENT_FAILED ->
                    messageSource.getMessage("payment.payment_failed", null, LocaleContextHolder.getLocale());
            case PAID -> messageSource.getMessage("payment.paid", null, LocaleContextHolder.getLocale());
            case PAYMENT_CANCELED ->
                    messageSource.getMessage("payment.payment_canceled", null, LocaleContextHolder.getLocale());
            case PAYMENT_EXPIRED ->
                    messageSource.getMessage("payment.payment_expired", null, LocaleContextHolder.getLocale());
            case PROCESS_REFUND ->
                    messageSource.getMessage("payment.process_refund", null, LocaleContextHolder.getLocale());
            case WAITING_REFUND ->
                    messageSource.getMessage("payment.waiting_refund", null, LocaleContextHolder.getLocale());
            case REFUNDED -> messageSource.getMessage("payment.refunded", null, LocaleContextHolder.getLocale());
        };
    }

}
