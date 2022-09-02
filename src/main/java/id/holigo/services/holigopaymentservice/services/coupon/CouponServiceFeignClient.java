package id.holigo.services.holigopaymentservice.services.coupon;

import id.holigo.services.common.model.ApplyCouponDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "holigo-coupon-service", url = "10.1.1.15:8114")
public interface CouponServiceFeignClient {

    String APPLY_COUPON_URL = "/api/v1/applyCoupon";

    @RequestMapping(method = RequestMethod.GET, value = APPLY_COUPON_URL)
    ResponseEntity<ApplyCouponDto> getApplyCoupon(@RequestParam("transactionId") UUID transactionId,
                                                  @RequestParam("couponCode") String couponCode,
                                                  @RequestParam("paymentServiceId") String paymentServiceId,
                                                  @RequestHeader("user-id") Long userId);

    @RequestMapping(method = RequestMethod.POST, value = APPLY_COUPON_URL, consumes = "application/json")
    ResponseEntity<ApplyCouponDto> createApplyCoupon(ApplyCouponDto applyCouponDto);
}
