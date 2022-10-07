package id.holigo.services.holigopaymentservice.config;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import id.holigo.services.common.model.DepositDto;
import id.holigo.services.common.model.PointDto;
import id.holigo.services.common.model.TransactionDto;
import id.holigo.services.holigopaymentservice.services.deposit.DepositService;
import id.holigo.services.holigopaymentservice.services.point.PointService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.events.PaymentStatusEvent;
import id.holigo.services.holigopaymentservice.repositories.PaymentRepository;
import id.holigo.services.holigopaymentservice.services.PaymentServiceImpl;
import id.holigo.services.holigopaymentservice.services.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.jms.JMSException;

@RequiredArgsConstructor
@Slf4j
@EnableStateMachineFactory(name = "paymentSMF")
@Configuration
public class PaymentSMConfig extends StateMachineConfigurerAdapter<PaymentStatusEnum, PaymentStatusEvent> {

    private final TransactionService transactionService;
    private final PaymentRepository paymentRepository;
    private final PointService pointService;
    private final DepositService depositService;


    @Override
    public void configure(StateMachineStateConfigurer<PaymentStatusEnum, PaymentStatusEvent> states)
            throws Exception {
        states.withStates().initial(PaymentStatusEnum.WAITING_PAYMENT)
                .states(EnumSet.allOf(PaymentStatusEnum.class))
                .end(PaymentStatusEnum.PAYMENT_CANCELED)
                .end(PaymentStatusEnum.PAYMENT_EXPIRED)
                .end(PaymentStatusEnum.REFUNDED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentStatusEnum, PaymentStatusEvent> transitions)
            throws Exception {
        transitions.withExternal().source(PaymentStatusEnum.WAITING_PAYMENT).target(PaymentStatusEnum.PAID)
                .event(PaymentStatusEvent.PAYMENT_PAID).action(paidAction())
                .and().withExternal().source(PaymentStatusEnum.WAITING_PAYMENT).target(PaymentStatusEnum.PAYMENT_EXPIRED)
                .event(PaymentStatusEvent.PAYMENT_EXPIRED).action(refundPointAndDepositAction())
                .and().withExternal().source(PaymentStatusEnum.WAITING_PAYMENT).target(PaymentStatusEnum.PAYMENT_CANCELED)
                .event(PaymentStatusEvent.PAYMENT_CANCEL).action(refundPointAndDepositAction())
                .and().withExternal().source(PaymentStatusEnum.PAID).target(PaymentStatusEnum.REFUNDED)
                .event(PaymentStatusEvent.PAYMENT_REFUND);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentStatusEnum, PaymentStatusEvent> config)
            throws Exception {
        StateMachineListenerAdapter<PaymentStatusEnum, PaymentStatusEvent> adapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<PaymentStatusEnum, PaymentStatusEvent> from,
                                     State<PaymentStatusEnum, PaymentStatusEvent> to) {
                log.info(String.format("stateChange(from: %s, to %s)", from, to));
            }
        };
        config.withConfiguration().listener(adapter);
    }

    @Bean
    public Action<PaymentStatusEnum, PaymentStatusEvent> paidAction() {
        return context -> {
            Payment payment = paymentRepository
                    .getById(UUID.fromString(context.getMessageHeader(PaymentServiceImpl.PAYMENT_HEADER).toString()));
            transactionService.issuedTransaction(payment.getTransactionId(), payment);
        };
    }

    @Bean
    public Action<PaymentStatusEnum, PaymentStatusEvent> refundPointAndDepositAction() {
        log.info("refundPointAndDepositAction is running ......");
        return stateContext -> {
            TransactionDto transactionDto = null;
            Payment payment = paymentRepository.getById(UUID.fromString(stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_HEADER).toString()));
            try {
                if (payment.getPointAmount().compareTo(BigDecimal.ZERO) > 0) {
                    log.info("Menggunakan point");
                    transactionDto = transactionService.getTransaction(payment.getTransactionId());
                    PointDto point = pointService.credit(PointDto.builder()
                            .creditAmount(payment.getPointAmount().intValue())
                            .paymentId(payment.getId())
                            .informationIndex("pointStatement.refund")
                            .invoiceNumber(transactionDto.getInvoiceNumber())
                            .isValid(false)
                            .transactionId(transactionDto.getId())
                            .transactionType(transactionDto.getTransactionType())
                            .userId(payment.getUserId())
                            .build());
                    if (point.getIsValid()) {
                        payment.setNote("Point refunded");
                        paymentRepository.save(payment);
                    }

                }
                log.info("point sudah lewat");
                if (payment.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
                    log.info("Menggunakan deposit");
                    if (transactionDto != null) {
                        transactionDto = transactionService.getTransaction(payment.getTransactionId());
                    }
                    assert transactionDto != null;
                    DepositDto deposit = depositService.credit(DepositDto.builder()
                            .category("REFUND")
                            .creditAmount(payment.getDepositAmount())
                            .paymentId(payment.getId())
                            .informationIndex("depositStatement.refund")
                            .invoiceNumber(transactionDto.getInvoiceNumber())
                            .transactionId(transactionDto.getId())
                            .transactionType(transactionDto.getTransactionType())
                            .userId(payment.getUserId())
                            .build());
                    if (deposit.getIsValid()) {
                        payment.setNote((payment.getNote().length() > 0) ? payment.getNote() + "#" + "Deposit refunded" : "Deposit refunded");
                        paymentRepository.save(payment);
                    }
                }

            } catch (JMSException | JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
