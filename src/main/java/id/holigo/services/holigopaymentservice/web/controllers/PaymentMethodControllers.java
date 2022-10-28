package id.holigo.services.holigopaymentservice.web.controllers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import id.holigo.services.common.model.TransactionDto;
import id.holigo.services.holigopaymentservice.domain.PaymentMethod;
import id.holigo.services.holigopaymentservice.services.transaction.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import id.holigo.services.holigopaymentservice.services.PaymentMethodService;
import id.holigo.services.holigopaymentservice.web.mappers.PaymentMethodMapper;
import id.holigo.services.holigopaymentservice.web.model.PaymentMethodDto;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.jms.JMSException;

@AllArgsConstructor
@NoArgsConstructor
@RestController
public class PaymentMethodControllers {

    private PaymentMethodService paymentMethodService;

    private PaymentMethodMapper paymentMethodMapper;

    private TransactionService transactionService;

    @Autowired
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Autowired
    public void setPaymentMethodMapper(PaymentMethodMapper paymentMethodMapper) {
        this.paymentMethodMapper = paymentMethodMapper;
    }

    @Autowired
    public void setPaymentMethodService(PaymentMethodService paymentMethodService) {
        this.paymentMethodService = paymentMethodService;
    }

    @GetMapping("/api/v1/paymentMethods")
    public ResponseEntity<List<PaymentMethodDto>> index(@RequestParam(value = "transactionId", required = false) UUID transactionId) throws JMSException, JsonProcessingException {
        TransactionDto transactionDto = null;
        if (transactionId != null) {
            transactionDto = transactionService.getTransaction(transactionId);
        }
        TransactionDto finalTransactionDto = transactionDto;
        return new ResponseEntity<>(
                paymentMethodService.getShowPaymentMethod(transactionId).stream()
                        .map(paymentMethod -> paymentMethodMapper.paymentMethodToPaymentMethodDto(paymentMethod, finalTransactionDto)).collect(Collectors.toList()),
                HttpStatus.OK);
    }
}
