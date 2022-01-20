package id.holigo.services.holigopaymentservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import id.holigo.services.holigopaymentservice.domain.BankTransferCallback;

public interface BankTransferCallbackRepository extends JpaRepository<BankTransferCallback, Long> {

}
