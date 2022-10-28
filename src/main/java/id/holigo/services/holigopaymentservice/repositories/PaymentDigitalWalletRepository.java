package id.holigo.services.holigopaymentservice.repositories;

import id.holigo.services.holigopaymentservice.domain.PaymentDigitalWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentDigitalWalletRepository extends JpaRepository<PaymentDigitalWallet, UUID> {
}
