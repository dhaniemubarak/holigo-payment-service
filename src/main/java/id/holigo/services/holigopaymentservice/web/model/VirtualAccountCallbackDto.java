package id.holigo.services.holigopaymentservice.web.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VirtualAccountCallbackDto implements Serializable {

    private Long id;

    private String paymentMethod;

    private String paymentMerchant;

    private String accountNumber;

    private BigDecimal amount;

    private String reference;

    private Timestamp transactionTime;

}
