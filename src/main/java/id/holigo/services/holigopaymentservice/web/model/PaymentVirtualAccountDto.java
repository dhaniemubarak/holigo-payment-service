package id.holigo.services.holigopaymentservice.web.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import id.holigo.services.common.model.PaymentServiceDto;
import id.holigo.services.common.model.PaymentStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentVirtualAccountDto {

    private UUID id;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;
    private BigDecimal totalAmount;
    private BigDecimal serviceFeeAmount;
    private BigDecimal billAmount;
    private String name;
    private String description;
    private String accountNumber;
    private PaymentStatusEnum status;
    private String reference;
    private PaymentServiceDto paymentService;
    @Builder.Default
    private List<PaymentInstructionDto> paymentInstructions = new ArrayList<>();
}
