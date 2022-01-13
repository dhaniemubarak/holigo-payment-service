package id.holigo.services.holigopaymentservice.web.mappers;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

import id.holigo.services.holigopaymentservice.domain.PaymentMethod;
import id.holigo.services.holigopaymentservice.web.model.PaymentMethodDto;

@DecoratedWith(PaymentMethodMapperDecorator.class)
@Mapper
public interface PaymentMethodMapper {
    PaymentMethodDto paymentMethodToPaymentMethodDto(PaymentMethod paymentMethod);

    PaymentMethod paymentMethodDtoToPaymentMethod(PaymentMethodDto paymentMethodDto);
}
