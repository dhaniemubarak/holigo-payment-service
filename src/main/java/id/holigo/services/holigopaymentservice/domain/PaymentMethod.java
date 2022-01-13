package id.holigo.services.holigopaymentservice.domain;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PaymentMethod {
    @Id
    @Column(columnDefinition = "varchar(10)", nullable = false)
    private String id;

    private String indexName;

    private PaymentServiceStatusEnum status;

    private boolean isShow;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @OneToMany(mappedBy = "paymentMethod")
    @JsonManagedReference
    private List<PaymentService> paymentServices = new ArrayList<>();

}
