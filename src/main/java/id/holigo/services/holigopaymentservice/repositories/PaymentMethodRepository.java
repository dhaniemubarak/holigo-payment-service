package id.holigo.services.holigopaymentservice.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import id.holigo.services.holigopaymentservice.domain.PaymentMethod;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, String> {
    List<PaymentMethod> findAllByIsShowOrderByCreatedAtAsc(boolean isShow);
}
