package id.holigo.services.holigopaymentservice.services;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.jms.JMSException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import id.holigo.services.common.model.StatusPaymentEnum;
import id.holigo.services.common.model.TransactionDto;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.domain.PaymentBankTransfer;
import id.holigo.services.holigopaymentservice.repositories.PaymentBankTransferRepository;
import id.holigo.services.holigopaymentservice.repositories.PaymentRepository;
import id.holigo.services.holigopaymentservice.services.transaction.TransactionService;
import id.holigo.services.holigopaymentservice.web.exceptions.ForbiddenException;
import id.holigo.services.holigopaymentservice.web.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    private final Integer min = 1;

    private final Integer max = 999;

    @Autowired
    private final TransactionService transactionService;

    @Autowired
    private final MessageSource messageSource;

    @Autowired
    private final StatusPaymentService statusPaymentService;

    @Autowired
    private final PaymentRepository paymentRepository;

    @Autowired
    private final PaymentBankTransferRepository paymentBankTransferRepository;

    @Override
    public Payment createPayment(Payment payment) throws JsonMappingException, JsonProcessingException, JMSException {

        TransactionDto transactionDto = transactionService.getTransaction(payment.getTransactionId());

        if (transactionDto.getUserId() == null) {
            throw new NotFoundException(messageSource.getMessage("holigo-transaction-service.not_found", null,
                    LocaleContextHolder.getLocale()));
        }

        if (transactionDto.getUserId() != payment.getUserId()) {
            throw new ForbiddenException(messageSource.getMessage("payment.user_transaction_not_match", null,
                    LocaleContextHolder.getLocale()));
        }

        if (transactionDto.getStatusPayment() != StatusPaymentEnum.WAITING_PAYMENT) {
            String message = statusPaymentService.getStatusMessage(transactionDto.getStatusPayment());
            throw new ForbiddenException(messageSource.getMessage(message, null,
                    LocaleContextHolder.getLocale()));
        }

        BigDecimal pointAmount = BigDecimal.valueOf(0);
        if (payment.getIsSplitBill()) {
            // PIN validation

            // Point credit
        }

        // Check for voucher
        BigDecimal discountAmount = BigDecimal.valueOf(0.00);

        // Switch selected payment
        BigDecimal paymentServiceAmount = BigDecimal.valueOf(0.00);
        BigDecimal serviceFeeAmount = BigDecimal.valueOf(0.00);
        BigDecimal totalAmount = transactionDto.getFareAmount().subtract(discountAmount);
        String detailType = null;
        String detailId = UUID.randomUUID().toString();
        switch (payment.getPaymentServiceId()) {
            case "BT_BCA":
            case "BT_MANDIRI":
            case "BT_BNI":
            case "BT_BSI":
                Integer uniqueCode = randomNumber();
                serviceFeeAmount = serviceFeeAmount.add(BigDecimal.valueOf(uniqueCode));
                paymentServiceAmount = totalAmount
                        .add(paymentServiceAmount.add(serviceFeeAmount).subtract(pointAmount));
                PaymentBankTransfer paymentBankTransfer = new PaymentBankTransfer();
                paymentBankTransfer.setTotalAmount(totalAmount);
                paymentBankTransfer.setUniqueCode(uniqueCode);
                paymentBankTransfer.setFdsAmount(BigDecimal.valueOf(0));
                paymentBankTransfer.setBillAmount(paymentServiceAmount);
                paymentBankTransfer.setVatAmount(BigDecimal.valueOf(0));
                paymentBankTransfer.setServiceFeeAmount(serviceFeeAmount);
                PaymentBankTransfer savedPaymentBankTransfer = paymentBankTransferRepository.save(paymentBankTransfer);
                detailType = "bankTransfer";
                detailId = savedPaymentBankTransfer.getId().toString();
                break;
            case "VA_BCA":
            case "VA_MANDIRI":
            case "VA_BNI":
                detailType = "virtualAccount";
                break;
            case "CC_ALL":
                detailType = "creditCard";
                break;
            default:
                detailType = "undefined";
                break;
        }
        BigDecimal remainingAmount = paymentServiceAmount;

        // Set payment
        payment.setFareAmount(transactionDto.getFareAmount());
        payment.setDiscountAmount(discountAmount);
        payment.setServiceFeeAmount(serviceFeeAmount);
        payment.setPointAmount(pointAmount);
        payment.setTotalAmount(totalAmount);
        payment.setPaymentServiceAmount(paymentServiceAmount);
        payment.setRemainingAmount(remainingAmount);
        payment.setStatus(StatusPaymentEnum.WAITING_PAYMENT);
        payment.setDetailType(detailType);
        payment.setDetailId(detailId);

        // Create payment after get callback from supplier
        Payment savedPayment = paymentRepository.save(payment);

        return savedPayment;
    }

    private Integer randomNumber() {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

}
