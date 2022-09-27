package id.holigo.services.holigopaymentservice.services.accountBalance;

import id.holigo.services.common.model.AccountBalanceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "holigo-account-balance-service")
public interface AccountBalanceServiceFeignClient {
    String ACCOUNT_BALANCE_URL = "/api/v1/accountBalance";

    @RequestMapping(method = RequestMethod.GET, value = ACCOUNT_BALANCE_URL)
    ResponseEntity<AccountBalanceDto> getAccountBalance(@RequestHeader("user-id") Long userId);
}
