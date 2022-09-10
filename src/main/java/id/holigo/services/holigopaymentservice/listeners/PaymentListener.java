package id.holigo.services.holigopaymentservice.listeners;

import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import id.holigo.services.common.model.DepositDto;
import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.services.PaymentService;
import id.holigo.services.holigopaymentservice.services.deposit.DepositService;
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

import javax.jms.JMSException;

@RequiredArgsConstructor
@Component
public class PaymentListener {
    private PaymentRepository paymentRepository;
    private PaymentBankTransferRepository paymentBankTransferRepository;
    private PaymentVirtualAccountRepository paymentVirtualAccountRepository;
    private BankTransferCallbackService bankTransferCallbackService;
    private VirtualAccountCallbackService virtualAccountCallbackService;
    private DepositService depositService;
    private PaymentService paymentService;

    @Autowired
    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Autowired
    public void setDepositService(DepositService depositService) {
        this.depositService = depositService;
    }

    @Autowired
    public void setPaymentRepository(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Autowired
    public void setPaymentBankTransferRepository(PaymentBankTransferRepository paymentBankTransferRepository) {
        this.paymentBankTransferRepository = paymentBankTransferRepository;
    }

    public void setPaymentVirtualAccountRepository(PaymentVirtualAccountRepository paymentVirtualAccountRepository) {
        this.paymentVirtualAccountRepository = paymentVirtualAccountRepository;
    }

    @Autowired
    public void setBankTransferCallbackService(BankTransferCallbackService bankTransferCallbackService) {
        this.bankTransferCallbackService = bankTransferCallbackService;
    }

    @Autowired
    public void setVirtualAccountCallbackService(VirtualAccountCallbackService virtualAccountCallbackService) {
        this.virtualAccountCallbackService = virtualAccountCallbackService;
    }

    @Transactional
    @JmsListener(destination = JmsConfig.UPDATE_PAYMENT_STATUS_BY_PAYMENT_ID)
    public void listenForUpdatePaymentStatus(TransactionEvent transactionEvent) {
        TransactionDto transactionDto = transactionEvent.getTransactionDto();
        Payment payment = paymentRepository.getById(transactionDto.getPaymentId());
        if (transactionDto.getOrderStatus().equals(OrderStatusEnum.ISSUED_FAILED)
                && payment.getStatus().equals(PaymentStatusEnum.PAID)) {
            DepositDto depositDto = DepositDto.builder()
                    .creditAmount(payment.getPaymentServiceAmount())
                    .paymentId(payment.getId())
                    .informationIndex("depositStatement.refundTransaction")
                    .invoiceNumber(transactionDto.getInvoiceNumber())
                    .transactionId(transactionDto.getId())
                    .transactionType(transactionDto.getTransactionType())
                    .userId(transactionDto.getUserId())
                    .build();
            try {
                DepositDto resultDepositDto = depositService.credit(depositDto);
                if (resultDepositDto.getIsValid()) {
                    paymentService.refundPayment(payment.getId());
                }

            } catch (JMSException | JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        switch (payment.getDetailType()) {
            case "bankTransfer" -> {
                PaymentBankTransfer paymentBankTransfer = paymentBankTransferRepository
                        .getById(UUID.fromString(payment.getDetailId()));
                if (transactionDto.getOrderStatus().equals(OrderStatusEnum.ISSUED)) {
                    bankTransferCallbackService.issuedTransaction(paymentBankTransfer.getCallbackId());
                    if (!payment.getIsServiceFeeRefunded()) {
                        DepositDto depositDto = DepositDto.builder()
                                .creditAmount(payment.getServiceFeeAmount())
                                .paymentId(payment.getId())
                                .informationIndex("depositStatement.refundServiceFee")
                                .invoiceNumber(transactionDto.getInvoiceNumber())
                                .transactionId(transactionDto.getId())
                                .transactionType(transactionDto.getTransactionType())
                                .userId(transactionDto.getUserId())
                                .build();
                        try {
                            DepositDto resultDepositDto = depositService.credit(depositDto);
                            if (resultDepositDto.getIsValid()) {
                                payment.setIsServiceFeeRefunded(resultDepositDto.getIsValid());
                                paymentRepository.save(payment);
                            }

                        } catch (JMSException | JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
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
