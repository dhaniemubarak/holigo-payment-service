package id.holigo.services.holigopaymentservice.web.controllers;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.PaymentVirtualAccount;
import id.holigo.services.holigopaymentservice.repositories.PaymentVirtualAccountRepository;
import id.holigo.services.holigopaymentservice.web.mappers.PaymentVirtualAccountMapper;
import id.holigo.services.holigopaymentservice.web.model.PaymentVirtualAccountDto;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class PaymentVirtualAccountController {

    private final PaymentVirtualAccountMapper paymentVirtualAccountMapper;

    private final PaymentVirtualAccountRepository paymentVirtualAccountRepository;

    @GetMapping("/api/v1/paymentVirtualAccounts")
    public ResponseEntity<PaymentVirtualAccountDto> index(@Valid @RequestParam("accountNumber") String accountNumber) {

        Optional<PaymentVirtualAccount> fetchPaymentVirtualAccount = paymentVirtualAccountRepository
                .findByAccountNumberAndStatus(accountNumber, PaymentStatusEnum.WAITING_PAYMENT);

        return fetchPaymentVirtualAccount.map(paymentVirtualAccount -> new ResponseEntity<>(paymentVirtualAccountMapper
                .paymentVirtualAccountToPaymentVirtualAccountDto(paymentVirtualAccount, false, false),
                HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
