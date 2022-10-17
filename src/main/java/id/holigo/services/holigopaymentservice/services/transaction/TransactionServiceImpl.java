package id.holigo.services.holigopaymentservice.services.transaction;

import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jms.core.JmsTemplate;
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

    private final JmsTemplate jmsTemplate;

    private final ObjectMapper objectMapper;

    @Override
    public TransactionDto getTransaction(UUID id) throws JsonProcessingException, JMSException {
        TransactionDto transactionDto = TransactionDto.builder().id(id).build();
        Message received = jmsTemplate.sendAndReceive(JmsConfig.GET_TRANSACTION_BY_ID, session -> {
            Message message;
            try {
                message = session.createTextMessage(objectMapper.writeValueAsString(transactionDto));
            } catch (JsonProcessingException e) {
                throw new JMSException(e.getMessage());
            }
            message.setStringProperty("_type", "id.holigo.services.common.model.TransactionDto");
            return message;
        });
        assert received != null;
        return objectMapper.readValue(received.getBody(String.class), TransactionDto.class);
    }

    @Override
    public void issuedTransaction(UUID id, Payment payment) {
        TransactionDto transactionDto = TransactionDto.builder().id(id).paymentStatus(payment.getStatus())
                .paymentId(payment.getId()).build();
        jmsTemplate.convertAndSend(JmsConfig.ISSUED_TRANSACTION_BY_ID, new TransactionEvent(transactionDto));
    }

    @Override
    public void setPaymentInTransaction(UUID id, Payment payment) {
        TransactionDto transactionDto;
        if (payment.getPaymentService() != null) {
            transactionDto = TransactionDto.builder().id(id).paymentStatus(payment.getStatus())
                    .paymentId(payment.getId()).pointAmount(payment.getPointAmount())
                    .paymentServiceId(payment.getPaymentService().getId())
                    .discountAmount(payment.getDiscountAmount())
                    .voucherCode(payment.getCouponCode()).build();
        } else {
            transactionDto = TransactionDto.builder().id(id).paymentStatus(payment.getStatus())
                    .pointAmount(payment.getPointAmount())
                    .discountAmount(payment.getDiscountAmount())
                    .voucherCode(payment.getCouponCode()).build();
        }

        jmsTemplate.convertAndSend(JmsConfig.SET_PAYMENT_IN_TRANSACTION_BY_ID, new TransactionEvent(transactionDto));
    }

}
