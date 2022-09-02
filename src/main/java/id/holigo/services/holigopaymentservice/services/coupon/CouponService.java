package id.holigo.services.holigopaymentservice.services.coupon;

import com.fasterxml.jackson.core.JsonProcessingException;
import id.holigo.services.common.model.ApplyCouponDto;

import javax.jms.JMSException;
import java.util.UUID;

public interface CouponService {
    ApplyCouponDto applyCoupon(UUID transactionId, String couponCode, String paymentServiceId, Long userId);
    ApplyCouponDto createApplyCoupon(ApplyCouponDto applyCouponDto) throws JMSException, JsonProcessingException;
}
