package id.holigo.services.common.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDto {
    
    private UUID id;

    private UUID parentId;

    private String shortId;

    private Long userId;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private Timestamp deletedAt;

    private Timestamp expiredAt;

    private BigDecimal discountAmount;

    private BigDecimal fareAmount;

    private BigDecimal ntaAmount;

    private BigDecimal nraAmount;

    private Integer serviceId;

    private Integer productId;

    private String indexUser;

    private String indexCommission;

    private String indexProduct;

    private String transactionId;

    private String transactionType;

    private UUID paymentId;

    private PaymentStatusEnum statusPayment;

    private StatusOrderEnum statusOrder;
}
