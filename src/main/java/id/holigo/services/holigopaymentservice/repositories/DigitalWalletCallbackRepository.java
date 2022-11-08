package id.holigo.services.holigopaymentservice.repositories;

import id.holigo.services.holigopaymentservice.domain.DigitalWalletCallback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DigitalWalletCallbackRepository extends JpaRepository<DigitalWalletCallback, Long> {
}
