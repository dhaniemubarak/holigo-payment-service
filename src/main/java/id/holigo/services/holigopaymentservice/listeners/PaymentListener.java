package id.holigo.services.holigopaymentservice.listeners;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import id.holigo.services.common.events.TransactionEvent;
import id.holigo.services.common.model.OrderStatusEnum;
import id.holigo.services.common.model.TransactionDto;
import id.holigo.services.holigopaymentservice.config.JmsConfig;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.domain.PaymentBankTransfer;
import id.holigo.services.holigopaymentservice.domain.PaymentVirtualAccount;
import id.holigo.services.holigopaymentservice.repositories.PaymentBankTransferRepository;
import id.holigo.services.holigopaymentservice.repositories.PaymentRepository;
import id.holigo.services.holigopaymentservice.repositories.PaymentVirtualAccountRepository;
import id.holigo.services.holigopaymentservice.services.BankTransferCallbackService;
import id.holigo.services.holigopaymentservice.services.VirtualAccountCallbackService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class PaymentListener {

    @Autowired
    private final PaymentRepository paymentRepository;

    @Autowired
    private final PaymentBankTransferRepository paymentBankTransferRepository;

    @Autowired
    private final PaymentVirtualAccountRepository paymentVirtualAccountRepository;

    @Autowired
    private final BankTransferCallbackService bankTransferCallbackService;

    @Autowired
    private final VirtualAccountCallbackService virtualAccountCallbackService;

    @Transactional
    @JmsListener(destination = JmsConfig.UPDATE_PAYMENT_STATUS_BY_PAYMENT_ID)
    public void listenForUpdatePaymentStatus(TransactionEvent transactionEvent) {
        TransactionDto transactionDto = transactionEvent.getTransactionDto();
        Payment payment = paymentRepository.getById(transactionDto.getPaymentId());
        switch (payment.getDetailType()) {
            case "bankTransfer" -> {
                PaymentBankTransfer paymentBankTransfer = paymentBankTransferRepository
                        .getById(UUID.fromString(payment.getDetailId()));
                if (transactionDto.getOrderStatus().equals(OrderStatusEnum.ISSUED)) {
                    bankTransferCallbackService.issuedTransaction(paymentBankTransfer.getCallbackId());
                }
                if (transactionDto.getOrderStatus().equals(OrderStatusEnum.ISSUED_FAILED)) {
                    bankTransferCallbackService.failedTransaction(paymentBankTransfer.getCallbackId());
                }
            }
            case "virtualAccount" -> {
                PaymentVirtualAccount paymentVirtualAccount = paymentVirtualAccountRepository
                        .getById(UUID.fromString(payment.getDetailId()));
                if (transactionDto.getOrderStatus().equals(OrderStatusEnum.ISSUED)) {
                    virtualAccountCallbackService.issuedTransaction(paymentVirtualAccount.getCallbackId());
                }
                if (transactionDto.getOrderStatus().equals(OrderStatusEnum.ISSUED_FAILED)) {
                    virtualAccountCallbackService.failedTransaction(paymentVirtualAccount.getCallbackId());
                }
            }
        }

    }
}
