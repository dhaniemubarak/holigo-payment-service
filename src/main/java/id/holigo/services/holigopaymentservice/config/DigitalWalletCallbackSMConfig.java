package id.holigo.services.holigopaymentservice.config;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.*;
import id.holigo.services.holigopaymentservice.events.DigitalWalletStatusEvent;
import id.holigo.services.holigopaymentservice.repositories.DigitalWalletCallbackRepository;
import id.holigo.services.holigopaymentservice.repositories.PaymentDigitalWalletRepository;
import id.holigo.services.holigopaymentservice.services.*;
import lombok.extern.slf4j.Slf4j;
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

import java.util.EnumSet;
import java.util.Optional;

@Slf4j
@EnableStateMachineFactory(name = "digitalWalletCallbackSMF")
@Configuration
public class DigitalWalletCallbackSMConfig
        extends StateMachineConfigurerAdapter<PaymentCallbackStatusEnum, DigitalWalletStatusEvent> {

    private DigitalWalletCallbackRepository digitalWalletCallbackRepository;

    private PaymentDigitalWalletRepository paymentDigitalWalletRepository;

    private PaymentDigitalWalletService paymentDigitalWalletService;

    @Autowired
    public void setDigitalWalletCallbackRepository(DigitalWalletCallbackRepository digitalWalletCallbackRepository) {
        this.digitalWalletCallbackRepository = digitalWalletCallbackRepository;
    }

    @Autowired
    public void setPaymentDigitalWalletRepository(PaymentDigitalWalletRepository paymentDigitalWalletRepository) {
        this.paymentDigitalWalletRepository = paymentDigitalWalletRepository;
    }

    @Autowired
    public void setPaymentDigitalWalletService(PaymentDigitalWalletService paymentDigitalWalletService) {
        this.paymentDigitalWalletService = paymentDigitalWalletService;
    }

    @Override
    public void configure(StateMachineStateConfigurer<PaymentCallbackStatusEnum, DigitalWalletStatusEvent> states)
            throws Exception {
        states.withStates().initial(PaymentCallbackStatusEnum.RECEIVED)
                .states(EnumSet.allOf(PaymentCallbackStatusEnum.class))
                .end(PaymentCallbackStatusEnum.ISSUED)
                .end(PaymentCallbackStatusEnum.TRANSACTION_NOT_FOUND)
                .end(PaymentCallbackStatusEnum.ISSUED_FAILED);
    }

    @Override
    public void configure(
            StateMachineTransitionConfigurer<PaymentCallbackStatusEnum, DigitalWalletStatusEvent> transitions)
            throws Exception {
        transitions.withExternal().source(PaymentCallbackStatusEnum.RECEIVED)
                .target(PaymentCallbackStatusEnum.RECEIVED)
                .action(receivedAction())
                .event(DigitalWalletStatusEvent.FIND_TRANSACTION)
                .and()
                .withExternal().source(PaymentCallbackStatusEnum.RECEIVED)
                .target(PaymentCallbackStatusEnum.PROCESS_ISSUED)
                .action(processIssuedAction())
                .event(DigitalWalletStatusEvent.PROCESS_ISSUED)
                .and()
                .withExternal().source(PaymentCallbackStatusEnum.RECEIVED)
                .target(PaymentCallbackStatusEnum.TRANSACTION_NOT_FOUND)
                .action(transactionNotFoundAction())
                .event(DigitalWalletStatusEvent.TRANSACTION_NOT_FOUND)
                .and()
                .withExternal().source(PaymentCallbackStatusEnum.PROCESS_ISSUED)
                .target(PaymentCallbackStatusEnum.ISSUED)
                .event(DigitalWalletStatusEvent.ISSUED)
                .and()
                .withExternal().source(PaymentCallbackStatusEnum.PROCESS_ISSUED)
                .target(PaymentCallbackStatusEnum.ISSUED_FAILED)
                .event(DigitalWalletStatusEvent.ISSUED_FAILED);
    }

    @Override
    public void configure(
            StateMachineConfigurationConfigurer<PaymentCallbackStatusEnum, DigitalWalletStatusEvent> config)
            throws Exception {
        StateMachineListenerAdapter<PaymentCallbackStatusEnum, DigitalWalletStatusEvent> adapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<PaymentCallbackStatusEnum, DigitalWalletStatusEvent> from,
                                     State<PaymentCallbackStatusEnum, DigitalWalletStatusEvent> to) {
                log.info(String.format("stateChange(from: %s, to %s)", from, to));
            }
        };
        config.withConfiguration().listener(adapter);
    }

    public Action<PaymentCallbackStatusEnum, DigitalWalletStatusEvent> receivedAction() {
        return context -> {
            DigitalWalletCallback digitalWalletCallback = digitalWalletCallbackRepository
                    .getById(Long.valueOf(
                            context.getMessageHeader(
                                            DigitalWalletCallbackServiceImpl.DIGITAL_WALLET_CALLBACK_HEADER)
                                    .toString()));

            Optional<PaymentDigitalWallet> fetchPaymentDigitalWallet = paymentDigitalWalletRepository
                    .findByAccountNumberAndStatusAndBillAmount(
                            digitalWalletCallback.getAccountNumber(),
                            PaymentStatusEnum.WAITING_PAYMENT,
                            digitalWalletCallback.getAmount()
                    );
            if (fetchPaymentDigitalWallet.isPresent()) {
                context.getStateMachine()
                        .sendEvent(MessageBuilder
                                .withPayload(DigitalWalletStatusEvent.PROCESS_ISSUED)
                                .setHeader(DigitalWalletCallbackServiceImpl.DIGITAL_WALLET_CALLBACK_HEADER,
                                        digitalWalletCallback.getId())
                                .build());
                PaymentDigitalWallet paymentDigitalWallet = fetchPaymentDigitalWallet.get();
                paymentDigitalWallet.setCallbackId(digitalWalletCallback.getId());
                paymentDigitalWalletRepository.save(paymentDigitalWallet);
            } else {
                context.getStateMachine()
                        .sendEvent(MessageBuilder.withPayload(
                                        DigitalWalletStatusEvent.TRANSACTION_NOT_FOUND)
                                .setHeader(DigitalWalletCallbackServiceImpl.DIGITAL_WALLET_CALLBACK_HEADER,
                                        digitalWalletCallback.getId())
                                .build());

            }
        };
    }

    public Action<PaymentCallbackStatusEnum, DigitalWalletStatusEvent> processIssuedAction() {
        return context -> {

            Optional<PaymentDigitalWallet> fetchPaymentDigitalWallet = paymentDigitalWalletRepository
                    .findByCallbackId(Long.valueOf(context.getMessageHeader(
                                    DigitalWalletCallbackServiceImpl.DIGITAL_WALLET_CALLBACK_HEADER)
                            .toString()));
            if (fetchPaymentDigitalWallet.isPresent()) {
                PaymentDigitalWallet paymentDigitalWallet = fetchPaymentDigitalWallet.get();
                paymentDigitalWalletService.paymentHasBeenPaid(paymentDigitalWallet.getId());

            }
        };
    }

    public Action<PaymentCallbackStatusEnum, DigitalWalletStatusEvent> transactionNotFoundAction() {
        return context -> {
            DigitalWalletCallback digitalWalletCallback = digitalWalletCallbackRepository.getById(Long.valueOf(
                    context.getMessageHeader(
                                    DigitalWalletCallbackServiceImpl.DIGITAL_WALLET_CALLBACK_HEADER)
                            .toString()));
        };
    }
}
