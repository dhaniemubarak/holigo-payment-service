package id.holigo.services.holigopaymentservice.services.coupon;

import id.holigo.services.common.model.ApplyCouponDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CouponServiceImpl implements CouponService {

    private CouponServiceFeignClient couponServiceFeignClient;

    @Autowired
    public void setCouponServiceFeignClient(CouponServiceFeignClient couponServiceFeignClient) {
        this.couponServiceFeignClient = couponServiceFeignClient;
    }

    @Override
    public ApplyCouponDto applyCoupon(UUID transactionId, String couponCode, String paymentServiceId, Long userId) {
        return null;
    }

    @Override
    public ApplyCouponDto createApplyCoupon(ApplyCouponDto applyCouponDto) {
        return null;
    }
}
