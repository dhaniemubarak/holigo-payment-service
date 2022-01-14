package id.holigo.services.holigopaymentservice.services;

import javax.jms.JMSException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import id.holigo.services.holigopaymentservice.domain.Payment;

public interface PaymentService {
    Payment createPayment(Payment payment)  throws JsonMappingException, JsonProcessingException, JMSException ;
}
