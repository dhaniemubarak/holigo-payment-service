package id.holigo.services.holigopaymentservice.web.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestBillingStatusDto implements Serializable {
    private Boolean dev;
    private String transactionId;
}
