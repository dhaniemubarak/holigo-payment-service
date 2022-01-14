package id.holigo.services.holigopaymentservice.web.controllers;

import javax.jms.JMSException;
import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.services.PaymentService;
import id.holigo.services.holigopaymentservice.web.mappers.PaymentMapper;
import id.holigo.services.holigopaymentservice.web.model.RequestPaymentDto;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class PaymentController {

    @Autowired
    private final PaymentMapper paymentMapper;

    @Autowired
    private final PaymentService paymentService;

    @PostMapping("/api/v1/payments")
    public ResponseEntity<HttpStatus> createPaymet(@Valid @RequestBody RequestPaymentDto requestPaymentDto,
            @RequestHeader("user-id") Long userId) throws JsonMappingException, JsonProcessingException, JMSException {
        Payment payment = paymentMapper.requestPaymentDtoToPayment(requestPaymentDto);
        payment.setUserId(userId);
        Payment savedPayment = paymentService.createPayment(payment);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(
                UriComponentsBuilder.fromPath("/api/v1/payments/{id}").buildAndExpand(savedPayment.getId()).toUri());
        return new ResponseEntity<HttpStatus>(httpHeaders, HttpStatus.CREATED);
    }
}