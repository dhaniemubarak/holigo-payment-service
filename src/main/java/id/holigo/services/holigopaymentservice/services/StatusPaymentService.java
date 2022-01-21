package id.holigo.services.holigopaymentservice.services;

import id.holigo.services.common.model.PaymentStatusEnum;

public interface StatusPaymentService {
    String getStatusMessage(PaymentStatusEnum statusPaymentEnum);
}
