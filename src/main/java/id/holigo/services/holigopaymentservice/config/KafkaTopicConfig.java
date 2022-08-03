package id.holigo.services.holigopaymentservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String UPDATE_PAYMENT = "update-payment";

    public static final String CANCEL_PAYMENT = "cancel-payment";

    @Bean
    public NewTopic updatePayment() {
        return TopicBuilder.name(UPDATE_PAYMENT).build();
    }

    public NewTopic cancelPayment() {
        return TopicBuilder.name(CANCEL_PAYMENT).build();
    }


}
