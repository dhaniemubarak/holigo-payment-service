package id.holigo.services.holigopaymentservice.services;

import java.util.UUID;

import org.springframework.statemachine.StateMachine;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.common.model.TransactionDto;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.domain.PaymentVirtualAccount;
import id.holigo.services.holigopaymentservice.events.PaymentVirtualAccountEvent;

public interface PaymentVirtualAccountService {
    PaymentVirtualAccount createNewVirtualAccount(TransactionDto transactionDto, Payment payment);

    StateMachine<PaymentStatusEnum, PaymentVirtualAccountEvent> paymentHasBeenPaid(UUID id);

}
