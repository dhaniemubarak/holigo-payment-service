package id.holigo.services.holigopaymentservice.web.model;

import java.util.List;

import id.holigo.services.common.model.PaymentServiceDto;
import id.holigo.services.common.model.PaymentServiceStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodDto {
    private String id;

    private String name;

    private PaymentServiceStatusEnum status;

    private List<PaymentServiceDto> paymentServices;
}
