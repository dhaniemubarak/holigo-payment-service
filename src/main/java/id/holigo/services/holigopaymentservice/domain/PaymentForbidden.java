package id.holigo.services.holigopaymentservice.domain;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PaymentForbidden {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String paymentServiceId;

    private Integer productId;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;
}
