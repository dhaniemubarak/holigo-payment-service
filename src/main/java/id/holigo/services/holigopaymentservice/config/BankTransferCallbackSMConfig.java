package id.holigo.services.holigopaymentservice.config;

import java.util.EnumSet;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.BankTransferCallback;
import id.holigo.services.holigopaymentservice.domain.PaymentCallbackStatusEnum;
import id.holigo.services.holigopaymentservice.domain.PaymentBankTransfer;
import id.holigo.services.holigopaymentservice.events.BankTransferStatusEvent;
import id.holigo.services.holigopaymentservice.repositories.BankTransferCallbackRepository;
import id.holigo.services.holigopaymentservice.repositories.PaymentBankTransferRepository;
import id.holigo.services.holigopaymentservice.services.BankTransferCallbackServiceImpl;
import id.holigo.services.holigopaymentservice.services.PaymentBankTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@EnableStateMachineFactory(name = "bankTransferCallbackSMF")
@Configuration
public class BankTransferCallbackSMConfig
        extends StateMachineConfigurerAdapter<PaymentCallbackStatusEnum, BankTransferStatusEvent> {

    @Autowired
    private final BankTransferCallbackRepository bankTransferCallbackRepository;

    @Autowired
    private final PaymentBankTransferRepository paymentBankTransferRepository;

    @Autowired
    private final PaymentBankTransferService paymentBankTransferService;

    @Override
    public void configure(StateMachineStateConfigurer<PaymentCallbackStatusEnum, BankTransferStatusEvent> states)
            throws Exception {
        states.withStates().initial(PaymentCallbackStatusEnum.RECEIVED)
                .states(EnumSet.allOf(PaymentCallbackStatusEnum.class))
                .end(PaymentCallbackStatusEnum.ISSUED)
                .end(PaymentCallbackStatusEnum.TRANSACTION_NOT_FOUND)
                .end(PaymentCallbackStatusEnum.ISSUED_FAILED);
    }

    @Override
    public void configure(
            StateMachineTransitionConfigurer<PaymentCallbackStatusEnum, BankTransferStatusEvent> transitions)
            throws Exception {
        transitions.withExternal().source(PaymentCallbackStatusEnum.RECEIVED)
                .target(PaymentCallbackStatusEnum.RECEIVED)
                .action(receivedAction())
                .event(BankTransferStatusEvent.FIND_TRANSACTION)
                .and()
                .withExternal().source(PaymentCallbackStatusEnum.RECEIVED)
                .target(PaymentCallbackStatusEnum.PROCESS_ISSUED)
                .action(processIssuedAction())
                .event(BankTransferStatusEvent.PROCESS_ISSUED)
                .and()
                .withExternal().source(PaymentCallbackStatusEnum.RECEIVED)
                .target(PaymentCallbackStatusEnum.TRANSACTION_NOT_FOUND)
                .action(transactionNotFoundAction())
                .event(BankTransferStatusEvent.TRANSACTION_NOT_FOUND)
                .and()
                .withExternal().source(PaymentCallbackStatusEnum.PROCESS_ISSUED)
                .target(PaymentCallbackStatusEnum.ISSUED)
                .event(BankTransferStatusEvent.ISSUED)
                .and()
                .withExternal().source(PaymentCallbackStatusEnum.PROCESS_ISSUED)
                .target(PaymentCallbackStatusEnum.ISSUED_FAILED)
                .event(BankTransferStatusEvent.ISSUED_FAILED);
    }

    @Override
    public void configure(
            StateMachineConfigurationConfigurer<PaymentCallbackStatusEnum, BankTransferStatusEvent> config)
            throws Exception {
        StateMachineListenerAdapter<PaymentCallbackStatusEnum, BankTransferStatusEvent> adapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<PaymentCallbackStatusEnum, BankTransferStatusEvent> from,
                                     State<PaymentCallbackStatusEnum, BankTransferStatusEvent> to) {
                log.info(String.format("stateChange(from: %s, to %s)", from, to));
            }
        };
        config.withConfiguration().listener(adapter);
    }

    public Action<PaymentCallbackStatusEnum, BankTransferStatusEvent> receivedAction() {
        return context -> {
            BankTransferCallback bankTransferCallback = bankTransferCallbackRepository
                    .getById(Long.valueOf(
                            context.getMessageHeader(
                                            BankTransferCallbackServiceImpl.BANK_TRANSFER_CALLBACK_HEADER)
                                    .toString()));

            Optional<PaymentBankTransfer> fetchPaymentBankTransfer = paymentBankTransferRepository
                    .findByPaymentServiceIdAndBillAmountAndStatus(
                            bankTransferCallback.getPaymentMerchant(),
                            bankTransferCallback.getAmount(),
                            PaymentStatusEnum.WAITING_PAYMENT);
            if (fetchPaymentBankTransfer.isPresent()) {
                context.getStateMachine()
                        .sendEvent(MessageBuilder
                                .withPayload(BankTransferStatusEvent.PROCESS_ISSUED)
                                .setHeader(BankTransferCallbackServiceImpl.BANK_TRANSFER_CALLBACK_HEADER,
                                        bankTransferCallback.getId())
                                .build());
                PaymentBankTransfer paymentBankTransfer = fetchPaymentBankTransfer.get();
                paymentBankTransfer.setCallbackId(bankTransferCallback.getId());
                paymentBankTransferRepository.save(paymentBankTransfer);

                paymentBankTransferService.paymentHasBeenPaid(paymentBankTransfer.getId());

            } else {
                context.getStateMachine()
                        .sendEvent(MessageBuilder.withPayload(
                                        BankTransferStatusEvent.TRANSACTION_NOT_FOUND)
                                .setHeader(BankTransferCallbackServiceImpl.BANK_TRANSFER_CALLBACK_HEADER,
                                        bankTransferCallback.getId())
                                .build());

            }
        };
    }

    public Action<PaymentCallbackStatusEnum, BankTransferStatusEvent> processIssuedAction() {
        return context -> {
            BankTransferCallback bankTransferCallback = bankTransferCallbackRepository.getById(Long.valueOf(
                    context.getMessageHeader(
                                    BankTransferCallbackServiceImpl.BANK_TRANSFER_CALLBACK_HEADER)
                            .toString()));

            // context.getStateMachine()
            // .sendEvent(MessageBuilder
            // .withPayload(BankTransferStatusEvent.TRANSACTION_NOT_FOUND)
            // .setHeader(BankTransferCallbackServiceImpl.BANK_TRANSFER_CALLBACK_HEADER,
            // context.getMessageHeader(
            // BankTransferCallbackServiceImpl.BANK_TRANSFER_CALLBACK_HEADER))
            // .build());
        };
    }

    public Action<PaymentCallbackStatusEnum, BankTransferStatusEvent> transactionNotFoundAction() {
        return context -> {
            BankTransferCallback bankTransferCallback = bankTransferCallbackRepository.getById(Long.valueOf(
                    context.getMessageHeader(
                                    BankTransferCallbackServiceImpl.BANK_TRANSFER_CALLBACK_HEADER)
                            .toString()));
        };
    }
}
