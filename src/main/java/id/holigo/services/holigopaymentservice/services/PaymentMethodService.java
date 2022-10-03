package id.holigo.services.holigopaymentservice.services;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import id.holigo.services.holigopaymentservice.domain.PaymentMethod;

import javax.jms.JMSException;

public interface PaymentMethodService {
    List<PaymentMethod> getShowPaymentMethod(UUID transactionId) throws JMSException, JsonProcessingException;
}
