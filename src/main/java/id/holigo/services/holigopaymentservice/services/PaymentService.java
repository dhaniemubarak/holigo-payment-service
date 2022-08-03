package id.holigo.services.holigopaymentservice.services;

import java.util.UUID;

import javax.jms.JMSException;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.statemachine.StateMachine;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.events.PaymentStatusEvent;

public interface PaymentService {
    Payment createPayment(Payment payment) throws JsonProcessingException, JMSException;

    StateMachine<PaymentStatusEnum, PaymentStatusEvent> paymentHasBeenPaid(UUID id);

    void paymentExpired(UUID id);

    void paymentCanceled(UUID id);

    void cancelPayment(Payment payment);
}
