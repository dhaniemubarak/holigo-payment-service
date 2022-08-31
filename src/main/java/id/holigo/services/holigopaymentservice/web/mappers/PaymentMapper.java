package id.holigo.services.holigopaymentservice.web.mappers;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

import id.holigo.services.common.model.PaymentDtoForUser;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.web.model.RequestPaymentDto;
import org.mapstruct.Mapping;

@DecoratedWith(PaymentMapperDecorator.class)
@Mapper
public interface PaymentMapper {
    @Mapping(target = "isFreeServiceFee",ignore = true)
    @Mapping(target = "isFreeAdmin",ignore = true)
    @Mapping(target = "couponValueAmount", ignore = true)
    @Mapping(target = "verifyType", ignore = true)
    @Mapping(target = "verifyId", ignore = true)
    @Mapping(target = "userId",ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "serviceFeeAmount", ignore = true)
    @Mapping(target = "remainingAmount", ignore = true)
    @Mapping(target = "paymentServiceAmount", ignore = true)
    @Mapping(target = "paymentService", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fareAmount", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "detailType", ignore = true)
    @Mapping(target = "detailId",ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Payment requestPaymentDtoToPayment(RequestPaymentDto requestPaymentDto);

    PaymentDtoForUser paymentToPaymentDtoForUser(Payment payment);
}
