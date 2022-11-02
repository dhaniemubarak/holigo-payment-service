package id.holigo.services.holigopaymentservice.web.controllers;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import javax.jms.JMSException;
import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import feign.FeignException;
import id.holigo.services.common.model.AccountBalanceDto;
import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.common.model.TransactionDto;
import id.holigo.services.holigopaymentservice.domain.*;
import id.holigo.services.holigopaymentservice.repositories.*;
import id.holigo.services.holigopaymentservice.services.StatusPaymentService;
import id.holigo.services.holigopaymentservice.services.accountBalance.AccountBalanceService;
import id.holigo.services.holigopaymentservice.services.transaction.TransactionService;
import id.holigo.services.holigopaymentservice.web.exceptions.ForbiddenException;
import id.holigo.services.holigopaymentservice.web.mappers.PaymentDigitalWalletMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
import id.holigo.services.holigopaymentservice.services.PaymentService;
import id.holigo.services.holigopaymentservice.web.exceptions.NotFoundException;
import id.holigo.services.holigopaymentservice.web.mappers.PaymentBankTransferMapper;
import id.holigo.services.holigopaymentservice.web.mappers.PaymentMapper;
import id.holigo.services.holigopaymentservice.web.mappers.PaymentVirtualAccountMapper;
import id.holigo.services.holigopaymentservice.web.model.RequestPaymentDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class PaymentController {

    private PaymentMapper paymentMapper;

    private PaymentService paymentService;

    private PaymentRepository paymentRepository;

    private PaymentBankTransferRepository paymentBankTransferRepository;

    private PaymentVirtualAccountRepository paymentVirtualAccountRepository;

    private PaymentBankTransferMapper paymentBankTransferMapper;

    private PaymentVirtualAccountMapper paymentVirtualAccountMapper;

    private PaymentServiceRepository paymentServiceRepository;

    private AccountBalanceService accountBalanceService;

    private StatusPaymentService statusPaymentService;

    private TransactionService transactionService;

    private MessageSource messageSource;

    private PaymentForbiddenRepository paymentForbiddenRepository;

    private PaymentDepositRepository paymentDepositRepository;

    private PaymentPointRepository paymentPointRepository;

    private PaymentDigitalWalletRepository paymentDigitalWalletRepository;

    private PaymentDigitalWalletMapper paymentDigitalWalletMapper;

    @Autowired
    public void setPaymentDigitalWalletMapper(PaymentDigitalWalletMapper paymentDigitalWalletMapper) {
        this.paymentDigitalWalletMapper = paymentDigitalWalletMapper;
    }

    @Autowired
    public void setPaymentPointRepository(PaymentPointRepository paymentPointRepository) {
        this.paymentPointRepository = paymentPointRepository;
    }

    @Autowired
    public void setPaymentDigitalWalletRepository(PaymentDigitalWalletRepository paymentDigitalWalletRepository) {
        this.paymentDigitalWalletRepository = paymentDigitalWalletRepository;
    }

    @Autowired
    public void setPaymentDepositRepository(PaymentDepositRepository paymentDepositRepository) {
        this.paymentDepositRepository = paymentDepositRepository;
    }

    @Autowired
    public void setPaymentForbiddenRepository(PaymentForbiddenRepository paymentForbiddenRepository) {
        this.paymentForbiddenRepository = paymentForbiddenRepository;
    }

    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Autowired
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Autowired
    public void setStatusPaymentService(StatusPaymentService statusPaymentService) {
        this.statusPaymentService = statusPaymentService;
    }

    @Autowired
    public void setAccountBalanceService(AccountBalanceService accountBalanceService) {
        this.accountBalanceService = accountBalanceService;
    }

    @Autowired
    public void setPaymentBankTransferMapper(PaymentBankTransferMapper paymentBankTransferMapper) {
        this.paymentBankTransferMapper = paymentBankTransferMapper;
    }

    @Autowired
    public void setPaymentBankTransferRepository(PaymentBankTransferRepository paymentBankTransferRepository) {
        this.paymentBankTransferRepository = paymentBankTransferRepository;
    }

    @Autowired
    public void setPaymentMapper(PaymentMapper paymentMapper) {
        this.paymentMapper = paymentMapper;
    }

    @Autowired
    public void setPaymentRepository(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Autowired
    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Autowired
    public void setPaymentVirtualAccountRepository(PaymentVirtualAccountRepository paymentVirtualAccountRepository) {
        this.paymentVirtualAccountRepository = paymentVirtualAccountRepository;
    }

    @Autowired
    public void setPaymentServiceRepository(PaymentServiceRepository paymentServiceRepository) {
        this.paymentServiceRepository = paymentServiceRepository;
    }

    @Autowired
    public void setPaymentVirtualAccountMapper(PaymentVirtualAccountMapper paymentVirtualAccountMapper) {
        this.paymentVirtualAccountMapper = paymentVirtualAccountMapper;
    }

    @PostMapping("/api/v1/payments")
    public ResponseEntity<HttpStatus> createPayment(@Valid @RequestBody RequestPaymentDto requestPaymentDto,
                                                    @RequestHeader("user-id") Long userId) throws JsonProcessingException, JMSException {
        Optional<id.holigo.services.holigopaymentservice.domain.PaymentService> fetchPaymentService = paymentServiceRepository
                .findById(requestPaymentDto.getPaymentServiceId());
        if (fetchPaymentService.isEmpty()) {
            throw new NotFoundException("Payment method not found");
        }
        if (requestPaymentDto.getPointAmount() == null) {
            requestPaymentDto.setPointAmount(BigDecimal.ZERO);
        }
        if (requestPaymentDto.getDepositAmount() == null) {
            requestPaymentDto.setDepositAmount(BigDecimal.ZERO);
        }
        TransactionDto transactionDto = transactionService.getTransaction(requestPaymentDto.getTransactionId());

        if (transactionDto.getUserId() == null) {
            throw new NotFoundException(messageSource.getMessage("holigo-transaction-service.not_found", null,
                    LocaleContextHolder.getLocale()));
        }

        if (transactionDto.getUserId().longValue() != userId.longValue()) {
            throw new ForbiddenException(messageSource.getMessage("payment.user_transaction_not_match", null,
                    LocaleContextHolder.getLocale()));
        }

        Optional<PaymentForbidden> fetchPaymentForbidden = paymentForbiddenRepository
                .findByPaymentServiceIdAndProductId(requestPaymentDto.getPaymentServiceId(), transactionDto.getProductId());
        if (fetchPaymentForbidden.isPresent()) {
            throw new ForbiddenException(messageSource.getMessage("payment.forbidden_product", null,
                    LocaleContextHolder.getLocale()));
        }

        if (transactionDto.getPaymentStatus() != PaymentStatusEnum.SELECTING_PAYMENT
                && transactionDto.getPaymentStatus() != PaymentStatusEnum.WAITING_PAYMENT) {
            String message = statusPaymentService.getStatusMessage(transactionDto.getPaymentStatus());

            throw new ForbiddenException(message);
        }

        if (transactionDto.getPaymentStatus() == PaymentStatusEnum.WAITING_PAYMENT) {
            Payment waitingPayment = paymentRepository.getById(transactionDto.getPaymentId());
            paymentService.cancelPayment(waitingPayment, transactionDto);
        }
        AccountBalanceDto accountBalanceDto = null;
        try {
            accountBalanceDto = accountBalanceService.getAccountBalance(userId);
        } catch (FeignException e) {
            log.info("Error -> {}", e.getMessage());
        }

        // Cek apakah layanan pembayaran dibuka atau di tutup untuk produk yang mau
        // dibeli
        if (requestPaymentDto.getPaymentServiceId().equals("POINT") ||
                requestPaymentDto.getPaymentServiceId().equals("DEPOSIT") ||
                requestPaymentDto.getPointAmount().compareTo(BigDecimal.ZERO) > 0 ||
                requestPaymentDto.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
//            Boolean isValid = pinService.validate(
//                    PinValidationDto.builder().pin(requestPaymentDto.getPin()).build(), userId);
//            if (!isValid) {
//                throw new NotAcceptableException();
//            }
            if (accountBalanceDto == null) {
                throw new ForbiddenException("account balance not found");
            }
            if (accountBalanceDto.getPoint() == null) {
                accountBalanceDto.setPoint(0);
            }
            if (accountBalanceDto.getDeposit() == null) {
                accountBalanceDto.setDeposit(BigDecimal.ZERO);
            }
            if (requestPaymentDto.getPointAmount().compareTo(BigDecimal.valueOf(accountBalanceDto.getPoint())) > 0) {
                throw new ForbiddenException("Point tidak cukup");
            }
            if (requestPaymentDto.getDepositAmount().compareTo(accountBalanceDto.getDeposit()) > 0) {
                throw new ForbiddenException("Saldo tidak cukup");
            }
        }

        Payment payment = paymentMapper.requestPaymentDtoToPayment(requestPaymentDto);
        payment.setId(UUID.randomUUID());
        payment.setPaymentService(fetchPaymentService.get());
        payment.setUserId(userId);

        Payment savedPayment = paymentService.createPayment(payment, transactionDto, accountBalanceDto);
        switch (savedPayment.getPaymentService().getId()) {
            case "DEPOSIT" -> {
                PaymentDeposit paymentDeposit = paymentDepositRepository.getById(UUID.fromString(savedPayment.getDetailId()));
                if (paymentDeposit.getStatus().equals(PaymentStatusEnum.PAID)) {
                    paymentService.paymentHasBeenPaid(savedPayment.getId());
//                    transactionService.issuedTransaction(savedPayment.getTransactionId(), savedPayment);
                }
            }
            case "POINT" -> {
                PaymentPoint paymentPoint = paymentPointRepository.getById(UUID.fromString(savedPayment.getDetailId()));
                if (paymentPoint.getStatus().equals(PaymentStatusEnum.PAID)) {
                    paymentService.paymentHasBeenPaid(savedPayment.getId());
//                    transactionService.issuedTransaction(savedPayment.getTransactionId(), savedPayment);
                }
            }
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(
                UriComponentsBuilder.fromPath("/api/v1/payments/{id}").
                        buildAndExpand(savedPayment.getId()).
                        toUri());
        return new ResponseEntity<>(httpHeaders, HttpStatus.CREATED);
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
            case "bankTransfer" -> {
                Optional<PaymentBankTransfer> fetchPaymentBankTransfer = paymentBankTransferRepository
                        .findById(detailId);
                if (fetchPaymentBankTransfer.isEmpty()) {
                    throw new NotFoundException("Detail not found.");
                }
                response = new ResponseEntity<>(paymentBankTransferMapper
                        .paymentBankTransferToPaymentBankTransferDto(fetchPaymentBankTransfer.get()), HttpStatus.OK);
            }
            case "virtualAccount" -> {
                Optional<PaymentVirtualAccount> fetchPaymentVirtualAccount = paymentVirtualAccountRepository
                        .findById(detailId);
                if (fetchPaymentVirtualAccount.isEmpty()) {
                    throw new NotFoundException("Detail not found.");
                }
                response = new ResponseEntity<>(paymentVirtualAccountMapper
                        .paymentVirtualAccountToPaymentVirtualAccountDto(fetchPaymentVirtualAccount.get(), true, true),
                        HttpStatus.OK);
            }
            case "digitalWallet" -> {
                Optional<PaymentDigitalWallet> fetchPaymentDigitalWallet = paymentDigitalWalletRepository.findById(detailId);
                if (fetchPaymentDigitalWallet.isEmpty()) {
                    throw new NotFoundException("Detail not found");
                }
                response = new ResponseEntity<>(paymentDigitalWalletMapper
                        .paymentDigitalWalletToPaymentDigitalWalletDto(fetchPaymentDigitalWallet.get()), HttpStatus.OK);
            }
            default -> response = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return response;
    }
}