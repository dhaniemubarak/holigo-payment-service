package id.holigo.services.holigopaymentservice.services.transaction;

import java.util.UUID;

import javax.jms.JMSException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import id.holigo.services.common.model.TransactionDto;

public interface TransactionService {
    TransactionDto getTransaction(UUID id) throws JsonMappingException, JsonProcessingException, JMSException;

    void issuedTransaction(UUID id);

}
