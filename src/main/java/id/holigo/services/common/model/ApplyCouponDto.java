package id.holigo.services.common.model;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyCouponDto implements Serializable {

    private Boolean isValid;

    private Boolean isFreeAdmin;

    private Boolean isFreeServiceFee;

    private BigDecimal discountAmount;

    private String message;
}
