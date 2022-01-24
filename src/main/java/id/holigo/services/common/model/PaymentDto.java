package id.holigo.services.common.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto implements Serializable {

    private static final long serialVersionUID = 60L;

    private UUID id;

    private UUID transactionId;

    private Long userId;

    private String paymentServiceId;

    private BigDecimal fareAmount;

    private BigDecimal serviceFeeAmount;

    private BigDecimal discountAmount;

    private BigDecimal totalAmount;

    private BigDecimal paymentServiceAmount;

    private Boolean isSplitBill;

    private BigDecimal pointAmount;

    private BigDecimal remainingAmount;

    @Enumerated(EnumType.STRING)
    private PaymentStatusEnum status;

    private String verifyType;

    private String verifyId;

    private String detailType;

    private String detailId;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private Timestamp deletedAt;

    private String voucherCode;

    private String pin;
}
