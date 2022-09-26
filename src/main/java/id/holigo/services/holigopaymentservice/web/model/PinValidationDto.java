package id.holigo.services.holigopaymentservice.web.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PinValidationDto implements Serializable {
    private String pin;
    private Integer attemptGranted;
}
