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

    @Autowired
    private final PaymentVirtualAccountMapper paymentVirtualAccountMapper;

    @Autowired
    private final PaymentVirtualAccountRepository paymentVirtualAccountRepository;

    @GetMapping("/api/v1/paymentVirtualAccounts")
    public ResponseEntity<PaymentVirtualAccountDto> index(@Valid @RequestParam("accountNumber") String accountNumber) {

        Optional<PaymentVirtualAccount> fetchPaymentVirtualAccount = paymentVirtualAccountRepository
                .findByAccountNumberAndStatus(accountNumber, PaymentStatusEnum.WAITING_PAYMENT);

        if (fetchPaymentVirtualAccount.isPresent()) {
            return new ResponseEntity<>(paymentVirtualAccountMapper
                    .paymentVirtualAccountToPaymentVirtualAccountDto(fetchPaymentVirtualAccount.get(), false, false),
                    HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
