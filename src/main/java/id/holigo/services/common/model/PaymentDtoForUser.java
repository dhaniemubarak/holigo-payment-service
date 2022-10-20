package id.holigo.services.common.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentDtoForUser implements Serializable {

    private UUID id;

    private BigDecimal fareAmount;

    private BigDecimal serviceFeeAmount;

    private BigDecimal discountAmount;

    private BigDecimal totalAmount;

    private BigDecimal paymentServiceAmount;

    private Boolean isSplitBill;

    private BigDecimal pointAmount;

    private BigDecimal depositAmount;

    private BigDecimal remainingAmount;

    private PaymentStatusEnum status;

    private PaymentServiceDto paymentService;

    private String detailId;

    private String detailType;

}
