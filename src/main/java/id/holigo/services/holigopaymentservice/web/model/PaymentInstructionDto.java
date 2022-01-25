package id.holigo.services.holigopaymentservice.web.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentInstructionDto implements Serializable {

    private String indexDescription;

    private Integer sort;

    private String category;
}
