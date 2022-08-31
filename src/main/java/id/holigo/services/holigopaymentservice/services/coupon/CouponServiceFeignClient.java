package id.holigo.services.holigopaymentservice.services.coupon;

import id.holigo.services.common.model.ApplyCouponDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient("holigo-coupon-service")
public interface CouponServiceFeignClient {

    String APPLY_COUPON_URL = "/api/v1/applyCoupon";
    @RequestMapping(method = RequestMethod.GET, value = APPLY_COUPON_URL)
    ResponseEntity<ApplyCouponDto> getApplyCoupon(@RequestParam("transactionId") UUID transactionId,
                                                  @RequestParam("couponCode") String couponCode,
                                                  @RequestParam("paymentServiceId") String paymentServiceId,
                                                  @RequestHeader("user-id") Long userId);
}
