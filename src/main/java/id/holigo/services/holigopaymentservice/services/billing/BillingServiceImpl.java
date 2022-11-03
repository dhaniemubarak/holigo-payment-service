package id.holigo.services.holigopaymentservice.services.billing;

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

    private BillingServiceFeignClient billingServiceFeignClient;

    @Autowired
    public void setBillingServiceFeignClient(BillingServiceFeignClient billingServiceFeignClient) {
        this.billingServiceFeignClient = billingServiceFeignClient;
    }

    @Override
    public ResponseBillingDto postPayment(RequestBillingDto requestBillingDto) {
        ResponseEntity<ResponseBillingDto> response = billingServiceFeignClient.checkout(requestBillingDto);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return response.getBody();
        }
        return null;
    }
}
