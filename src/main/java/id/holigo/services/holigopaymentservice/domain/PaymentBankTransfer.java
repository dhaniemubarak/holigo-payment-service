package id.holigo.services.holigopaymentservice.domain;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import id.holigo.services.common.model.PaymentStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class PaymentBankTransfer {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36, columnDefinition = "varchar(36)", updatable = false, nullable = false)
    @Type(type = "org.hibernate.type.UUIDCharType")
    private UUID id;

    @Column(columnDefinition = "varchar(10)", nullable = false)
    private String paymentServiceId;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(precision = 10, scale = 2, nullable = false, columnDefinition = "decimal(10,2) default 0")
    private BigDecimal vatAmount;

    @Column(precision = 10, scale = 2, nullable = false, columnDefinition = "decimal(10,2) default 0")
    private BigDecimal fdsAmount;

    @Column(nullable = false, columnDefinition = "int(4) default 0")
    private Integer uniqueCode;

    @Column(precision = 10, scale = 2, nullable = false, columnDefinition = "decimal(10,2) default 0")
    private BigDecimal serviceFeeAmount;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal billAmount;

    @Enumerated(EnumType.STRING)
    private PaymentStatusEnum status;

    private String reference;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @Column(nullable = true)
    private Timestamp deletedAt;

    @Column(nullable = true)
    private Long callbackId;

}
