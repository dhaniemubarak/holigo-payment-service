package id.holigo.services.holigopaymentservice.web.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseBillingStatusDataDto implements Serializable {
    private String cardNum;
    private String transactionId;
    private String transactionStatus;
    private BigDecimal amount;
}
