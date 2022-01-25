package id.holigo.services.holigopaymentservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import id.holigo.services.holigopaymentservice.domain.CompanyBankAccount;

public interface CompanyBankAccountRepository extends JpaRepository<CompanyBankAccount, Integer> {

    CompanyBankAccount getByPaymentServiceId(String paymentServiceId);
}
