package id.holigo.services.holigopaymentservice.domain;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import id.holigo.services.common.model.PaymentServiceStatusEnum;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PaymentMethod {
    @Id
    @Column(columnDefinition = "varchar(10)", nullable = false)
    private String id;

    private String indexName;

    private PaymentServiceStatusEnum status;

    private Boolean isShow;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @OneToMany(mappedBy = "paymentMethod")
    @OrderBy(value = "createdAt asc")
    @JsonManagedReference
    private List<PaymentService> paymentServices = new ArrayList<>();

}
