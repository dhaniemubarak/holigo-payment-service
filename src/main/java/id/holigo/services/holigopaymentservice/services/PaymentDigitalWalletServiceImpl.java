package id.holigo.services.holigopaymentservice.services;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.common.model.TransactionDto;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.domain.PaymentDigitalWallet;
import id.holigo.services.holigopaymentservice.repositories.PaymentDigitalWalletRepository;
import id.holigo.services.holigopaymentservice.services.billing.BillingService;
import id.holigo.services.holigopaymentservice.web.model.RequestBillingDto;
import id.holigo.services.holigopaymentservice.web.model.ResponseBillingDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Slf4j
@Service
public class PaymentDigitalWalletServiceImpl implements PaymentDigitalWalletService {

    private BillingService billingService;

    private PaymentDigitalWalletRepository paymentDigitalWalletRepository;

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
}
