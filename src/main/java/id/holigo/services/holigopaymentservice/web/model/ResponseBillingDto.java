package id.holigo.services.holigopaymentservice.web.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseBillingDto implements Serializable {
    private Boolean status;
    private String error_code;
    private String error_message;
    private String filePath;
    private ResponseBillingDataDto data;
}
