package id.holigo.services.holigopaymentservice.web.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestPaymentDto implements Serializable {

    static final long serialVersionUID = -155181210L;

    private String paymentServiceId;

    private UUID transactionId;

    private Boolean isSplitBill;

    private BigDecimal pointAmount;

    private BigDecimal depositAmount;

    private String pin;

    private String couponCode;
}
