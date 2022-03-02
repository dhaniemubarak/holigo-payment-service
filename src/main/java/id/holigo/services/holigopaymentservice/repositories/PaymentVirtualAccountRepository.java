package id.holigo.services.holigopaymentservice.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.PaymentVirtualAccount;

public interface PaymentVirtualAccountRepository extends JpaRepository<PaymentVirtualAccount, UUID> {

    Optional<PaymentVirtualAccount> findByAccountNumberAndStatus(String accountNumber, PaymentStatusEnum status);

    Optional<PaymentVirtualAccount> findByCallbackId(Long callbackId);
}
