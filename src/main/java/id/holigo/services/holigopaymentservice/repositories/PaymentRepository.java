package id.holigo.services.holigopaymentservice.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByDetailTypeAndDetailId(String detailType, String detailId);

    List<Payment> findAllByUserIdAndStatusAndPaymentServiceId(Long userId, PaymentStatusEnum status,
                                                              String paymentServiceId);

    List<Payment> findAllByTransactionIdAndStatus(UUID transactionId, PaymentStatusEnum status);

    List<Payment> findAllByStatusAndPaymentServiceId(PaymentStatusEnum paymentStatusEnum, String paymentServiceId);

    List<Payment> findAllByTransactionId(UUID transactionId);
}
