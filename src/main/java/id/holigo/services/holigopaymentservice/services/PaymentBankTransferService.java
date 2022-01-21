package id.holigo.services.holigopaymentservice.services;

import java.util.UUID;

import org.springframework.statemachine.StateMachine;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.events.PaymentBankTransferEvent;

public interface PaymentBankTransferService {

    StateMachine<PaymentStatusEnum, PaymentBankTransferEvent> paymentHasBeenPaid(UUID id);
}
