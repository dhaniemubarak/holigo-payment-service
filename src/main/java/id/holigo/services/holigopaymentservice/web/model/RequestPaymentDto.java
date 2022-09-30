package id.holigo.services.holigopaymentservice.web.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestPaymentDto implements Serializable {

    static final long serialVersionUID = -155181210L;

    private String paymentServiceId;

    private UUID transactionId;

    private Boolean isSplitBill;

    @Builder.Default
    private BigDecimal pointAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal depositAmount = BigDecimal.ZERO;

    private String pin;

    private String couponCode;
}
