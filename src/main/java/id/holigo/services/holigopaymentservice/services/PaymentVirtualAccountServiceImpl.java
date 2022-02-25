package id.holigo.services.holigopaymentservice.services;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import id.holigo.services.common.model.TransactionDto;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.domain.PaymentVirtualAccount;
import id.holigo.services.holigopaymentservice.repositories.PaymentVirtualAccountRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PaymentVirtualAccountServiceImpl implements PaymentVirtualAccountService {

    @Autowired
    private final PaymentVirtualAccountRepository paymentVirtualAccountRepository;

    @Override
    public PaymentVirtualAccount createNewVirtualAccount(TransactionDto transactionDto, Payment payment) {
        BigDecimal totalAmount = transactionDto.getFareAmount();
        BigDecimal serviceFeeAmount = BigDecimal.valueOf(2750.00);
        BigDecimal billAmount = totalAmount.add(serviceFeeAmount);
        String[] users = transactionDto.getIndexUser().split("\\|");
        String[] products = transactionDto.getIndexProduct().split("\\");
        String name = users[0];
        String description = products[1] + " " + products[2] + " " + products[3];
        String accountNumber = users[1];

        PaymentVirtualAccount paymentVirtualAccount = new PaymentVirtualAccount();
        paymentVirtualAccount.setUserId(payment.getUserId());
        paymentVirtualAccount.setTotalAmount(totalAmount);
        paymentVirtualAccount.setServiceFeeAmount(serviceFeeAmount);
        paymentVirtualAccount.setBillAmount(billAmount);
        paymentVirtualAccount.setName(name);
        paymentVirtualAccount.setDescription(description);
        paymentVirtualAccount.setAccountNumber(accountNumber);
        paymentVirtualAccount.setStatus(transactionDto.getPaymentStatus());
        paymentVirtualAccount.setPaymentServiceId(payment.getPaymentServiceId());
        return paymentVirtualAccountRepository.save(paymentVirtualAccount);
    }

    String getAccountNumber(String phoneNumber) {
        String accountNumber = "14045";
        if (phoneNumber.substring(0, 2) == "62") {
            int length = phoneNumber.length();
            accountNumber += phoneNumber.substring(2, length - 2);
        } else {
            accountNumber += phoneNumber;
        }
        return accountNumber;
    }

}
