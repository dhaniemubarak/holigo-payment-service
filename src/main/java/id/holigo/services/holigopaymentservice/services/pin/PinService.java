package id.holigo.services.holigopaymentservice.services.pin;

import id.holigo.services.holigopaymentservice.web.model.PinValidationDto;
import org.springframework.http.ResponseEntity;

public interface PinService {
    Boolean validate(PinValidationDto pinValidationDto, Long userId);
}
