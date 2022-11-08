package id.holigo.services.holigopaymentservice.web.model;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DigitalWalletCallbackDto implements Serializable {

    private Long id;

    private String paymentMethod;

    private String paymentMerchant;

    private String accountNumber;

    private BigDecimal amount;

    private String reference;

    private Timestamp transactionTime;

}
