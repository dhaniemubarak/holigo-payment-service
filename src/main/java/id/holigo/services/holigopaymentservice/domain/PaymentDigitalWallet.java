package id.holigo.services.holigopaymentservice.domain;

import id.holigo.services.common.model.PaymentStatusEnum;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class PaymentDigitalWallet {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36, columnDefinition = "varchar(36)", updatable = false, nullable = false)
    @Type(type = "org.hibernate.type.UUIDCharType")
    private UUID id;

    private Long userId;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    private Timestamp deletedAt;
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(precision = 10, scale = 2, nullable = false, columnDefinition = "decimal(10,2) default 0")
    private BigDecimal serviceFeeAmount;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal billAmount;

    @Column(columnDefinition = "varchar(100)", nullable = false)
    private String name;

    private String description;

    @Column(columnDefinition = "varchar(25)")
    private String accountNumber;

    @Lob
    private String paymentUrl;

    @Enumerated(EnumType.STRING)
    private PaymentStatusEnum status;

    @Column(columnDefinition = "varchar(100)")
    private String reference;

    private String invoiceNumber;

    private Long callbackId;

    private String note;

    @OneToOne
    private PaymentService paymentService;
}
