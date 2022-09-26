package id.holigo.services.holigopaymentservice.services.point;

import com.fasterxml.jackson.core.JsonProcessingException;
import id.holigo.services.common.model.PointDto;

import javax.jms.JMSException;

public interface PointService {

    PointDto credit(PointDto pointDto) throws JMSException, JsonProcessingException;

    PointDto debit(PointDto pointDto) throws JMSException, JsonProcessingException;

}
