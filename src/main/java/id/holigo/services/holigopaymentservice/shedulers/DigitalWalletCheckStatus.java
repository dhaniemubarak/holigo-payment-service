package id.holigo.services.holigopaymentservice.shedulers;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.PaymentDigitalWallet;
import id.holigo.services.holigopaymentservice.repositories.PaymentDigitalWalletRepository;
import id.holigo.services.holigopaymentservice.services.PaymentDigitalWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DigitalWalletCheckStatus {

    private PaymentDigitalWalletRepository paymentDigitalWalletRepository;

    private PaymentDigitalWalletService paymentDigitalWalletService;

    @Autowired
    public void setPaymentDigitalWalletRepository(PaymentDigitalWalletRepository paymentDigitalWalletRepository) {
        this.paymentDigitalWalletRepository = paymentDigitalWalletRepository;
    }

    @Autowired
    public void setPaymentDigitalWalletService(PaymentDigitalWalletService paymentDigitalWalletService) {
        this.paymentDigitalWalletService = paymentDigitalWalletService;
    }

    @Scheduled(fixedRate = 30000)
    public void danaCheckStatus() {
        List<PaymentDigitalWallet> paymentDigitalWalletList = paymentDigitalWalletRepository.findAllByStatus(PaymentStatusEnum.WAITING_PAYMENT);
        paymentDigitalWalletList.forEach(paymentDigitalWallet -> paymentDigitalWalletService.checkStatus(paymentDigitalWallet));
    }
}
