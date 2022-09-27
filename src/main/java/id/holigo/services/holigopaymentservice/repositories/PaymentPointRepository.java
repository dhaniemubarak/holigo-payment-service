package id.holigo.services.holigopaymentservice.repositories;

import id.holigo.services.holigopaymentservice.domain.PaymentPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentPointRepository extends JpaRepository<PaymentPoint, UUID> {
}
