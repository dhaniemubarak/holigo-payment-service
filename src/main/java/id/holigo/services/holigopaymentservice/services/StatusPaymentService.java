package id.holigo.services.holigopaymentservice.services;

import id.holigo.services.common.model.StatusPaymentEnum;

public interface StatusPaymentService {
    String getStatusMessage(StatusPaymentEnum statusPaymentEnum);
}
