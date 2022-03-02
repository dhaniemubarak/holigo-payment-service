package id.holigo.services.holigopaymentservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import id.holigo.services.holigopaymentservice.domain.PaymentService;

public interface PaymentServiceRepository extends JpaRepository<PaymentService, String> {

}
