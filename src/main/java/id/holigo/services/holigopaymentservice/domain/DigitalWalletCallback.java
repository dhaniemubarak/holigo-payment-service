package id.holigo.services.holigopaymentservice.domain;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
public class DigitalWalletCallback {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(columnDefinition = "varchar(5)")
    private String paymentMethod;

    @Column(columnDefinition = "varchar(10)")
    private String paymentMerchant;

    @Column(columnDefinition = "varchar(20)")
    private String accountNumber;

    @Column(precision = 10, scale = 2, nullable = false, columnDefinition = "decimal(10,2) default 0")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentCallbackStatusEnum processStatus;

    private String reference;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    private Timestamp transactionTime;
}
