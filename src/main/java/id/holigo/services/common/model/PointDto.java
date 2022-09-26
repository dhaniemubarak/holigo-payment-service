package id.holigo.services.common.model;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointDto implements Serializable {

    private Long userId;

    @Builder.Default
    private Boolean isValid = false;

    @Builder.Default
    private Integer point = 0;

    @Builder.Default
    private Integer creditAmount = 0;

    @Builder.Default
    private Integer debitAmount = 0;

    private UUID transactionId;

    private UUID paymentId;

    private String informationIndex;

    private String informationValue;

    private String transactionType;

    private String invoiceNumber;

    private String message;
}
