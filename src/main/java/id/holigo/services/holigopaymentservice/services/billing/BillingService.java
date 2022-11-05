package id.holigo.services.holigopaymentservice.services.billing;

import id.holigo.services.holigopaymentservice.web.model.RequestBillingDto;
import id.holigo.services.holigopaymentservice.web.model.RequestBillingStatusDto;
import id.holigo.services.holigopaymentservice.web.model.ResponseBillingDto;
import id.holigo.services.holigopaymentservice.web.model.ResponseBillingStatusDto;

public interface BillingService {
    ResponseBillingDto postPayment(RequestBillingDto requestBillingDto);

    ResponseBillingStatusDto postCheckStatus(RequestBillingStatusDto requestBillingStatusDto);
}
