package id.holigo.services.holigopaymentservice.services;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.common.model.TransactionDto;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.domain.PaymentVirtualAccount;
import id.holigo.services.holigopaymentservice.events.PaymentVirtualAccountEvent;
import id.holigo.services.holigopaymentservice.repositories.PaymentVirtualAccountRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PaymentVirtualAccountServiceImpl implements PaymentVirtualAccountService {
    public static final String PAYMENT_VIRTUAL_ACCOUNT_HEADER = "payment_virtual_account_id";
    private final PaymentVirtualAccountRepository paymentVirtualAccountRepository;
    private final StateMachineFactory<PaymentStatusEnum, PaymentVirtualAccountEvent> stateMachineFactory;
    private final PaymentVirtualAccountInterceptor paymentVirtualAccountInterceptor;

    @Override
    public PaymentVirtualAccount createNewVirtualAccount(TransactionDto transactionDto, Payment payment) {
        BigDecimal totalAmount = payment.getPaymentServiceAmount();
        BigDecimal serviceFeeAmount = BigDecimal.valueOf(2750.00);
        BigDecimal billAmount = totalAmount.add(serviceFeeAmount);
        String[] users = transactionDto.getIndexUser().split("\\|");
        String[] products = transactionDto.getIndexProduct().split("\\|");
        String name = users[0];
        String description = products[1] + " " + products[2] + " " + products[3];
        String accountNumber = getAccountNumber(transactionDto, users[1]);

        PaymentVirtualAccount paymentVirtualAccount = new PaymentVirtualAccount();
        paymentVirtualAccount.setUserId(payment.getUserId());
        paymentVirtualAccount.setTotalAmount(totalAmount);
        paymentVirtualAccount.setServiceFeeAmount(serviceFeeAmount);
        paymentVirtualAccount.setBillAmount(billAmount);
        paymentVirtualAccount.setName(name);
        paymentVirtualAccount.setDescription(description);
        paymentVirtualAccount.setAccountNumber(accountNumber);
        paymentVirtualAccount.setStatus(PaymentStatusEnum.WAITING_PAYMENT);
        paymentVirtualAccount.setPaymentService(payment.getPaymentService());
        return paymentVirtualAccountRepository.save(paymentVirtualAccount);
    }

    String getAccountNumber(TransactionDto transactionDto, String phoneNumber) {
        String accountNumber = "14045";
        if (phoneNumber.substring(0, 2).equals("62")) {
            int length = phoneNumber.length();
            accountNumber += phoneNumber.substring(2, length - 2);
        } else if (phoneNumber.equals("null")) {
            accountNumber += transactionDto.getInvoiceNumber().replaceAll("/", "");
        } else {
            accountNumber += phoneNumber;

        }

        return accountNumber;
    }

    @Override
    public StateMachine<PaymentStatusEnum, PaymentVirtualAccountEvent> paymentHasBeenPaid(UUID id) {
        StateMachine<PaymentStatusEnum, PaymentVirtualAccountEvent> sm = build(id);
        sendEvent(id, sm, PaymentVirtualAccountEvent.PAYMENT_PAID);
        return sm;
    }

    @Override
    public StateMachine<PaymentStatusEnum, PaymentVirtualAccountEvent> cancelPayment(UUID id) {
        StateMachine<PaymentStatusEnum, PaymentVirtualAccountEvent> sm = build(id);
        sendEvent(id, sm, PaymentVirtualAccountEvent.PAYMENT_CANCELED);
        return sm;
    }

    private void sendEvent(UUID id,
                           StateMachine<PaymentStatusEnum, PaymentVirtualAccountEvent> sm, PaymentVirtualAccountEvent event) {
        Message<PaymentVirtualAccountEvent> message = MessageBuilder.withPayload(event)
                .setHeader(PAYMENT_VIRTUAL_ACCOUNT_HEADER, id).build();
        sm.sendEvent(message);
    }

    private StateMachine<PaymentStatusEnum, PaymentVirtualAccountEvent> build(UUID id) {
        PaymentVirtualAccount paymentVirtualAccount = paymentVirtualAccountRepository.getById(id);

        StateMachine<PaymentStatusEnum, PaymentVirtualAccountEvent> sm = stateMachineFactory
                .getStateMachine(paymentVirtualAccount.getId().toString());

        sm.stop();
        sm.getStateMachineAccessor().doWithAllRegions(sma -> {
            sma.addStateMachineInterceptor(paymentVirtualAccountInterceptor);
            sma.resetStateMachine(new DefaultStateMachineContext<>(
                    paymentVirtualAccount.getStatus(), null, null, null));
        });
        sm.start();
        return sm;
    }

}
