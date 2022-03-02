package id.holigo.services.holigopaymentservice.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.BankTransferCallback;
import id.holigo.services.holigopaymentservice.domain.PaymentCallbackStatusEnum;
import id.holigo.services.holigopaymentservice.domain.PaymentBankTransfer;
import id.holigo.services.holigopaymentservice.repositories.BankTransferCallbackRepository;
import id.holigo.services.holigopaymentservice.repositories.PaymentBankTransferRepository;

@SpringBootTest
public class BankTransferCallbackServiceImplTest {

        @Autowired
        PaymentBankTransferRepository paymentBankTransferRepository;

        @Autowired
        BankTransferCallbackService bankTransferCallbackService;

        @Autowired
        BankTransferCallbackRepository bankTransferCallbackRepository;

        BankTransferCallback bankTransferCallback;

        PaymentBankTransfer paymentBankTransfer;

        @BeforeEach
        public void setUp() {
                paymentBankTransfer = PaymentBankTransfer.builder()
                                .id(UUID.fromString("ef9abf24-a4bd-45a6-9191-293d6ac61c0b"))
                                .paymentServiceId("BT_BCA").totalAmount(new BigDecimal(98500.00))
                                .vatAmount(new BigDecimal(0))
                                .fdsAmount(new BigDecimal(0)).uniqueCode(35).serviceFeeAmount(new BigDecimal(35))
                                .billAmount(new BigDecimal(98535.00)).status(PaymentStatusEnum.WAITING_PAYMENT)
                                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                                .updatedAt(Timestamp.valueOf(LocalDateTime.now())).build();
                bankTransferCallback = BankTransferCallback.builder().paymentMethod("TRF").paymentMerchant("BT_BCA")
                                .amount(new BigDecimal("98535.00")).createdAt(Timestamp.valueOf(LocalDateTime.now()))
                                .updatedAt(Timestamp.valueOf(LocalDateTime.now())).build();
        }

        @Transactional
        @Test
        public void testFindTransaction() {
                BankTransferCallback savedBankTransferCallback = bankTransferCallbackService
                                .newBankTransfer(bankTransferCallback);
                bankTransferCallbackService.findTransaction(savedBankTransferCallback.getId());

                BankTransferCallback findTransactionBankTransferCallback = bankTransferCallbackRepository
                                .getById(savedBankTransferCallback.getId());
                assertEquals(PaymentCallbackStatusEnum.PROCESS_ISSUED,
                                findTransactionBankTransferCallback.getProcessStatus());
        }
}
