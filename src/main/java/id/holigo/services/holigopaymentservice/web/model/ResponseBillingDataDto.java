package id.holigo.services.holigopaymentservice.web.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseBillingDataDto implements Serializable {

    private String transactionReference;
    private String transactionId;
    private String paymentCode;
    private String paymentUrl;
    private BigDecimal amount;

}
