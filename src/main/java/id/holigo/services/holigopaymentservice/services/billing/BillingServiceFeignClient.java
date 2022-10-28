package id.holigo.services.holigopaymentservice.services.billing;

import id.holigo.services.holigopaymentservice.web.model.RequestBillingDto;
import id.holigo.services.holigopaymentservice.web.model.ResponseBillingDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "billing-nicepay", url = "https://billing.holigo.id")
public interface BillingServiceFeignClient {

    String CHECKOUT_PATH = "/nicepay/checkout";

    @RequestMapping(method = RequestMethod.POST, value = CHECKOUT_PATH)
    ResponseEntity<ResponseBillingDto> checkout(@RequestBody RequestBillingDto requestBillingDto);
}
