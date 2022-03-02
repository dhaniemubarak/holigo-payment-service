package id.holigo.services.holigopaymentservice.web.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import id.holigo.services.common.model.PaymentStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentBankTransferDto implements Serializable {

    private UUID id;

    private String paymentServiceId;

    private BigDecimal totalAmount;

    private BigDecimal vatAmount;

    private BigDecimal fdsAmount;

    private Integer uniqueCode;

    private BigDecimal serviceFeeAmount;

    private BigDecimal billAmount;

    private PaymentStatusEnum status;

    private String reference;

    private CompanyBankAccountDto bank;

    @Builder.Default
    private List<PaymentInstructionDto> paymentInstructions = new ArrayList<>();
}
