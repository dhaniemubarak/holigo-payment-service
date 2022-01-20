package id.holigo.services.holigopaymentservice.web.mappers;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

import id.holigo.services.holigopaymentservice.domain.BankTransferCallback;
import id.holigo.services.holigopaymentservice.web.model.BankTransferCallbackDto;

@DecoratedWith(BankTransferCallbackMapperDecorator.class)
@Mapper
public interface BankTransferCallbackMapper {

    BankTransferCallbackDto bankTransferCallbackToBankTransferCallbackDto(BankTransferCallback bankTransferCallback);

    BankTransferCallback bankTransferCallbackDtoToBankTransferCallback(BankTransferCallbackDto bankTransferCallbackDto);
}
