package id.holigo.services.holigopaymentservice.web.model;

import id.holigo.services.common.model.PaymentServiceDto;
import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.PaymentService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDigitalWalletDto implements Serializable {

    private UUID id;

    private Long userId;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private BigDecimal totalAmount;

    private BigDecimal serviceFeeAmount;

    private BigDecimal billAmount;

    private String name;

    private String description;

    private String accountNumber;

    private String paymentUrl;

    private String paymentCode;

    private PaymentStatusEnum status;

    private String reference;

    private String invoiceNumber;

    private Long callbackId;

    private String note;

    private PaymentServiceDto paymentService;
}
