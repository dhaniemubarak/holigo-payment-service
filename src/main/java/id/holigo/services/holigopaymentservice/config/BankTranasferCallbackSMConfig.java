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
import id.holigo.services.holigopaymentservice.domain.BankTransferStatusEnum;
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
public class BankTranasferCallbackSMConfig
                extends StateMachineConfigurerAdapter<BankTransferStatusEnum, BankTransferStatusEvent> {

        @Autowired
        private final BankTransferCallbackRepository bankTransferCallbackRepository;

        @Autowired
        private final PaymentBankTransferRepository paymentBankTransferRepository;

        @Autowired
        private final PaymentBankTransferService paymentBankTransferService;

        @Override
        public void configure(StateMachineStateConfigurer<BankTransferStatusEnum, BankTransferStatusEvent> states)
                        throws Exception {
                states.withStates().initial(BankTransferStatusEnum.RECEIVED)
                                .states(EnumSet.allOf(BankTransferStatusEnum.class))
                                .end(BankTransferStatusEnum.ISSUED)
                                .end(BankTransferStatusEnum.TRANSACTION_NOT_FOUND)
                                .end(BankTransferStatusEnum.ISSUED_FAILED);
        }

        @Override
        public void configure(
                        StateMachineTransitionConfigurer<BankTransferStatusEnum, BankTransferStatusEvent> transitions)
                        throws Exception {
                transitions.withExternal().source(BankTransferStatusEnum.RECEIVED)
                                .target(BankTransferStatusEnum.RECEIVED)
                                .action(receivedAction())
                                .event(BankTransferStatusEvent.FIND_TRANSACTION)
                                .and()
                                .withExternal().source(BankTransferStatusEnum.RECEIVED)
                                .target(BankTransferStatusEnum.PROCESS_ISSUED)
                                .action(processIssuedAction())
                                .event(BankTransferStatusEvent.PROCESS_ISSUED)
                                .and()
                                .withExternal().source(BankTransferStatusEnum.RECEIVED)
                                .target(BankTransferStatusEnum.TRANSACTION_NOT_FOUND)
                                .action(transactionNotFoundAction())
                                .event(BankTransferStatusEvent.TRANSACTION_NOT_FOUND)
                                .and()
                                .withExternal().source(BankTransferStatusEnum.PROCESS_ISSUED)
                                .target(BankTransferStatusEnum.ISSUED)
                                .event(BankTransferStatusEvent.ISSUED)
                                .and()
                                .withExternal().source(BankTransferStatusEnum.PROCESS_ISSUED)
                                .target(BankTransferStatusEnum.ISSUED_FAILED)
                                .event(BankTransferStatusEvent.ISSUED_FAILED);
        }

        @Override
        public void configure(
                        StateMachineConfigurationConfigurer<BankTransferStatusEnum, BankTransferStatusEvent> config)
                        throws Exception {
                StateMachineListenerAdapter<BankTransferStatusEnum, BankTransferStatusEvent> adapter = new StateMachineListenerAdapter<>() {
                        @Override
                        public void stateChanged(State<BankTransferStatusEnum, BankTransferStatusEvent> from,
                                        State<BankTransferStatusEnum, BankTransferStatusEvent> to) {
                                log.info(String.format("stateChange(from: %s, to %s)", from, to));
                        }
                };
                config.withConfiguration().listener(adapter);
        }

        public Action<BankTransferStatusEnum, BankTransferStatusEvent> receivedAction() {
                return context -> {
                        // find

                        log.info("receivedAction was called");
                        log.info("header -> {}",
                                        context.getMessageHeader(
                                                        BankTransferCallbackServiceImpl.BANK_TRANSFER_CALLBEACK_HEADER));
                        BankTransferCallback bankTransferCallback = bankTransferCallbackRepository
                                        .getById(Long.valueOf(
                                                        context.getMessageHeader(
                                                                        BankTransferCallbackServiceImpl.BANK_TRANSFER_CALLBEACK_HEADER)
                                                                        .toString()));
                        log.info("bank Transfer callback -> {}",
                                        bankTransferCallback);

                        Optional<PaymentBankTransfer> fetchPaymentBankTransfer = paymentBankTransferRepository
                                        .findByPaymentServiceIdAndBillAmountAndStatus(
                                                        bankTransferCallback.getPaymentMerchant(),
                                                        bankTransferCallback.getAmount(),
                                                        PaymentStatusEnum.WAITING_PAYMENT);
                        if (fetchPaymentBankTransfer.isPresent()) {
                                log.info("Payment found");
                                context.getStateMachine()
                                                .sendEvent(MessageBuilder
                                                                .withPayload(BankTransferStatusEvent.PROCESS_ISSUED)
                                                                .setHeader(BankTransferCallbackServiceImpl.BANK_TRANSFER_CALLBEACK_HEADER,
                                                                                bankTransferCallback.getId())
                                                                .build());
                                PaymentBankTransfer paymentBankTransfer = fetchPaymentBankTransfer.get();
                                paymentBankTransfer.setCallbackId(bankTransferCallback.getId());
                                paymentBankTransferRepository.save(paymentBankTransfer);

                                paymentBankTransferService.paymentHasBeenPaid(paymentBankTransfer.getId());

                        } else {
                                log.info("Payment not found");
                                context.getStateMachine()
                                                .sendEvent(MessageBuilder.withPayload(
                                                                BankTransferStatusEvent.TRANSACTION_NOT_FOUND)
                                                                .setHeader(BankTransferCallbackServiceImpl.BANK_TRANSFER_CALLBEACK_HEADER,
                                                                                bankTransferCallback.getId())
                                                                .build());

                        }
                };
        }

        public Action<BankTransferStatusEnum, BankTransferStatusEvent> processIssuedAction() {
                return context -> {
                        log.info("processIssuedAction is running....");
                        BankTransferCallback bankTransferCallback = bankTransferCallbackRepository.getById(Long.valueOf(
                                        context.getMessageHeader(
                                                        BankTransferCallbackServiceImpl.BANK_TRANSFER_CALLBEACK_HEADER)
                                                        .toString()));
                        log.info("latest status bankTransferCallback -> {}", bankTransferCallback);
                        context.getStateMachine()
                                        .sendEvent(MessageBuilder
                                                        .withPayload(BankTransferStatusEvent.TRANSACTION_NOT_FOUND)
                                                        .setHeader(BankTransferCallbackServiceImpl.BANK_TRANSFER_CALLBEACK_HEADER,
                                                                        context.getMessageHeader(
                                                                                        BankTransferCallbackServiceImpl.BANK_TRANSFER_CALLBEACK_HEADER))
                                                        .build());
                };
        }

        public Action<BankTransferStatusEnum, BankTransferStatusEvent> transactionNotFoundAction() {
                return context -> {
                        log.info("transactionNotFoundAction is running....");
                        BankTransferCallback bankTransferCallback = bankTransferCallbackRepository.getById(Long.valueOf(
                                        context.getMessageHeader(
                                                        BankTransferCallbackServiceImpl.BANK_TRANSFER_CALLBEACK_HEADER)
                                                        .toString()));
                        log.info("latest status bankTransferCallback -> {}", bankTransferCallback);
                };
        }
}
