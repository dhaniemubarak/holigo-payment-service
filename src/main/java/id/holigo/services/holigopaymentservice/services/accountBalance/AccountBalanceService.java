package id.holigo.services.holigopaymentservice.services.accountBalance;

import id.holigo.services.common.model.AccountBalanceDto;

public interface AccountBalanceService {

    AccountBalanceDto getAccountBalance(Long userId);
}
