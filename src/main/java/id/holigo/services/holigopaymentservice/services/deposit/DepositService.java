package id.holigo.services.holigopaymentservice.services.deposit;

import com.fasterxml.jackson.core.JsonProcessingException;
import id.holigo.services.common.model.DepositDto;

import javax.jms.JMSException;

public interface DepositService {

    DepositDto credit(DepositDto depositDto) throws JMSException, JsonProcessingException;

    DepositDto debit(DepositDto depositDto) throws JMSException, JsonProcessingException;
}
