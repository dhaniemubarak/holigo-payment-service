package id.holigo.services.common.model;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountBalanceDto implements Serializable {

    private Long userId;

    private BigDecimal deposit;

    private Integer point;
}
