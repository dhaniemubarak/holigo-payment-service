package id.holigo.services.holigopaymentservice.web.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import id.holigo.services.holigopaymentservice.services.PaymentMethodService;
import id.holigo.services.holigopaymentservice.web.mappers.PaymentMethodMapper;
import id.holigo.services.holigopaymentservice.web.model.PaymentMethodDto;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@RestController
public class PaymentMethodControllers {

    @Autowired
    private PaymentMethodService paymentMethodService;

    @Autowired
    private PaymentMethodMapper paymentMethodMapper;

    @GetMapping("/api/v1/payment_methods")
    public ResponseEntity<List<PaymentMethodDto>> index() {
        return new ResponseEntity<>(
                paymentMethodService.getShowPaymentMethod().stream()
                        .map(paymentMethodMapper::paymentMethodToPaymentMethodDto).collect(Collectors.toList()),
                HttpStatus.OK);
    }
}
