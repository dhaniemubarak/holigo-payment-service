package id.holigo.services.holigopaymentservice.services.logs;

import id.holigo.services.holigopaymentservice.web.model.SupplierLogDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LogServiceImpl implements LogService {

    private LogServiceFeignClient logServiceFeignClient;

    @Autowired
    public void setLogServiceFeignClient(LogServiceFeignClient logServiceFeignClient) {
        this.logServiceFeignClient = logServiceFeignClient;
    }

    @Override
    public void sendSupplierLog(SupplierLogDto supplierLogDto) {
        try {
            logServiceFeignClient.sendLog(supplierLogDto);
        } catch (Exception e) {
            log.error("Error send supplier logs -> {}", e.getMessage());
        }
    }
}
