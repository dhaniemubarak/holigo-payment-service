package id.holigo.services.holigopaymentservice.services;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import id.holigo.services.common.model.TransactionDto;
import id.holigo.services.holigopaymentservice.services.transaction.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import id.holigo.services.holigopaymentservice.domain.PaymentMethod;
import id.holigo.services.holigopaymentservice.repositories.PaymentMethodRepository;

import javax.jms.JMSException;

@Service
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private PaymentMethodRepository paymentMethodRepository;

    private TransactionService transactionService;

    @Autowired
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Autowired
    public void setPaymentMethodRepository(PaymentMethodRepository paymentMethodRepository) {
        this.paymentMethodRepository = paymentMethodRepository;
    }

    @Override
    public List<PaymentMethod> getShowPaymentMethod(UUID transactionId) throws JMSException, JsonProcessingException {
        List<PaymentMethod> paymentMethods = paymentMethodRepository.findAllByIsShow(true);
        if (transactionId != null) {
            TransactionDto transactionDto = transactionService.getTransaction(transactionId);
            if (transactionDto.getTransactionType().equals("HTD")) {
                paymentMethods.remove(0);
            }
        }
        return paymentMethods;
    }

}
