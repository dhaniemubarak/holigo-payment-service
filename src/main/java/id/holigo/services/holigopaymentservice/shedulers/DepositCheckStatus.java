package id.holigo.services.holigopaymentservice.shedulers;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.repositories.PaymentRepository;
import id.holigo.services.holigopaymentservice.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DepositCheckStatus {

    private PaymentRepository paymentRepository;


    private PaymentService paymentService;

    @Autowired
    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Autowired
    public void setPaymentRepository(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Scheduled(fixedRate = 30000)
    void depositCheckStatus() {
        List<Payment> paymentList = paymentRepository.findAllByStatusAndPaymentServiceId(PaymentStatusEnum.WAITING_PAYMENT, "DEPOSIT");
        paymentList.forEach(payment -> paymentService.checkDepositStatus(payment));
    }
}
