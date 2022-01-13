package id.holigo.services.holigopaymentservice.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import id.holigo.services.holigopaymentservice.domain.PaymentMethod;
import id.holigo.services.holigopaymentservice.repositories.PaymentMethodRepository;

@Service
public class PaymentMethodServiceImpl implements PaymentMethodService {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Override
    public List<PaymentMethod> getShowPaymentMethod() {

        List<PaymentMethod> paymentMethods = paymentMethodRepository.findAllByIsShow(true);
        return paymentMethods;
    }

}
