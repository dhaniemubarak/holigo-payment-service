package id.holigo.services.holigopaymentservice.services.coupon;

import id.holigo.services.common.model.ApplyCouponDto;

import java.util.UUID;

public interface CouponService {
    ApplyCouponDto applyCoupon(UUID transactionId, String couponCode, String paymentServiceId, Long userId);
}
