package id.holigo.services.holigopaymentservice.repositories;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.PaymentDigitalWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface PaymentDigitalWalletRepository extends JpaRepository<PaymentDigitalWallet, UUID> {
    Optional<PaymentDigitalWallet> findByAccountNumberAndStatusAndBillAmount(String accountNumber, PaymentStatusEnum paymentStatusEnum, BigDecimal billAmount);

    Optional<PaymentDigitalWallet> findByCallbackId(Long id);
}
