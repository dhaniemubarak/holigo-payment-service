package id.holigo.services.holigopaymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.time.Year;
import java.util.Calendar;
import java.util.Stack;

@EnableFeignClients
@SpringBootApplication
public class HoligoPaymentServiceApplication {

    public static void main(String[] args) {
        System.out.println();
        SpringApplication.run(HoligoPaymentServiceApplication.class, args);
    }

}
