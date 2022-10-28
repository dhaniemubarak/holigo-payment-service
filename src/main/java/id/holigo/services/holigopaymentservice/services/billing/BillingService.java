package id.holigo.services.holigopaymentservice.services.billing;

import id.holigo.services.holigopaymentservice.web.model.RequestBillingDto;
import id.holigo.services.holigopaymentservice.web.model.ResponseBillingDto;

public interface BillingService {
    ResponseBillingDto postPayment(RequestBillingDto requestBillingDto);
}
