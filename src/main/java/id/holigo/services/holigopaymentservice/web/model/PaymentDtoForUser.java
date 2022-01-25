package id.holigo.services.holigopaymentservice.web.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

import id.holigo.services.common.model.PaymentStatusEnum;
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

    private BigDecimal remainingAmount;

    private PaymentStatusEnum status;

    private String paymentServiceId;

    private String detailId;

    private String detailType;

}
