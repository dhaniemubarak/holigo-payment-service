package id.holigo.services.holigopaymentservice.services;

import java.util.UUID;

import javax.jms.JMSException;

import com.fasterxml.jackson.core.JsonProcessingException;

import id.holigo.services.common.model.AccountBalanceDto;
import id.holigo.services.common.model.TransactionDto;
import org.springframework.statemachine.StateMachine;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.events.PaymentStatusEvent;

public interface PaymentService {
    Payment createPayment(Payment payment, TransactionDto transactionDto, AccountBalanceDto accountBalanceDto) throws JsonProcessingException, JMSException;

    StateMachine<PaymentStatusEnum, PaymentStatusEvent> paymentHasBeenPaid(UUID id);

    StateMachine<PaymentStatusEnum, PaymentStatusEvent> paymentExpired(UUID id);

    StateMachine<PaymentStatusEnum, PaymentStatusEvent> refundPayment(UUID id);

    StateMachine<PaymentStatusEnum, PaymentStatusEvent> paymentCanceled(UUID id);

    void cancelPayment(Payment payment);
}
