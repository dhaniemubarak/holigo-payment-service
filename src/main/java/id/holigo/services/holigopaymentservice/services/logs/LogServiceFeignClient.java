package id.holigo.services.holigopaymentservice.services.logs;

import id.holigo.services.holigopaymentservice.web.model.SupplierLogDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "holigo-supplier-message-service")
public interface LogServiceFeignClient {
    String LOGS_URL = "/api/v1/supplier/message/logs";

    @RequestMapping(method = RequestMethod.GET, value = LOGS_URL)
    ResponseEntity<HttpStatus> sendLog(@RequestBody SupplierLogDto supplierLogDto);
}
