package id.holigo.services.holigopaymentservice.web.controllers;

import java.util.Optional;
import java.util.UUID;

import javax.jms.JMSException;
import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import id.holigo.services.common.model.PaymentDtoForUser;
import id.holigo.services.holigopaymentservice.domain.Payment;

import id.holigo.services.holigopaymentservice.domain.PaymentBankTransfer;
import id.holigo.services.holigopaymentservice.domain.PaymentVirtualAccount;
import id.holigo.services.holigopaymentservice.repositories.PaymentBankTransferRepository;
import id.holigo.services.holigopaymentservice.repositories.PaymentRepository;
import id.holigo.services.holigopaymentservice.repositories.PaymentServiceRepository;
import id.holigo.services.holigopaymentservice.repositories.PaymentVirtualAccountRepository;
import id.holigo.services.holigopaymentservice.services.PaymentService;
import id.holigo.services.holigopaymentservice.web.exceptions.NotFoundException;
import id.holigo.services.holigopaymentservice.web.mappers.PaymentBankTransferMapper;
import id.holigo.services.holigopaymentservice.web.mappers.PaymentMapper;
import id.holigo.services.holigopaymentservice.web.mappers.PaymentVirtualAccountMapper;
import id.holigo.services.holigopaymentservice.web.model.RequestPaymentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PaymentController {

    @Autowired
    private final PaymentMapper paymentMapper;

    @Autowired
    private final PaymentService paymentService;

    @Autowired
    private final PaymentRepository paymentRepository;

    @Autowired
    private final PaymentBankTransferRepository paymentBankTransferRepository;

    @Autowired
    private final PaymentVirtualAccountRepository paymentVirtualAccountRepository;

    @Autowired
    private final PaymentBankTransferMapper paymentBankTransferMapper;

    @Autowired
    private final PaymentVirtualAccountMapper paymentVirtualAccountMapper;

    @Autowired
    private final PaymentServiceRepository paymentServiceRepository;

    @PostMapping("/api/v1/payments")
    public ResponseEntity<HttpStatus> createPayment(@Valid @RequestBody RequestPaymentDto requestPaymentDto,
            @RequestHeader("user-id") Long userId) throws JsonMappingException, JsonProcessingException, JMSException {
        Optional<id.holigo.services.holigopaymentservice.domain.PaymentService> fetchPaymentService = paymentServiceRepository
                .findById(requestPaymentDto.getPaymentServiceId());
        if (fetchPaymentService.isEmpty()) {
            throw new NotFoundException("Payment method not found");
        }
        Payment payment = paymentMapper.requestPaymentDtoToPayment(requestPaymentDto);
        payment.setPaymentService(fetchPaymentService.get());
        payment.setUserId(userId);
        Payment savedPayment = paymentService.createPayment(payment);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(
                UriComponentsBuilder.fromPath("/api/v1/payments/{id}").buildAndExpand(savedPayment.getId()).toUri());
        return new ResponseEntity<HttpStatus>(httpHeaders, HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/payments/{id}")
    public ResponseEntity<PaymentDtoForUser> getPayment(@Valid @PathVariable("id") UUID id) {
        Optional<Payment> fetchPayment = paymentRepository.findById(id);
        if (fetchPayment.isEmpty()) {
            throw new NotFoundException("Transaction not found");
        }
        PaymentDtoForUser paymentDtoForUser = paymentMapper.paymentToPaymentDtoForUser(fetchPayment.get());
        return new ResponseEntity<>(paymentDtoForUser, HttpStatus.OK);
    }

    @GetMapping("/api/v1/payments/{paymentId}/{paymentService}/{detailId}")
    public ResponseEntity<?> getPaymentDetail(
            @RequestHeader("user-id") Long userId,
            @PathVariable("paymentId") UUID paymentId,
            @PathVariable("paymentService") String paymentService,
            @PathVariable("detailId") UUID detailId) {
        ResponseEntity<?> response;
        switch (paymentService) {
            case "bankTransfer":
                log.info("bank transfer is running with id -> {}", detailId);
                Optional<PaymentBankTransfer> fetchPaymentBankTransfer = paymentBankTransferRepository
                        .findById(detailId);
                if (fetchPaymentBankTransfer.isEmpty()) {
                    throw new NotFoundException("Detail not found.");
                }
                response = new ResponseEntity<>(paymentBankTransferMapper
                        .paymentBankTransferToPaymentBankTransferDto(fetchPaymentBankTransfer.get()), HttpStatus.OK);
                break;
            case "virtualAccount":
                Optional<PaymentVirtualAccount> fetchPaymentVirtualAccount = paymentVirtualAccountRepository
                        .findById(detailId);
                if (fetchPaymentVirtualAccount.isEmpty()) {
                    throw new NotFoundException("Detail not found.");
                }
                response = new ResponseEntity<>(paymentVirtualAccountMapper
                        .paymentVirtualAccountToPaymentVirtualAccountDto(fetchPaymentVirtualAccount.get(), true, true),
                        HttpStatus.OK);
                break;
            default:
                response = new ResponseEntity<>(HttpStatus.NOT_FOUND);
                break;
        }
        return response;
    }
}