package id.holigo.services.holigopaymentservice.services.pin;

import id.holigo.services.holigopaymentservice.web.exceptions.ForbiddenException;
import id.holigo.services.holigopaymentservice.web.exceptions.NotAcceptableException;
import id.holigo.services.holigopaymentservice.web.exceptions.NotFoundException;
import id.holigo.services.holigopaymentservice.web.model.PinValidationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class PinServiceImpl implements PinService {

    private PinServiceFeignClient pinServiceFeignClient;

    @Autowired
    public void setPinServiceFeignClient(PinServiceFeignClient pinServiceFeignClient) {
        this.pinServiceFeignClient = pinServiceFeignClient;
    }

    public Boolean validate(PinValidationDto pinValidationDto, Long userId) {
        ResponseEntity<PinValidationDto> result = pinServiceFeignClient.validate(pinValidationDto, userId);
        if (result.getStatusCode().equals(HttpStatus.NOT_ACCEPTABLE)) {
            throw new NotAcceptableException();
        }
        if (result.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
            throw new ForbiddenException();
        }
        if (result.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            throw new NotFoundException();
        }
        return true;
    }
}
