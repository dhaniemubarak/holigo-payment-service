package id.holigo.services.holigopaymentservice.web.model;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SupplierLogDto implements Serializable {

    private Long userId;

    private String supplier;

    private String url;

    private String code;

    private String message;

    private String logRequest;

    private String logResponse;
}
