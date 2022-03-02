package id.holigo.services.holigopaymentservice.domain;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonBackReference;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class PaymentService {

    @Id
    @Column(columnDefinition = "varchar(10)", nullable = false)
    private String id;

    private String indexName;

    private String imageUrl;

    private Time openTime;

    private Time closeTime;

    private BigDecimal minimumAmount;

    private BigDecimal maximumAmount;

    private BigDecimal serviceFee;

    private BigDecimal mdrPercent;

    private BigDecimal fdsAmount;

    PaymentServiceStatusEnum status;

    private boolean isShow;

    @Column(nullable = true)
    private String note;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference
    private PaymentMethod paymentMethod;

    @Builder.Default
    @OneToMany(mappedBy = "paymentService", fetch = FetchType.EAGER)
    private List<PaymentInstruction> paymentInstructions = new ArrayList<>();

}
