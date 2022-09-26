package id.holigo.services.holigopaymentservice.services.pin;

import id.holigo.services.holigopaymentservice.web.model.PinValidationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "holigo-pin-service")
public interface PinServiceFeignClient {

    String VALIDATE_PIN_URL = "/api/v1/pin/validate";

    @RequestMapping(method = RequestMethod.POST, value = VALIDATE_PIN_URL)
    ResponseEntity<PinValidationDto> validate(@RequestBody PinValidationDto pinValidationDto,
                                              @RequestHeader("user-id") Long userId);
}
