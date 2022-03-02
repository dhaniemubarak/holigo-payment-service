package id.holigo.services.holigopaymentservice.web.mappers;

import org.mapstruct.Mapper;

import id.holigo.services.holigopaymentservice.domain.VirtualAccountCallback;
import id.holigo.services.holigopaymentservice.web.model.VirtualAccountCallbackDto;

@Mapper
public interface VirtualAccountCallbackMapper {

    VirtualAccountCallback virtualAccountCallbackDtoToVirtualAccountCallback(
            VirtualAccountCallbackDto virtualAccountCallbackDto);

    VirtualAccountCallbackDto virtualAccountCallbackToVirtualAccountCallbackDto(
            VirtualAccountCallback virtualAccountCallback);
}
