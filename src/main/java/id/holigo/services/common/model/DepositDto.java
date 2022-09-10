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
public class DepositDto implements Serializable {

    private Long userId;

    @Builder.Default
    private Boolean isValid = false;

    @Builder.Default
    private BigDecimal deposit = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal creditAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal debitAmount = BigDecimal.ZERO;

    private UUID transactionId;

    private UUID paymentId;

    private String informationIndex;

    private String informationValue;

    private String transactionType;

    private String invoiceNumber;

    private String message;
}
