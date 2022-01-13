package id.holigo.services.holigopaymentservice.services;

import java.util.List;

import id.holigo.services.holigopaymentservice.domain.PaymentMethod;

public interface PaymentMethodService {
    List<PaymentMethod> getShowPaymentMethod();
}
