package id.holigo.services.holigopaymentservice.services;

import id.holigo.services.common.model.TransactionDto;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.domain.PaymentVirtualAccount;

public interface PaymentVirtualAccountService {
    PaymentVirtualAccount createNewVirtualAccount(TransactionDto transactionDto, Payment payment);
}
