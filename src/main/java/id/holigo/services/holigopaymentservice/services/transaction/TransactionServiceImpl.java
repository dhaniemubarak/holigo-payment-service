package id.holigo.services.holigopaymentservice.services.transaction;

import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import id.holigo.services.common.events.TransactionEvent;
import id.holigo.services.common.model.TransactionDto;
import id.holigo.services.holigopaymentservice.config.JmsConfig;
import id.holigo.services.holigopaymentservice.domain.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private final JmsTemplate jmsTemplate;

    @Autowired
    private final ObjectMapper objectMapper;

    @Override
    public TransactionDto getTransaction(UUID id) throws JsonMappingException, JsonProcessingException, JMSException {
        log.info("getTransaction is running....");
        TransactionDto transactionDto = TransactionDto.builder().id(id).build();
        Message received = jmsTemplate.sendAndReceive(JmsConfig.GET_TRANSACTION_BY_ID, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                log.info("getTransaction creating message is running....");
                Message message = null;
                try {
                    message = session.createTextMessage(objectMapper.writeValueAsString(transactionDto));
                } catch (JsonProcessingException e) {
                    log.error("Error : " + e.getMessage());
                    throw new JMSException(e.getMessage());
                }
                message.setStringProperty("_type", "id.holigo.services.common.model.TransactionDto");
                return message;
            }
        });
        log.info("Finish send ....");
        TransactionDto result = objectMapper.readValue(received.getBody(String.class), TransactionDto.class);
        log.info("Result -> {}", result);
        return result;
    }

    @Override
    public void issuedTransaction(UUID id, Payment payment) {
        log.info("issuedTransaction is running with transaction id : " + id.toString());
        TransactionDto transactionDto = TransactionDto.builder().id(id).paymentStatus(payment.getStatus())
                .paymentId(payment.getId()).build();
        jmsTemplate.convertAndSend(JmsConfig.ISSUED_TRANSACTION_BY_ID, new TransactionEvent(transactionDto));
    }

    @Override
    public void setPaymentInTransaction(UUID id, Payment payment) {
        log.info("setPaymentInTransaction is running with transaction is -> {}", id);
        TransactionDto transactionDto;
        if (payment.getPaymentService() != null) {
            transactionDto = TransactionDto.builder().id(id).paymentStatus(payment.getStatus())
                    .paymentId(payment.getId()).pointAmount(payment.getPointAmount())
                    .paymentServiceId(payment.getPaymentService().getId())
                    .voucherCode(payment.getVoucherCode()).build();
        } else {
            transactionDto = TransactionDto.builder().id(id).paymentStatus(payment.getStatus())
                    .pointAmount(payment.getPointAmount())
                    .voucherCode(payment.getVoucherCode()).build();
        }

        jmsTemplate.convertAndSend(JmsConfig.SET_PAYMENT_IN_TRANSACTION_BY_ID, new TransactionEvent(transactionDto));
    }

}
