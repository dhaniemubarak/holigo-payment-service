package id.holigo.services.holigopaymentservice.repositories;

import id.holigo.services.holigopaymentservice.domain.PaymentForbidden;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentForbiddenRepository extends JpaRepository<PaymentForbidden, Integer> {

    Optional<PaymentForbidden> findByPaymentServiceIdAndProductId(String paymentServiceId, Integer productId);
}
