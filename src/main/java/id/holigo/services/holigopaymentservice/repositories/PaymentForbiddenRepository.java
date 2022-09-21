package id.holigo.services.holigopaymentservice.repositories;

import id.holigo.services.holigopaymentservice.domain.PaymentForbidden;
import id.holigo.services.holigopaymentservice.domain.PaymentService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentForbiddenRepository extends JpaRepository<PaymentForbidden, Integer> {

    Optional<PaymentForbidden> findByPaymentServiceAndProductId(PaymentService paymentService, Integer productId);
}
