package id.holigo.services.holigopaymentservice.services.point;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.holigo.services.common.model.PointDto;
import id.holigo.services.holigopaymentservice.config.JmsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;

@Service
public class PointServiceImpl implements PointService {

    private JmsTemplate jmsTemplate;

    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Override
    public PointDto credit(PointDto pointDto) throws JMSException, JsonProcessingException {
        Message received = jmsTemplate.sendAndReceive(JmsConfig.CREDIT_POINT, session -> {
            Message message;
            try {
                message = session.createTextMessage(objectMapper.writeValueAsString(pointDto));
                message.setStringProperty("_type", "id.holigo.services.common.model.PointDto");
            } catch (JsonProcessingException e) {
                throw new JMSException(e.getMessage());
            }
            return message;
        });
        assert received != null;
        return objectMapper.readValue(received.getBody(String.class),
                PointDto.class);
    }

    @Override
    public PointDto debit(PointDto pointDto) throws JMSException, JsonProcessingException {
        Message received = jmsTemplate.sendAndReceive(JmsConfig.DEBIT_POINT, session -> {
            Message message;
            try {
                message = session.createTextMessage(objectMapper.writeValueAsString(pointDto));
                message.setStringProperty("_type", "id.holigo.services.common.model.PointDto");
            } catch (JsonProcessingException e) {
                throw new JMSException(e.getMessage());
            }
            return message;
        });
        assert received != null;
        return objectMapper.readValue(received.getBody(String.class),
                PointDto.class);
    }
}
