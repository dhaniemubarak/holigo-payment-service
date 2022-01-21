package id.holigo.services.holigopaymentservice.repositories;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.PaymentBankTransfer;

public interface PaymentBankTransferRepository extends JpaRepository<PaymentBankTransfer, UUID> {
    Optional<PaymentBankTransfer> findByPaymentServiceIdAndBillAmountAndStatus(String paymentServiceId,
            BigDecimal billAmount, PaymentStatusEnum status);
}
