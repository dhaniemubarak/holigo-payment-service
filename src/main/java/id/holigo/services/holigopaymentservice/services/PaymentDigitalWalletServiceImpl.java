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

    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public static final String PAYMENT_DIGITAL_WALLET_HEADER = "payment_digital_wallet_id";

    private PaymentDigitalWalletInterceptor paymentDigitalWalletInterceptor;

    private final StateMachineFactory<PaymentStatusEnum, PaymentDigitalWalletEvent> stateMachineFactory;

    private BillingService billingService;

    private PaymentDigitalWalletRepository paymentDigitalWalletRepository;

    private LogService logService;

    @Autowired
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

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
        BigDecimal serviceFeeAmount = totalAmount.multiply(new BigDecimal("0.015")).setScale(0, RoundingMode.UP);
        BigDecimal billAmount = totalAmount.add(serviceFeeAmount).setScale(0, RoundingMode.UP);
        String[] users = transactionDto.getIndexUser().split("\\|");
        String[] products = transactionDto.getIndexProduct().split("\\|");
        String name = users[0];
        String description = products[1] + " " + products[2] + " " + products[3];
        String accountNumber = users[1];
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
                .paymentMerchant("DANA")
                .transactionId(paymentDigitalWallet.getInvoiceNumber())
                .transactionTime(Timestamp.valueOf(LocalDateTime.now()))
                .paymentMethod("EWAL")
                .transactionDescription(description).build();
        // API to supplier
        ResponseBillingDto responseBillingDto = billingService.postPayment(requestBillingDto);
        if (responseBillingDto != null) {
            if (responseBillingDto.getStatus() && responseBillingDto.getError_code().equals("000")) {
                paymentDigitalWallet.setPaymentUrl(responseBillingDto.getData().getPaymentUrl());
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
    public void checkStatus(PaymentDigitalWallet paymentDigitalWallet) {
        SupplierLogDto supplierLogDto = SupplierLogDto.builder().build();
        RequestBillingStatusDto requestBillingStatusDto = RequestBillingStatusDto.builder()
                .dev(false)
                .transactionId(paymentDigitalWallet.getInvoiceNumber()).build();
        ResponseBillingStatusDto responseBillingStatusDto = billingService.postCheckStatus(requestBillingStatusDto);
        try {
            supplierLogDto.setLogRequest(objectMapper.writeValueAsString(requestBillingStatusDto));
            supplierLogDto.setLogResponse(objectMapper.writeValueAsString(responseBillingStatusDto));
            supplierLogDto.setSupplier("nicepay");
            supplierLogDto.setUrl("https://billing.holigo.id/nicepay/status");
            supplierLogDto.setMessage(responseBillingStatusDto.getError_message());
            supplierLogDto.setCode("DANA");
            supplierLogDto.setUserId(paymentDigitalWallet.getUserId());
            logService.sendSupplierLog(supplierLogDto);
        } catch (JsonProcessingException e) {
            log.error("Error : {}", e.getMessage());
        }
        if (responseBillingStatusDto != null) {
            if (responseBillingStatusDto.getStatus()) {
                if (responseBillingStatusDto.getData().getTransactionStatus().equalsIgnoreCase("success") ||
                        responseBillingStatusDto.getData().getTransactionStatus().equalsIgnoreCase("paid")) {
                    paymentHasBeenPaid(paymentDigitalWallet.getId());
                }
                if (responseBillingStatusDto.getData().getTransactionStatus().equalsIgnoreCase("expired")) {
                    paymentHasBeenExpired(paymentDigitalWallet.getId());
                }
            }
        }

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
