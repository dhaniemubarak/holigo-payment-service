package id.holigo.services.holigopaymentservice.listeners;

import id.holigo.services.common.model.PaymentDto;
import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.config.KafkaTopicConfig;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.repositories.PaymentRepository;
import id.holigo.services.holigopaymentservice.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@RequiredArgsConstructor
@Component
public class KafkaPaymentListener {
    private final PaymentRepository paymentRepository;
    private final TransactionTemplate transactionTemplate;

    private final PaymentService paymentService;

    @KafkaListener(topics = KafkaTopicConfig.UPDATE_PAYMENT, groupId = "update-payment", containerFactory = "paymentListenerContainerFactory")
    void listen(PaymentDto paymentDto) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(@Nullable TransactionStatus status) {
                List<Payment> payments = paymentRepository.findAllByTransactionIdAndStatus(paymentDto.getTransactionId(), PaymentStatusEnum.WAITING_PAYMENT);
                payments.forEach(payment -> paymentService.paymentExpired(payment.getId()));
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
                    }
                });
            }
        });
    }
}
