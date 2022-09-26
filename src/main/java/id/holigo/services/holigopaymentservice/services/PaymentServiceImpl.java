package id.holigo.services.holigopaymentservice.services;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.jms.JMSException;
import javax.transaction.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import id.holigo.services.common.model.*;
import id.holigo.services.holigopaymentservice.domain.*;
import id.holigo.services.holigopaymentservice.repositories.PaymentForbiddenRepository;
import id.holigo.services.holigopaymentservice.services.coupon.CouponService;
import id.holigo.services.holigopaymentservice.services.deposit.DepositService;
import id.holigo.services.holigopaymentservice.services.pin.PinService;
import id.holigo.services.holigopaymentservice.services.point.PointService;
import id.holigo.services.holigopaymentservice.web.exceptions.CouponInvalidException;
import id.holigo.services.holigopaymentservice.web.exceptions.NotAcceptableException;
import id.holigo.services.holigopaymentservice.web.model.PinValidationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import id.holigo.services.holigopaymentservice.events.PaymentStatusEvent;
import id.holigo.services.holigopaymentservice.repositories.PaymentRepository;
import id.holigo.services.holigopaymentservice.services.transaction.TransactionService;
import id.holigo.services.holigopaymentservice.web.exceptions.ForbiddenException;
import id.holigo.services.holigopaymentservice.web.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    public static final String PAYMENT_HEADER = "payment_id";

    private TransactionService transactionService;

    private MessageSource messageSource;

    private StatusPaymentService statusPaymentService;

    private PaymentRepository paymentRepository;

    private PaymentBankTransferService paymentBankTransferService;

    private PaymentVirtualAccountService paymentVirtualAccountService;

    private CouponService couponService;

    private PaymentForbiddenRepository paymentForbiddenRepository;

    private PinService pinService;

    private PointService pointService;

    private DepositService depositService;

    @Autowired
    public void setDepositService(DepositService depositService) {
        this.depositService = depositService;
    }

    @Autowired
    public void setPointService(PointService pointService) {
        this.pointService = pointService;
    }

    @Autowired
    public void setPinService(PinService pinService) {
        this.pinService = pinService;
    }

    @Autowired
    public void setPaymentForbiddenRepository(PaymentForbiddenRepository paymentForbiddenRepository) {
        this.paymentForbiddenRepository = paymentForbiddenRepository;
    }

    private final StateMachineFactory<PaymentStatusEnum, PaymentStatusEvent> stateMachineFactory;

    private final PaymentInterceptor paymentInterceptor;

    @Autowired
    public void setPaymentRepository(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
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
    public void setPaymentBankTransferService(PaymentBankTransferService paymentBankTransferService) {
        this.paymentBankTransferService = paymentBankTransferService;
    }

    @Autowired
    public void setPaymentVirtualAccountService(PaymentVirtualAccountService paymentVirtualAccountService) {
        this.paymentVirtualAccountService = paymentVirtualAccountService;
    }

    @Autowired
    public void setStatusPaymentService(StatusPaymentService statusPaymentService) {
        this.statusPaymentService = statusPaymentService;
    }

    @Autowired
    public void setCouponService(CouponService couponService) {
        this.couponService = couponService;
    }

    @Override
    public Payment createPayment(Payment payment) throws JsonProcessingException, JMSException {

        TransactionDto transactionDto = transactionService.getTransaction(payment.getTransactionId());

        if (transactionDto.getUserId() == null) {
            throw new NotFoundException(messageSource.getMessage("holigo-transaction-service.not_found", null,
                    LocaleContextHolder.getLocale()));
        }

        if (transactionDto.getUserId().longValue() != payment.getUserId().longValue()) {
            throw new ForbiddenException(messageSource.getMessage("payment.user_transaction_not_match", null,
                    LocaleContextHolder.getLocale()));
        }

        if (transactionDto.getPaymentStatus() != PaymentStatusEnum.SELECTING_PAYMENT
                && transactionDto.getPaymentStatus() != PaymentStatusEnum.WAITING_PAYMENT) {
            String message = statusPaymentService.getStatusMessage(transactionDto.getPaymentStatus());

            throw new ForbiddenException(message);
        }

        if (transactionDto.getPaymentStatus() == PaymentStatusEnum.WAITING_PAYMENT) {
            Payment waitingPayment = paymentRepository.getById(transactionDto.getPaymentId());
            cancelPayment(waitingPayment);
        }

        // Cek apakah layanan pembayaran dibuka atau di tutup untuk produk yang mau
        // dibeli
        Optional<PaymentForbidden> fetchPaymentForbidden = paymentForbiddenRepository
                .findByPaymentServiceAndProductId(payment.getPaymentService(), transactionDto.getProductId());
        if (fetchPaymentForbidden.isPresent()) {
            throw new ForbiddenException(messageSource.getMessage("payment.forbidden_product", null,
                    LocaleContextHolder.getLocale()));
        }

        // Check for voucher
        BigDecimal discountAmount = BigDecimal.valueOf(0.00);
        if (payment.getCouponCode() != null) {
            // Get coupon value
            ApplyCouponDto applyCouponDto = couponService.applyCoupon(payment.getTransactionId(), payment.getCouponCode(),
                    payment.getPaymentService().getId(), payment.getUserId());
            if (applyCouponDto.getIsValid()) {
                // POST coupon
                applyCouponDto = couponService.createApplyCoupon(applyCouponDto);
                if (applyCouponDto.getId() != null) {
                    payment.setIsFreeAdmin(applyCouponDto.getIsFreeAdmin());
                    payment.setIsFreeServiceFee(applyCouponDto.getIsFreeServiceFee());
                    if (applyCouponDto.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                        discountAmount = applyCouponDto.getDiscountAmount();
                    }
                    payment.setApplyCouponId(applyCouponDto.getId());
                }
            } else {
                throw new CouponInvalidException(applyCouponDto.getMessage(), null, false, false);
            }
        }


        BigDecimal totalAmount = transactionDto.getFareAmount().subtract(payment.getDiscountAmount());

        BigDecimal pointAmount = BigDecimal.ZERO;

        if (payment.getPointAmount().compareTo(BigDecimal.ZERO) > 0) {
            Boolean isValid = pinService.validate(
                    PinValidationDto.builder().pin(payment.getPin()).build(), payment.getUserId());
            if (!isValid) {
                throw new NotAcceptableException();
            }
            // Point debit
            pointAmount = BigDecimal.valueOf(debitPoint(payment, transactionDto));
            totalAmount = totalAmount.subtract(pointAmount);
        }
        BigDecimal depositAmount = BigDecimal.ZERO;
        if (payment.getPaymentService().getId().equals("DEPOSIT")) {
            payment.setDepositAmount(depositAmount);
        }
        if (payment.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
            Boolean isValid = pinService.validate(
                    PinValidationDto.builder().pin(payment.getPin()).build(), payment.getUserId());
            if (!isValid) {
                throw new NotAcceptableException();
            }
            depositAmount = debitDeposit(payment.getDepositAmount(), payment, transactionDto);
            totalAmount = totalAmount.subtract(depositAmount);
        }
        payment.setDiscountAmount(discountAmount);
        payment.setPointAmount(pointAmount);
        payment.setDepositAmount(depositAmount);

        // Switch selected payment
        BigDecimal paymentServiceAmount = totalAmount;
        BigDecimal serviceFeeAmount = BigDecimal.valueOf(0.00);
        BigDecimal remainingAmount = paymentServiceAmount;
        String detailType;
        String detailId = UUID.randomUUID().toString();
        switch (payment.getPaymentService().getId()) {
            case "DEPOSIT" -> {
                Boolean isValid = pinService.validate(
                        PinValidationDto.builder().pin(payment.getPin()).build(), payment.getUserId());
                if (!isValid) {
                    throw new NotAcceptableException();
                }
                PaymentDeposit paymentDeposit = new PaymentDeposit();
                paymentDeposit.
                depositAmount = debitDeposit(payment.getPaymentServiceAmount(), payment, transactionDto);
                if (depositAmount.equals(payment.getPaymentServiceAmount())) {
                    remainingAmount = BigDecimal.ZERO;
                    // get detail id
                }
                detailType = "deposit";
                detailId = "";
            }
            case "BT_BCA", "BT_MANDIRI", "BT_BNI", "BT_BSI" -> {
                PaymentBankTransfer paymentBankTransfer = paymentBankTransferService
                        .createNewBankTransfer(transactionDto, payment);
                detailType = "bankTransfer";
                detailId = paymentBankTransfer.getId().toString();
                paymentServiceAmount = paymentBankTransfer.getBillAmount();
                remainingAmount = paymentBankTransfer.getBillAmount();
                serviceFeeAmount = paymentBankTransfer.getServiceFeeAmount();
                totalAmount = paymentBankTransfer.getTotalAmount();
            }
            case "VA_BCA", "VA_MANDIRI", "VA_BNI" -> {

                /*
                  Jika memilih virtual account, pastikan tagihan virtual account yang aktif
                  pada metode pembayaran virtual account dengan bank yang dipilih
                  untuk user tersebut hanya satu. Maka lakukan validasi terlebih dahulu

                  Untuk sementara batalkan terlebih dahulu jika ditemukan payment
                  yang sudah ada. Untuk memastikan pembayaran via virtual account
                  hanya bisa 1 pembayaran yang statusnya menunggu

                 */
                List<Payment> payments = paymentRepository.findAllByUserIdAndStatusAndPaymentServiceId(
                        payment.getUserId(), PaymentStatusEnum.WAITING_PAYMENT, payment.getPaymentService().getId());
                for (Payment pay : payments) {
                    cancelPayment(pay);
                }
                PaymentVirtualAccount paymentVirtualAccount = paymentVirtualAccountService
                        .createNewVirtualAccount(transactionDto, payment);
                detailType = "virtualAccount";
                detailId = paymentVirtualAccount.getId().toString();
                paymentServiceAmount = paymentVirtualAccount.getBillAmount();
                remainingAmount = paymentVirtualAccount.getBillAmount();
                serviceFeeAmount = paymentVirtualAccount.getServiceFeeAmount();
                totalAmount = paymentVirtualAccount.getTotalAmount();
            }
            case "CC_ALL" -> detailType = "creditCard";
            default -> detailType = "undefined";
        }

        // Set payment
        payment.setFareAmount(transactionDto.getFareAmount());
        payment.setDiscountAmount(discountAmount);
        payment.setServiceFeeAmount(serviceFeeAmount);
        payment.setPointAmount(pointAmount);
        payment.setTotalAmount(totalAmount);
        payment.setPaymentServiceAmount(paymentServiceAmount);
        payment.setRemainingAmount(remainingAmount);
        payment.setStatus(PaymentStatusEnum.WAITING_PAYMENT);
        payment.setDetailType(detailType);
        payment.setDetailId(detailId);

        // Create payment after get callback from supplier
        Payment savedPayment = paymentRepository.save(payment);
        transactionService.setPaymentInTransaction(payment.getTransactionId(), payment);
        return savedPayment;
    }

    @Transactional
    @Override
    public StateMachine<PaymentStatusEnum, PaymentStatusEvent> paymentHasBeenPaid(UUID id) {
        StateMachine<PaymentStatusEnum, PaymentStatusEvent> sm = build(id);
        sendEvent(id, sm, PaymentStatusEvent.PAYMENT_PAID);
        return sm;
    }

    @Transactional
    @Override
    public StateMachine<PaymentStatusEnum, PaymentStatusEvent> refundPayment(UUID id) {
        StateMachine<PaymentStatusEnum, PaymentStatusEvent> sm = build(id);
        sendEvent(id, sm, PaymentStatusEvent.PAYMENT_REFUND);
        return sm;
    }

    @Transactional
    @Override
    public StateMachine<PaymentStatusEnum, PaymentStatusEvent> paymentExpired(UUID id) {
        StateMachine<PaymentStatusEnum, PaymentStatusEvent> sm = build(id);
        sendEvent(id, sm, PaymentStatusEvent.PAYMENT_EXPIRED);
        return sm;
    }

    @Override
    public StateMachine<PaymentStatusEnum, PaymentStatusEvent> paymentCanceled(UUID id) {
        StateMachine<PaymentStatusEnum, PaymentStatusEvent> sm = build(id);
        sendEvent(id, sm, PaymentStatusEvent.PAYMENT_CANCEL);
        return sm;
    }


    private void sendEvent(UUID id,
                           StateMachine<PaymentStatusEnum, PaymentStatusEvent> sm, PaymentStatusEvent event) {
        Message<PaymentStatusEvent> message = MessageBuilder.withPayload(event)
                .setHeader(PAYMENT_HEADER, id).build();
        sm.sendEvent(message);
    }

    private StateMachine<PaymentStatusEnum, PaymentStatusEvent> build(UUID id) {
        Payment payment = paymentRepository.getById(id);

        StateMachine<PaymentStatusEnum, PaymentStatusEvent> sm = stateMachineFactory
                .getStateMachine(payment.getId().toString());

        sm.stop();
        sm.getStateMachineAccessor().doWithAllRegions(sma -> {
            sma.addStateMachineInterceptor(paymentInterceptor);
            sma.resetStateMachine(new DefaultStateMachineContext<>(
                    payment.getStatus(), null, null, null));
        });
        sm.start();
        return sm;
    }

    @Transactional
    @Override
    public void cancelPayment(Payment payment) {
        payment.setDeletedAt(Timestamp.valueOf(LocalDateTime.now()));
        payment.setStatus(PaymentStatusEnum.PAYMENT_CANCELED);
        Payment updatedPayment = paymentRepository.save(payment);
        // Refund point yang digunakan
        // Refund voucher yang digunakan
        switch (payment.getDetailType()) {
            case "virtualAccount" -> paymentVirtualAccountService.cancelPayment(UUID.fromString(payment.getDetailId()));
            case "bankTransfer" -> paymentBankTransferService.cancelPayment(UUID.fromString(payment.getDetailId()));
        }
        Payment pay = new Payment();
        pay.setId(updatedPayment.getId());
        pay.setStatus(PaymentStatusEnum.SELECTING_PAYMENT);
        pay.setPointAmount(new BigDecimal("0.00"));
        pay.setCouponCode(null);
        pay.setPaymentService(null);
        transactionService.setPaymentInTransaction(payment.getTransactionId(), pay);
//        }
    }

    private Integer debitPoint(Payment payment, TransactionDto transaction) {
        PointDto pointDto = PointDto.builder().debitAmount(payment.getPointAmount().intValue())
                .transactionId(payment.getTransactionId()).paymentId(payment.getId())
                .informationIndex("pointStatement.payment")
                .transactionType(transaction.getTransactionType())
                .invoiceNumber(transaction.getInvoiceNumber())
                .userId(transaction.getUserId())
                .build();
        PointDto resultPointDto;
        try {
            resultPointDto = pointService.credit(pointDto);
        } catch (JMSException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if (!resultPointDto.getIsValid()) {
            return 0;
        }
        return resultPointDto.getDebitAmount();
    }

    private BigDecimal debitDeposit(BigDecimal debitAmount, Payment payment, TransactionDto transactionDto) {
        DepositDto depositDto = DepositDto.builder()
                .category("PAYMENT")
                .debitAmount(debitAmount)
                .paymentId(payment.getId())
                .informationIndex("depositStatement.payment")
                .invoiceNumber(transactionDto.getInvoiceNumber())
                .transactionId(transactionDto.getId())
                .transactionType(transactionDto.getTransactionType())
                .userId(transactionDto.getUserId())
                .build();
        DepositDto resultDepositDto;
        try {
            resultDepositDto = depositService.credit(depositDto);
        } catch (JMSException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if (!resultDepositDto.getIsValid()) {
            return BigDecimal.ZERO;
        }
        return resultDepositDto.getDebitAmount();
    }

}