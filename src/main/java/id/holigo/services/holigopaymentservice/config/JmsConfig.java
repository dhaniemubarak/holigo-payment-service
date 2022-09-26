package id.holigo.services.holigopaymentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
public class JmsConfig {
    public static final String GET_TRANSACTION_BY_ID = "get-transaction-by-id";

    public static final String ISSUED_TRANSACTION_BY_ID = "issued-transaction-by-id";

    public static final String SET_PAYMENT_IN_TRANSACTION_BY_ID = "set-payment-in-transaction-by-id";

    public static final String UPDATE_PAYMENT_STATUS_BY_PAYMENT_ID = "update-payment-status-by-payment-id";

    public static final String CREATE_APPLY_COUPON = "create-apply-coupon";

    public static final String DEBIT_DEPOSIT = "debit-deposit";

    public static final String CREDIT_DEPOSIT = "credit-deposit";

    public static final String DEBIT_POINT = "debit-point";

    public static final String CREDIT_POINT = "credit-point";

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
}
