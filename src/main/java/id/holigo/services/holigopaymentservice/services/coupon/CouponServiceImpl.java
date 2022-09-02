package id.holigo.services.holigopaymentservice.services.coupon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.holigo.services.common.model.ApplyCouponDto;
import id.holigo.services.holigopaymentservice.config.JmsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.UUID;

@Service
public class CouponServiceImpl implements CouponService {

    private CouponServiceFeignClient couponServiceFeignClient;

    private JmsTemplate jmsTemplate;

    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setCouponServiceFeignClient(CouponServiceFeignClient couponServiceFeignClient) {
        this.couponServiceFeignClient = couponServiceFeignClient;
    }

    @Autowired
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Override
    public ApplyCouponDto applyCoupon(UUID transactionId, String couponCode, String paymentServiceId, Long userId) {
        return couponServiceFeignClient.getApplyCoupon(transactionId, couponCode, paymentServiceId, userId).getBody();
    }

    @Override
    public ApplyCouponDto createApplyCoupon(ApplyCouponDto applyCouponDto) throws JMSException, JsonProcessingException {
        Message received = jmsTemplate.sendAndReceive(JmsConfig.CREATE_APPLY_COUPON, session -> {
            Message message = null;
            try {
                message = session.createTextMessage(objectMapper.writeValueAsString(applyCouponDto));
            } catch (JsonProcessingException e) {
                throw new JMSException(e.getMessage());
            }
            message.setStringProperty("_type", "id.holigo.services.common.model.ApplyCouponDto");
            return message;
        });
        assert received != null;
        return objectMapper.readValue(received.getBody(String.class), ApplyCouponDto.class);
    }
}
