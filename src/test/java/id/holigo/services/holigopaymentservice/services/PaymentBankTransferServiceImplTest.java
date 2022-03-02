package id.holigo.services.holigopaymentservice.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.domain.PaymentBankTransfer;
import id.holigo.services.holigopaymentservice.domain.PaymentService;
import id.holigo.services.holigopaymentservice.repositories.BankTransferCallbackRepository;
import id.holigo.services.holigopaymentservice.repositories.PaymentBankTransferRepository;
import id.holigo.services.holigopaymentservice.repositories.PaymentRepository;

@SpringBootTest
public class PaymentBankTransferServiceImplTest {

    @Autowired
    PaymentBankTransferRepository paymentBankTransferRepository;

    @Autowired
    BankTransferCallbackRepository bankTransferCallbackRepository;

    @Autowired
    PaymentBankTransferService paymentBankTransferService;

    @Autowired
    PaymentRepository paymentRepository;

    PaymentBankTransfer paymentBankTransfer;

    Payment payment;

    PaymentService paymentService;

    @BeforeEach
    public void setUp() {

        paymentService = PaymentService.builder().id("BT_BCA").build();

        payment = Payment.builder().id(UUID.fromString("51f80e20-b573-447b-a365-040f53fb6e5b"))
                .transactionId(UUID.fromString("1ef35204-a505-455a-894a-b1b5cdf43b44")).userId(5L)
                .paymentService(paymentService)
                .fareAmount(new BigDecimal(98500.00)).serviceFeeAmount(new BigDecimal(35.00))
                .discountAmount(new BigDecimal(0.00))
                .totalAmount(new BigDecimal(98535.00)).paymentServiceAmount(new BigDecimal(98535.00)).isSplitBill(false)
                .pointAmount(new BigDecimal(0)).remainingAmount(new BigDecimal(98535.00))
                .status(PaymentStatusEnum.WAITING_PAYMENT).detailType("bankTransfer")
                .detailId("ef9abf24-a4bd-45a6-9191-293d6ac61c0b").createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now())).build();
        paymentBankTransfer = PaymentBankTransfer.builder().id(UUID.fromString("ef9abf24-a4bd-45a6-9191-293d6ac61c0b"))
                .paymentServiceId("VA_BCA").totalAmount(new BigDecimal(98500.00)).vatAmount(new BigDecimal(0))
                .fdsAmount(new BigDecimal(0)).uniqueCode(35).serviceFeeAmount(new BigDecimal(35))
                .billAmount(new BigDecimal(98535)).status(PaymentStatusEnum.WAITING_PAYMENT)
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now())).build();
    }

    @Transactional
    @Test
    void testProcessIssuedTransaction() {
        paymentRepository.save(payment);
        PaymentBankTransfer paymentBanktransfer = paymentBankTransferRepository.save(paymentBankTransfer);
        paymentBankTransferService.paymentHasBeenPaid(paymentBanktransfer.getId());
        System.out.println("Should be PAID");
        System.out.println(paymentBanktransfer.getStatus());

        assertEquals(PaymentStatusEnum.PAID, paymentBanktransfer.getStatus());

        Payment getPayment = paymentRepository.getById(payment.getId());
        assertEquals(PaymentStatusEnum.PAID, getPayment.getStatus());

    }
}
