package id.holigo.services.holigopaymentservice.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import javax.jms.JMSException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.holigo.services.common.model.*;
import id.holigo.services.holigopaymentservice.domain.*;
import id.holigo.services.holigopaymentservice.interceptors.PaymentInterceptor;
import id.holigo.services.holigopaymentservice.repositories.PaymentDepositRepository;
import id.holigo.services.holigopaymentservice.repositories.PaymentPointRepository;
import id.holigo.services.holigopaymentservice.services.billing.BillingService;
import id.holigo.services.holigopaymentservice.services.coupon.CouponService;
import id.holigo.services.holigopaymentservice.services.deposit.DepositService;
import id.holigo.services.holigopaymentservice.services.logs.LogService;
import id.holigo.services.holigopaymentservice.services.point.PointService;
import id.holigo.services.holigopaymentservice.web.exceptions.CouponInvalidException;
import id.holigo.services.holigopaymentservice.web.model.RequestBillingStatusDto;
import id.holigo.services.holigopaymentservice.web.model.ResponseBillingStatusDto;
import id.holigo.services.holigopaymentservice.web.model.SupplierLogDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    public static final String PAYMENT_HEADER = "payment_id";

    private TransactionService transactionService;
    private PaymentRepository paymentRepository;

    private PaymentBankTransferService paymentBankTransferService;

    private PaymentVirtualAccountService paymentVirtualAccountService;

    private CouponService couponService;
    private PointService pointService;

    private DepositService depositService;

    private PaymentDepositRepository paymentDepositRepository;

    private PaymentPointRepository paymentPointRepository;

    private PaymentDigitalWalletService paymentDigitalWalletService;

    private BillingService billingService;

    private LogService logService;

    @Autowired
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    @Autowired
    public void setBillingService(BillingService billingService) {
        this.billingService = billingService;
    }

    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setPaymentDigitalWalletService(PaymentDigitalWalletService paymentDigitalWalletService) {
        this.paymentDigitalWalletService = paymentDigitalWalletService;
    }

    @Autowired
    public void setPaymentPointRepository(PaymentPointRepository paymentPointRepository) {
        this.paymentPointRepository = paymentPointRepository;
    }

    @Autowired
    public void setPaymentDepositRepository(PaymentDepositRepository paymentDepositRepository) {
        this.paymentDepositRepository = paymentDepositRepository;
    }

    @Autowired
    public void setDepositService(DepositService depositService) {
        this.depositService = depositService;
    }

    @Autowired
    public void setPointService(PointService pointService) {
        this.pointService = pointService;
    }

    private final StateMachineFactory<PaymentStatusEnum, PaymentStatusEvent> stateMachineFactory;

    private final PaymentInterceptor paymentInterceptor;

    @Autowired
    public void setPaymentRepository(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
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
    public void setCouponService(CouponService couponService) {
        this.couponService = couponService;
    }

    @Override
    public Payment createPayment(Payment payment, TransactionDto transactionDto, AccountBalanceDto accountBalanceDto) throws JsonProcessingException, JMSException {
        // Check for voucher
        BigDecimal discountAmount = BigDecimal.valueOf(0.00);
        if (payment.getCouponCode() != null && payment.getCouponCode().length() > 0) {
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
        payment.setDiscountAmount(discountAmount);
        BigDecimal totalAmount = transactionDto.getFareAmount().subtract(payment.getDiscountAmount());
        BigDecimal pointAmount = BigDecimal.ZERO;
        if (payment.getPointAmount().compareTo(BigDecimal.ZERO) > 0 && !payment.getPaymentService().getId().equals("POINT")) {
            payment.setIsSplitBill(true);
            pointAmount = BigDecimal.valueOf(debitPoint(payment.getPointAmount(), payment, transactionDto));
            if (pointAmount.equals(BigDecimal.ZERO)) {
                throw new ForbiddenException("Gagal menggunakan point");
            }
            totalAmount = totalAmount.subtract(pointAmount);
        }
        payment.setPointAmount(pointAmount);
        BigDecimal depositAmount = BigDecimal.ZERO;
        if (payment.getDepositAmount().compareTo(BigDecimal.ZERO) > 0 && !payment.getPaymentService().getId().equals("DEPOSIT")) {
            payment.setIsSplitBill(true);
            depositAmount = debitDeposit(payment.getDepositAmount(), payment, transactionDto);
            totalAmount = totalAmount.subtract(depositAmount);
        }
        PaymentStatusEnum paymentStatus = PaymentStatusEnum.WAITING_PAYMENT;
        // Switch selected payment
        BigDecimal paymentServiceAmount = totalAmount;
        BigDecimal serviceFeeAmount = BigDecimal.valueOf(0.00);
        BigDecimal remainingAmount = paymentServiceAmount.add(serviceFeeAmount);
        String detailType;
        String detailId = null;
        payment.setPaymentServiceAmount(paymentServiceAmount);
        switch (payment.getPaymentService().getId()) {
            case "DEPOSIT" -> {
                if (remainingAmount.compareTo(accountBalanceDto.getDeposit()) >= 0) {
                    // TODO refund point if use point
                    // TODO refund voucher if use private voucher
                    throw new ForbiddenException("Saldo Anda kurang");
                }
                PaymentDeposit paymentDeposit = new PaymentDeposit();
                paymentDeposit.setUserId(payment.getUserId());
                paymentDeposit.setServiceFeeAmount(BigDecimal.ZERO);
                paymentDeposit.setTotalAmount(paymentServiceAmount);
                paymentDeposit.setBillAmount(paymentDeposit.getTotalAmount().add(paymentDeposit.getServiceFeeAmount()));
                paymentDeposit.setStatus(PaymentStatusEnum.WAITING_PAYMENT);
                depositAmount = debitDeposit(paymentDeposit.getBillAmount(), payment, transactionDto);
                if (depositAmount.equals(paymentDeposit.getBillAmount())) {
                    paymentDeposit.setStatus(PaymentStatusEnum.PAID);
                } else {
                    throw new ForbiddenException("Gagal menggunakan Holi Cash");
                }
                paymentDepositRepository.save(paymentDeposit);
                detailType = "deposit";
                detailId = paymentDeposit.getId().toString();
            }
            case "POINT" -> {
                PaymentPoint paymentPoint = new PaymentPoint();
                paymentPoint.setUserId(payment.getUserId());
                paymentPoint.setServiceFeeAmount(BigDecimal.ZERO);
                paymentPoint.setTotalAmount(paymentServiceAmount);
                paymentPoint.setBillAmount(paymentPoint.getTotalAmount().add(paymentPoint.getServiceFeeAmount()));
                paymentPoint.setStatus(PaymentStatusEnum.WAITING_PAYMENT);
                pointAmount = BigDecimal.valueOf(debitPoint(paymentPoint.getBillAmount(), payment, transactionDto)).setScale(2, RoundingMode.UP);
                if (pointAmount.equals(paymentPoint.getBillAmount())) {
                    paymentPoint.setStatus(PaymentStatusEnum.PAID);
                } else {
                    throw new ForbiddenException("Gagal menggunakan point");
                }
                paymentPointRepository.save(paymentPoint);
                detailType = "point";
                detailId = paymentPoint.getId().toString();
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
            case "VA_BCA", "VA_MANDIRI", "VA_BNI", "VA_MAYBANK", "VA_PERMATA",
                    "VA_PERMATA_S", "VA_KEB_HANA", "VA_CIMB", "VA_BRI", "VA_DANAMON",
                    "VA_BJB", "VA_BNC", "VA_OB" -> {

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
                    cancelPayment(pay, transactionDto);
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
            case "DANA", "QRIS" -> {
                PaymentDigitalWallet paymentDigitalWallet = paymentDigitalWalletService
                        .createPaymentDigitalWallet(transactionDto, payment);
                detailType = "digitalWallet";
                detailId = paymentDigitalWallet.getId().toString();
                paymentServiceAmount = paymentDigitalWallet.getBillAmount();
                remainingAmount = paymentDigitalWallet.getBillAmount();
                serviceFeeAmount = paymentDigitalWallet.getServiceFeeAmount();
                totalAmount = paymentDigitalWallet.getTotalAmount();
            }
            case "CC_ALL" -> detailType = "creditCard";
            default -> detailType = "undefined";
        }

        // Set payment
        payment.setFareAmount(transactionDto.getFareAmount());
        payment.setDiscountAmount(discountAmount);
        payment.setServiceFeeAmount(serviceFeeAmount);
        payment.setPointAmount(pointAmount);
        payment.setDepositAmount(depositAmount);
        payment.setTotalAmount(totalAmount);
        payment.setPaymentServiceAmount(paymentServiceAmount);
        payment.setRemainingAmount(remainingAmount);
        payment.setStatus(paymentStatus);
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
    public void refundPayment(UUID id) {
        StateMachine<PaymentStatusEnum, PaymentStatusEvent> sm = build(id);
        sendEvent(id, sm, PaymentStatusEvent.PAYMENT_REFUND);
    }

    @Transactional
    @Override
    public void paymentExpired(UUID id) {
        StateMachine<PaymentStatusEnum, PaymentStatusEvent> sm = build(id);
        sendEvent(id, sm, PaymentStatusEvent.PAYMENT_EXPIRED);
    }

    @Override
    public void paymentCanceled(UUID id) {
        StateMachine<PaymentStatusEnum, PaymentStatusEvent> sm = build(id);
        sendEvent(id, sm, PaymentStatusEvent.PAYMENT_CANCEL);
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
    public void cancelPayment(Payment payment, TransactionDto transactionDto) {
        paymentCanceled(payment.getId());
        payment.setDeletedAt(Timestamp.valueOf(LocalDateTime.now()));
        Payment updatedPayment = paymentRepository.save(payment);
        switch (payment.getDetailType()) {
            case "virtualAccount" -> paymentVirtualAccountService.cancelPayment(UUID.fromString(payment.getDetailId()));
            case "bankTransfer" -> paymentBankTransferService.cancelPayment(UUID.fromString(payment.getDetailId()));
        }
        Payment pay = new Payment();
        pay.setId(updatedPayment.getId());
        pay.setStatus(PaymentStatusEnum.SELECTING_PAYMENT);
        pay.setPointAmount(new BigDecimal("0.00"));
        pay.setDiscountAmount(new BigDecimal("0.00"));
        pay.setCouponCode(null);
        pay.setPaymentService(null);
        transactionService.setPaymentInTransaction(payment.getTransactionId(), pay);
//        }
    }

    @Transactional
    @Override
    public void checkDepositStatus(Payment payment) {
        PaymentDeposit paymentDeposit = paymentDepositRepository.getById(UUID.fromString(payment.getDetailId()));
        if (paymentDeposit.getStatus().equals(PaymentStatusEnum.PAID)) {
            paymentHasBeenPaid(payment.getId());
        }
    }

    @Override
    public void checkStatus(PaymentVirtualAccount paymentVirtualAccount) {
        RequestBillingStatusDto requestBillingStatusDto = RequestBillingStatusDto.builder()
                .dev(false)
                .transactionId(paymentVirtualAccount.getInvoiceNumber()).build();
        ResponseBillingStatusDto responseBillingStatusDto = checkStatusFromBiller(requestBillingStatusDto, paymentVirtualAccount.getUserId(), paymentVirtualAccount.getPaymentService().getId());
        if (responseBillingStatusDto != null) {
            if (responseBillingStatusDto.getStatus()) {
                if (responseBillingStatusDto.getData().getTransactionStatus().equalsIgnoreCase("success") ||
                        responseBillingStatusDto.getData().getTransactionStatus().equalsIgnoreCase("paid")) {
                    paymentVirtualAccountService.paymentHasBeenPaid(paymentVirtualAccount.getId());
                }
                if (responseBillingStatusDto.getData().getTransactionStatus().equalsIgnoreCase("expired")) {
                    paymentVirtualAccountService.paymentHasBeenExpired(paymentVirtualAccount.getId());
                }
            }
        }
    }

    @Override
    public void checkStatus(PaymentDigitalWallet paymentDigitalWallet) {
        RequestBillingStatusDto requestBillingStatusDto = RequestBillingStatusDto.builder()
                .dev(false)
                .transactionId(paymentDigitalWallet.getInvoiceNumber()).build();
        ResponseBillingStatusDto responseBillingStatusDto = checkStatusFromBiller(requestBillingStatusDto, paymentDigitalWallet.getUserId(), paymentDigitalWallet.getPaymentCode());
        if (responseBillingStatusDto != null) {
            if (responseBillingStatusDto.getStatus()) {
                if (responseBillingStatusDto.getData().getTransactionStatus().equalsIgnoreCase("success") ||
                        responseBillingStatusDto.getData().getTransactionStatus().equalsIgnoreCase("paid")) {
                    paymentDigitalWalletService.paymentHasBeenPaid(paymentDigitalWallet.getId());
                }
                if (responseBillingStatusDto.getData().getTransactionStatus().equalsIgnoreCase("expired")) {
                    paymentDigitalWalletService.paymentHasBeenExpired(paymentDigitalWallet.getId());
                }
            }
        }
    }

    private ResponseBillingStatusDto checkStatusFromBiller(RequestBillingStatusDto requestBillingStatusDto, Long userId, String supplierCode) {
        ResponseBillingStatusDto responseBillingStatusDto = billingService.postCheckStatus(requestBillingStatusDto);
        try {
            SupplierLogDto supplierLogDto = SupplierLogDto.builder().build();
            supplierLogDto.setLogRequest(objectMapper.writeValueAsString(requestBillingStatusDto));
            supplierLogDto.setLogResponse(objectMapper.writeValueAsString(responseBillingStatusDto));
            supplierLogDto.setSupplier("nicepay");
            supplierLogDto.setUrl("https://billing.holigo.id/nicepay/status");
            supplierLogDto.setMessage(responseBillingStatusDto.getError_message());
            supplierLogDto.setCode(supplierCode);
            supplierLogDto.setUserId(userId);
            logService.sendSupplierLog(supplierLogDto);
        } catch (JsonProcessingException e) {
            log.error("Error : {}", e.getMessage());
        }
        return responseBillingStatusDto;
    }


    private Integer debitPoint(BigDecimal pointAmount, Payment payment, TransactionDto transaction) {
        PointDto pointDto = PointDto.builder().debitAmount(pointAmount.intValue())
                .transactionId(payment.getTransactionId()).paymentId(payment.getId())
                .informationIndex("pointStatement.payment")
                .transactionType(transaction.getTransactionType())
                .invoiceNumber(transaction.getInvoiceNumber())
                .userId(transaction.getUserId())
                .build();
        PointDto resultPointDto;
        try {
            resultPointDto = pointService.debit(pointDto);
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
            resultDepositDto = depositService.debit(depositDto);
        } catch (JMSException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if (!resultDepositDto.getIsValid()) {
            return BigDecimal.ZERO;
        }

        return resultDepositDto.getDebitAmount();
    }

}