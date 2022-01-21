package id.holigo.services.common.events;

import java.io.Serializable;

import id.holigo.services.common.model.TransactionDto;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class TransactionEvent implements Serializable {

    static final long serialVersionUID = -1556695181210L;

    private TransactionDto transactionDto;

    public TransactionEvent(TransactionDto transactionDto) {
        this.transactionDto = transactionDto;
    }
}
