package id.holigo.services.holigopaymentservice.services.accountBalance;

import id.holigo.services.common.model.AccountBalanceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AccountBalanceServiceImpl implements AccountBalanceService {

    private AccountBalanceServiceFeignClient accountBalanceServiceFeignClient;

    @Autowired
    public void setAccountBalanceServiceFeignClient(AccountBalanceServiceFeignClient accountBalanceServiceFeignClient) {
        this.accountBalanceServiceFeignClient = accountBalanceServiceFeignClient;
    }

    @Override
    public AccountBalanceDto getAccountBalance(Long userId) {
        ResponseEntity<AccountBalanceDto> responseEntity = accountBalanceServiceFeignClient.getAccountBalance(userId);
        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            return responseEntity.getBody();
        }
        return null;
    }
}
