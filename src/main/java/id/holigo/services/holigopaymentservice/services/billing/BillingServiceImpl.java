package id.holigo.services.holigopaymentservice.services.billing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.holigo.services.holigopaymentservice.web.model.RequestBillingDto;
import id.holigo.services.holigopaymentservice.web.model.ResponseBillingDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BillingServiceImpl implements BillingService {

    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private BillingServiceFeignClient billingServiceFeignClient;

    @Autowired
    public void setBillingServiceFeignClient(BillingServiceFeignClient billingServiceFeignClient) {
        this.billingServiceFeignClient = billingServiceFeignClient;
    }

    @Override
    public ResponseBillingDto postPayment(RequestBillingDto requestBillingDto) {
        try {
            log.info("request dto -> {}", objectMapper.writeValueAsString(requestBillingDto));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        requestBillingDto.setAccountNumber("081227619520");
        requestBillingDto.setDev(true);
        ResponseEntity<ResponseBillingDto> response = billingServiceFeignClient.checkout(requestBillingDto);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return response.getBody();
        }
        return null;
    }
}
