package id.holigo.services.holigopaymentservice.services;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import id.holigo.services.holigopaymentservice.interceptors.PaymentBankTransferInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.common.model.TransactionDto;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.domain.PaymentBankTransfer;
import id.holigo.services.holigopaymentservice.events.PaymentBankTransferEvent;
import id.holigo.services.holigopaymentservice.repositories.PaymentBankTransferRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PaymentBankTransferServiceImpl implements PaymentBankTransferService {

    public static final String PAYMENT_BANK_TRANSFER_HEADER = "payment_bank_transfer_id";

    private final PaymentBankTransferRepository paymentBankTransferRepository;

    private final StateMachineFactory<PaymentStatusEnum, PaymentBankTransferEvent> stateMachineFactory;

    private final PaymentBankTransferInterceptor paymentBankTransferInterceptor;

    @Transactional
    @Override
    public StateMachine<PaymentStatusEnum, PaymentBankTransferEvent> paymentHasBeenPaid(UUID id) {
        StateMachine<PaymentStatusEnum, PaymentBankTransferEvent> sm = build(id);
        sendEvent(id, sm, PaymentBankTransferEvent.PAYMENT_PAID);
        return sm;
    }

    @Override
    public StateMachine<PaymentStatusEnum, PaymentBankTransferEvent> cancelPayment(UUID id) {
        StateMachine<PaymentStatusEnum, PaymentBankTransferEvent> sm = build(id);
        sendEvent(id, sm, PaymentBankTransferEvent.PAYMENT_CANCELED);
        return sm;
    }

    @Override
    public void paymentHasBeenExpired(UUID id) {
        StateMachine<PaymentStatusEnum, PaymentBankTransferEvent> sm = build(id);
        sendEvent(id, sm, PaymentBankTransferEvent.PAYMENT_EXPIRED);
    }

    private void sendEvent(UUID id,
                           StateMachine<PaymentStatusEnum, PaymentBankTransferEvent> sm, PaymentBankTransferEvent event) {
        Message<PaymentBankTransferEvent> message = MessageBuilder.withPayload(event)
                .setHeader(PAYMENT_BANK_TRANSFER_HEADER, id).build();
        sm.sendEvent(message);
    }

    private StateMachine<PaymentStatusEnum, PaymentBankTransferEvent> build(UUID id) {
        PaymentBankTransfer paymentBankTransfer = paymentBankTransferRepository.getById(id);

        StateMachine<PaymentStatusEnum, PaymentBankTransferEvent> sm = stateMachineFactory
                .getStateMachine(paymentBankTransfer.getId().toString());

        sm.stop();
        sm.getStateMachineAccessor().doWithAllRegions(sma -> {
            sma.addStateMachineInterceptor(paymentBankTransferInterceptor);
            sma.resetStateMachine(new DefaultStateMachineContext<>(
                    paymentBankTransfer.getStatus(), null, null, null));
        });
        sm.start();
        return sm;
    }

    @Override
    public PaymentBankTransfer createNewBankTransfer(TransactionDto transactionDto,
                                                     Payment payment) {
        BigDecimal serviceFeeAmount;
        BigDecimal paymentServiceAmount;
        BigDecimal totalAmount = payment.getPaymentServiceAmount();
        int uniqueCode;
        while (true) {
            uniqueCode = randomNumber();
            serviceFeeAmount = BigDecimal.valueOf(uniqueCode);
            paymentServiceAmount = totalAmount
                    .add(serviceFeeAmount);
            Optional<PaymentBankTransfer> tryFetchPaymentBankTransfer = paymentBankTransferRepository
                    .findByPaymentServiceIdAndBillAmountAndStatus(
                            payment.getPaymentService().getId(),
                            paymentServiceAmount,
                            PaymentStatusEnum.WAITING_PAYMENT);
            if (tryFetchPaymentBankTransfer.isEmpty()) {
                break;
            }
        }
        PaymentBankTransfer paymentBankTransfer = new PaymentBankTransfer();
        paymentBankTransfer.setUserId(payment.getUserId());
        paymentBankTransfer.setPaymentServiceId(payment.getPaymentService().getId());
        paymentBankTransfer.setTotalAmount(totalAmount);
        paymentBankTransfer.setUniqueCode(uniqueCode);
        paymentBankTransfer.setFdsAmount(BigDecimal.valueOf(0));
        paymentBankTransfer.setBillAmount(paymentServiceAmount);
        paymentBankTransfer.setVatAmount(BigDecimal.valueOf(0));
        paymentBankTransfer.setServiceFeeAmount(serviceFeeAmount);
        paymentBankTransfer.setStatus(PaymentStatusEnum.WAITING_PAYMENT);
        return paymentBankTransferRepository.save(paymentBankTransfer);
    }

    private Integer randomNumber() {
        int min = 1;
        int max = 99;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

}
