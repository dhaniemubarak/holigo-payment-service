package id.holigo.services.common.model;

import java.math.BigDecimal;
import java.sql.Time;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentServiceDto {
    private String id;

    private String name;

    private String imageUrl;

    private Time openTime;

    private Time closeTime;

    private BigDecimal minimumAmount;

    private BigDecimal maximumAmount;

    private BigDecimal serviceFee;

    private BigDecimal mdrPercent;

    private BigDecimal fdsAmount;

    PaymentServiceStatusEnum status;

    private String note;
}
