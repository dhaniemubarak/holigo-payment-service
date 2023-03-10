package id.holigo.services.holigopaymentservice.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import id.holigo.services.common.model.PaymentDto;
import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.common.model.TransactionDto;
import id.holigo.services.holigopaymentservice.config.KafkaTopicConfig;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.repositories.PaymentRepository;
import id.holigo.services.holigopaymentservice.services.PaymentBankTransferService;
import id.holigo.services.holigopaymentservice.services.PaymentDigitalWalletService;
import id.holigo.services.holigopaymentservice.services.PaymentService;
import id.holigo.services.holigopaymentservice.services.PaymentVirtualAccountService;
import id.holigo.services.holigopaymentservice.services.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.jms.JMSException;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class KafkaPaymentListener {
    private final PaymentRepository paymentRepository;
    private final TransactionTemplate transactionTemplate;

    private final TransactionService transactionService;
    private final PaymentService paymentService;
    private final PaymentBankTransferService paymentBankTransferService;
    private final PaymentVirtualAccountService paymentVirtualAccountService;
    private final PaymentDigitalWalletService paymentDigitalWalletService;

    @KafkaListener(topics = KafkaTopicConfig.UPDATE_PAYMENT, groupId = "update-payment", containerFactory = "paymentListenerContainerFactory")
    void listen(PaymentDto paymentDto) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(@Nullable TransactionStatus status) {
                List<Payment> payments = paymentRepository.findAllByTransactionIdAndStatus(paymentDto.getTransactionId(), PaymentStatusEnum.WAITING_PAYMENT);
                payments.forEach(payment -> {
                    paymentService.paymentExpired(payment.getId());
                    switch (payment.getDetailType()) {
                        case "bankTransfer" ->
                                paymentBankTransferService.paymentHasBeenExpired(UUID.fromString(payment.getDetailId()));
                        case "virtualAccount" ->
                                paymentVirtualAccountService.paymentHasBeenExpired(UUID.fromString(payment.getDetailId()));
                        case "digitalWallet" ->
                                paymentDigitalWalletService.paymentHasBeenExpired(UUID.fromString(payment.getDetailId()));

                    }
                });
            }
        });
    }

    @KafkaListener(topics = KafkaTopicConfig.CANCEL_PAYMENT, groupId = "cancel-payment", containerFactory = "paymentListenerContainerFactory")
    void listenForCancel(PaymentDto paymentDto) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(@Nullable TransactionStatus status) {
                List<Payment> payments = paymentRepository.findAllByTransactionId(paymentDto.getTransactionId());
                payments.forEach(payment -> {
                    if (payment.getStatus() != PaymentStatusEnum.PAID) {
                        paymentService.paymentCanceled(payment.getId());
                        switch (payment.getDetailType()) {
                            case "bankTransfer" ->
                                    paymentBankTransferService.cancelPayment(UUID.fromString(payment.getDetailId()));
                            case "virtualAccount" ->
                                    paymentVirtualAccountService.cancelPayment(UUID.fromString(payment.getDetailId()));
                            case "digitalWallet" ->
                                    paymentDigitalWalletService.cancelPayment(UUID.fromString(payment.getDetailId()));
                        }

                    }
                });
            }
        });
    }
}
