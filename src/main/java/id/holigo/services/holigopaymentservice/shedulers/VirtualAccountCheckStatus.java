package id.holigo.services.holigopaymentservice.shedulers;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.PaymentVirtualAccount;
import id.holigo.services.holigopaymentservice.repositories.PaymentVirtualAccountRepository;
import id.holigo.services.holigopaymentservice.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class VirtualAccountCheckStatus {

    private PaymentVirtualAccountRepository paymentVirtualAccountRepository;

    private PaymentService paymentService;

    @Autowired
    public void setPaymentVirtualAccountRepository(PaymentVirtualAccountRepository paymentVirtualAccountRepository) {
        this.paymentVirtualAccountRepository = paymentVirtualAccountRepository;
    }

    @Autowired
    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Scheduled(fixedRate = 10000)
    public void vaCheckStatus() {
        List<String> paymentServiceList = new ArrayList<>();
        paymentServiceList.add("VA_BCA");
        paymentServiceList.add("VA_MANDIRI");
        List<PaymentVirtualAccount> paymentVirtualAccountList = paymentVirtualAccountRepository
                .findAllByStatusAndPaymentServiceIdNotIn(PaymentStatusEnum.WAITING_PAYMENT, paymentServiceList);
        paymentVirtualAccountList.forEach(paymentVirtualAccount -> paymentService.checkStatus(paymentVirtualAccount));

    }
}
