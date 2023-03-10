package id.holigo.services.holigopaymentservice.config;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import id.holigo.services.holigopaymentservice.domain.PaymentCallbackStatusEnum;
import id.holigo.services.holigopaymentservice.events.BankTransferStatusEvent;

@SpringBootTest
public class BankTranasferCallbackSMConfigTest {

    @Autowired
    StateMachineFactory<PaymentCallbackStatusEnum, BankTransferStatusEvent> factory;

    @Test
    void testNewStateMachine() {
        StateMachine<PaymentCallbackStatusEnum, BankTransferStatusEvent> sm = factory.getStateMachine(UUID.randomUUID());
        sm.start();
        System.out.println(sm.getState().toString());
        sm.sendEvent(BankTransferStatusEvent.FIND_TRANSACTION);
        System.out.println(sm.getState().toString());
        sm.sendEvent(BankTransferStatusEvent.TRANSACTION_NOT_FOUND);
        System.out.println(sm.getState().toString());
        sm.sendEvent(BankTransferStatusEvent.PROCESS_ISSUED);
        System.out.println(sm.getState().toString());
        sm.sendEvent(BankTransferStatusEvent.ISSUED);
        System.out.println(sm.getState().toString());
    }
}
