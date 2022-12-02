package id.holigo.services.holigopaymentservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.common.model.TransactionDto;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.domain.PaymentDigitalWallet;
import id.holigo.services.holigopaymentservice.events.PaymentDigitalWalletEvent;
import id.holigo.services.holigopaymentservice.interceptors.PaymentDigitalWalletInterceptor;
import id.holigo.services.holigopaymentservice.repositories.PaymentDigitalWalletRepository;
import id.holigo.services.holigopaymentservice.services.billing.BillingService;
import id.holigo.services.holigopaymentservice.services.logs.LogService;
import id.holigo.services.holigopaymentservice.web.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentDigitalWalletServiceImpl implements PaymentDigitalWalletService {
    public static final String PAYMENT_DIGITAL_WALLET_HEADER = "payment_digital_wallet_id";

    private PaymentDigitalWalletInterceptor paymentDigitalWalletInterceptor;

    private final StateMachineFactory<PaymentStatusEnum, PaymentDigitalWalletEvent> stateMachineFactory;

    private BillingService billingService;

    private PaymentDigitalWalletRepository paymentDigitalWalletRepository;

    @Autowired
    public void setPaymentDigitalWalletInterceptor(PaymentDigitalWalletInterceptor paymentDigitalWalletInterceptor) {
        this.paymentDigitalWalletInterceptor = paymentDigitalWalletInterceptor;
    }

    @Autowired
    public void setPaymentDigitalWalletRepository(PaymentDigitalWalletRepository paymentDigitalWalletRepository) {
        this.paymentDigitalWalletRepository = paymentDigitalWalletRepository;
    }

    @Autowired
    public void setBillingService(BillingService billingService) {
        this.billingService = billingService;
    }

    @Override
    public PaymentDigitalWallet createPaymentDigitalWallet(TransactionDto transactionDto, Payment payment) {
        BigDecimal totalAmount = payment.getPaymentServiceAmount();
        BigDecimal serviceFeeAmount;
        if (payment.getPaymentService().getId().equals("DANA")) {
            serviceFeeAmount = totalAmount.multiply(new BigDecimal("0.015")).setScale(0, RoundingMode.UP);
        } else {
            serviceFeeAmount = totalAmount.multiply(new BigDecimal("0.0064")).setScale(0, RoundingMode.UP);
        }

        BigDecimal billAmount = totalAmount.add(serviceFeeAmount).setScale(0, RoundingMode.UP);
        String[] users = transactionDto.getIndexUser().split("\\|");
        String[] products = transactionDto.getIndexProduct().split("\\|");
        String name = users[0];
        String description;
        switch (transactionDto.getTransactionType()) {
            case "AIR", "TRAIN" -> description = products[1] + " " + products[2];
            default -> description = products[1] + " " + products[2] + " " + products[3];
        }
        String accountNumber = users[1];
        if (accountNumber == null || accountNumber.equals("null")) {
            accountNumber = "081388882386";
        }
        PaymentDigitalWallet paymentDigitalWallet = new PaymentDigitalWallet();
        paymentDigitalWallet.setUserId(payment.getUserId());
        paymentDigitalWallet.setTotalAmount(totalAmount);
        paymentDigitalWallet.setServiceFeeAmount(serviceFeeAmount);
        paymentDigitalWallet.setBillAmount(billAmount);
        paymentDigitalWallet.setName(name);
        paymentDigitalWallet.setDescription(description);
        paymentDigitalWallet.setAccountNumber(accountNumber);
        paymentDigitalWallet.setStatus(PaymentStatusEnum.WAITING_PAYMENT);
        paymentDigitalWallet.setPaymentService(payment.getPaymentService());
        paymentDigitalWallet.setInvoiceNumber(transactionDto.getInvoiceNumber());
        paymentDigitalWalletRepository.save(paymentDigitalWallet);
        RequestBillingDto requestBillingDto = RequestBillingDto.builder()
                .accountNumber(accountNumber)
                .amount(billAmount)
                .dev(false)
                .paymentMerchant(payment.getPaymentService().getId().equals("DANA") ? "DANA" : "SHOP")
                .transactionId(paymentDigitalWallet.getInvoiceNumber())
                .transactionTime(Timestamp.valueOf(LocalDateTime.now()))
                .paymentMethod(payment.getPaymentService().getId().equals("DANA") ? "EWAL" : "QRIS")
                .transactionDescription(description).build();
        // API to supplier
        ResponseBillingDto responseBillingDto = billingService.postPayment(requestBillingDto);
        if (responseBillingDto != null) {
            if (responseBillingDto.getStatus() && responseBillingDto.getError_code().equals("000")) {
                paymentDigitalWallet.setPaymentUrl(responseBillingDto.getData().getPaymentUrl());
                paymentDigitalWallet.setPaymentCode(responseBillingDto.getData().getPaymentCode());
                paymentDigitalWallet.setReference(responseBillingDto.getData().getTransactionReference());
                paymentDigitalWalletRepository.save(paymentDigitalWallet);
            } else {
                paymentDigitalWallet.setStatus(PaymentStatusEnum.PAYMENT_FAILED);
                paymentDigitalWallet.setNote(responseBillingDto.getError_message());
                paymentDigitalWalletRepository.save(paymentDigitalWallet);
            }
        }
        return paymentDigitalWallet;
    }

    @Override
    public StateMachine<PaymentStatusEnum, PaymentDigitalWalletEvent> paymentHasBeenPaid(UUID id) {
        StateMachine<PaymentStatusEnum, PaymentDigitalWalletEvent> sm = build(id);
        sendEvent(id, sm, PaymentDigitalWalletEvent.PAYMENT_PAID);
        return sm;
    }

    @Override
    public void paymentHasBeenExpired(UUID id) {
        StateMachine<PaymentStatusEnum, PaymentDigitalWalletEvent> sm = build(id);
        sendEvent(id, sm, PaymentDigitalWalletEvent.PAYMENT_EXPIRED);
    }

    @Override
    public StateMachine<PaymentStatusEnum, PaymentDigitalWalletEvent> cancelPayment(UUID id) {
        StateMachine<PaymentStatusEnum, PaymentDigitalWalletEvent> sm = build(id);
        sendEvent(id, sm, PaymentDigitalWalletEvent.PAYMENT_CANCELED);
        return sm;
    }

    private void sendEvent(UUID id,
                           StateMachine<PaymentStatusEnum, PaymentDigitalWalletEvent> sm, PaymentDigitalWalletEvent event) {
        Message<PaymentDigitalWalletEvent> message = MessageBuilder.withPayload(event)
                .setHeader(PAYMENT_DIGITAL_WALLET_HEADER, id).build();
        sm.sendEvent(message);
    }

    private StateMachine<PaymentStatusEnum, PaymentDigitalWalletEvent> build(UUID id) {
        PaymentDigitalWallet paymentDigitalWallet = paymentDigitalWalletRepository.getById(id);

        StateMachine<PaymentStatusEnum, PaymentDigitalWalletEvent> sm = stateMachineFactory
                .getStateMachine(paymentDigitalWallet.getId().toString());

        sm.stop();
        sm.getStateMachineAccessor().doWithAllRegions(sma -> {
            sma.addStateMachineInterceptor(paymentDigitalWalletInterceptor);
            sma.resetStateMachine(new DefaultStateMachineContext<>(
                    paymentDigitalWallet.getStatus(), null, null, null));
        });
        sm.start();
        return sm;
    }
}
