package id.holigo.services.holigopaymentservice.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.UUID;

import id.holigo.services.holigopaymentservice.interceptors.PaymentVirtualAccountInterceptor;
import id.holigo.services.holigopaymentservice.services.billing.BillingService;
import id.holigo.services.holigopaymentservice.web.model.RequestBillingDto;
import id.holigo.services.holigopaymentservice.web.model.ResponseBillingDto;
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

    private final BillingService billingService;

    @Override
    public PaymentVirtualAccount createNewVirtualAccount(TransactionDto transactionDto, Payment payment) {
        BigDecimal totalAmount = payment.getPaymentServiceAmount();
        BigDecimal serviceFeeAmount = BigDecimal.valueOf(2750.00);
        BigDecimal billAmount = totalAmount.add(serviceFeeAmount);
        String[] users = transactionDto.getIndexUser().split("\\|");
        String[] products = transactionDto.getIndexProduct().split("\\|");
        String name = users[0];
        String description = products[1] + " " + products[2] + " " + products[3];
        String accountNumber = null;
        String reference = null;
        String invoiceNumber = transactionDto.getInvoiceNumber();

        switch (payment.getPaymentService().getId()) {
            case "VA_BNI", "VA_MAYBANK", "VA_PERMATA", "VA_PERMATA_S", "VA_KEB_HANA",
                    "VA_CIMB", "VA_BRI", "VA_DANAMON", "VA_BJB", "VA_BNC", "VA_OB" -> {
                String paymentMerchant = null;
                switch (payment.getPaymentService().getId()) {
                    case "VA_BNI" -> paymentMerchant = "BNIN";
                    case "VA_MAYBANK" -> paymentMerchant = "IBBK";
                    case "VA_PERMATA" -> paymentMerchant = "BBBA";
                    case "VA_PERMATA_S" -> paymentMerchant = "BBBB";
                    case "VA_KEB_HANA" -> paymentMerchant = "HNBN";
                    case "VA_CIMB" -> paymentMerchant = "BNIA";
                    case "VA_BRI" -> paymentMerchant = "BRIN";
                    case "VA_DANAMON" -> paymentMerchant = "BDIN";
                    case "VA_BJB" -> paymentMerchant = "PDJB";
                    case "VA_BNC" -> paymentMerchant = "YUDB";
                    case "VA_OB" -> paymentMerchant = "OTHR";
                }
                RequestBillingDto requestBillingDto = RequestBillingDto.builder()
                        .accountNumber(getAccountNumber(transactionDto, users[1], payment))
                        .amount(billAmount)
                        .dev(false)
                        .paymentMerchant(paymentMerchant)
                        .transactionId(invoiceNumber)
                        .transactionTime(Timestamp.valueOf(LocalDateTime.now()))
                        .paymentMethod("VirtualAccount")
                        .transactionDescription(description).build();
                // API to supplier
                ResponseBillingDto responseBillingDto = billingService.postPayment(requestBillingDto);
                if (responseBillingDto != null) {
                    if (responseBillingDto.getStatus() && responseBillingDto.getError_code().equals("000")) {
                        accountNumber = responseBillingDto.getData().getPaymentCode();
                        reference = responseBillingDto.getData().getTransactionReference();
                    }
                }
            }
            default -> accountNumber = getAccountNumber(transactionDto, users[1], payment);
        }
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
        paymentVirtualAccount.setInvoiceNumber(invoiceNumber);
        paymentVirtualAccount.setReference(reference);
        return paymentVirtualAccountRepository.save(paymentVirtualAccount);
    }

    static String getAccountNumber(TransactionDto transactionDto, String phoneNumber, Payment payment) {
        String accountNumber;
        switch (payment.getPaymentService().getId()) {
            case "VA_BCA" -> accountNumber = "14045";
            case "VA_MANDIRI" -> accountNumber = "70016";
            default -> accountNumber = "";
        }

        if (phoneNumber.startsWith("62")) {
            accountNumber += "0" + phoneNumber.substring(2);
        } else if (phoneNumber.equals("null")) {
            accountNumber += transactionDto.getInvoiceNumber().replaceAll("/", "");
            accountNumber = accountNumber.replace("" + Year.now().getValue(), "");
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
