package id.holigo.services.holigopaymentservice.web.model;

import id.holigo.services.common.model.PaymentServiceDto;
import id.holigo.services.common.model.PaymentServiceStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyBankAccountDto {
    private Integer id;

    private String bankName;

    private String accountNumber;

    private String accountName;

    private PaymentServiceStatusEnum status;

    private PaymentServiceDto paymentService;
    
}
