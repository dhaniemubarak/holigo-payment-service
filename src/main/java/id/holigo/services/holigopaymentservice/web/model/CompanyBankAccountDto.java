package id.holigo.services.holigopaymentservice.web.model;

import java.util.ArrayList;
import java.util.List;

import id.holigo.services.holigopaymentservice.domain.PaymentServiceStatusEnum;
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
