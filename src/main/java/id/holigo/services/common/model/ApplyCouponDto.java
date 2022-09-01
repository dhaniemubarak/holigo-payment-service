package id.holigo.services.common.model;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyCouponDto implements Serializable {

    private UUID id;

    private Boolean isValid;

    private Boolean isFreeAdmin;

    private Boolean isFreeServiceFee;

    private BigDecimal discountAmount;

    private String couponCode;

    private Long userId;

    private UUID transactionId;

    private String paymentServiceId;

    private String message;
}
