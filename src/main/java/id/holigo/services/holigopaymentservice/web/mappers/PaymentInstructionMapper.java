package id.holigo.services.holigopaymentservice.web.mappers;

import org.mapstruct.Mapper;

import id.holigo.services.holigopaymentservice.domain.PaymentInstruction;
import id.holigo.services.holigopaymentservice.web.model.PaymentInstructionDto;

@Mapper
public interface PaymentInstructionMapper {
    PaymentInstructionDto paymentInstructionToPaymentInstructionDto(PaymentInstruction paymentInstruction);
}
