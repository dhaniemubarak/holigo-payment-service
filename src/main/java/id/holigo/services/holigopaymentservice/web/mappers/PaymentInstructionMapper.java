package id.holigo.services.holigopaymentservice.web.mappers;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

import id.holigo.services.holigopaymentservice.domain.PaymentInstruction;
import id.holigo.services.holigopaymentservice.web.model.PaymentInstructionDto;

@DecoratedWith(PaymentInstructionMapperDecorator.class)
@Mapper
public interface PaymentInstructionMapper {
    PaymentInstructionDto paymentInstructionToPaymentInstructionDto(PaymentInstruction paymentInstruction);
}
