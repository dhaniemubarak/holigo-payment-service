package id.holigo.services.holigopaymentservice.repositories;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;

import id.holigo.services.holigopaymentservice.domain.PaymentCallbackStatusEnum;
import id.holigo.services.holigopaymentservice.domain.VirtualAccountCallback;

public interface VirtualAccountCallbackRepository extends JpaRepository<VirtualAccountCallback, Long> {
    VirtualAccountCallback findByAccountNumberAndAmountAndProcessStatus(String accountNumber, BigDecimal amount,
            PaymentCallbackStatusEnum status);
}
