package id.holigo.services.holigopaymentservice.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import id.holigo.services.holigopaymentservice.domain.PaymentBankTransfer;

public interface PaymentBankTransferRepository extends JpaRepository<PaymentBankTransfer, UUID> {

}
