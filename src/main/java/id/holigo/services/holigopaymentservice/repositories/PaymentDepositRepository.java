package id.holigo.services.holigopaymentservice.repositories;

import id.holigo.services.holigopaymentservice.domain.PaymentDeposit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentDepositRepository extends JpaRepository<PaymentDeposit, UUID> {
}
