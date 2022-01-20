package id.holigo.services.holigopaymentservice.web.model;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankTransferCallbackDto {

    private Long id;

    private String paymentMethod;

    private String paymentMerchant;

    private BigDecimal amount;

    private String reference;

}