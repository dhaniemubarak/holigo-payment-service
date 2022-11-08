package id.holigo.services.holigopaymentservice.web.mappers;

import id.holigo.services.holigopaymentservice.domain.DigitalWalletCallback;
import id.holigo.services.holigopaymentservice.web.model.DigitalWalletCallbackDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface DigitalWalletCallbackMapper {
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "processStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    DigitalWalletCallback digitalWalletCallbackDtoToDigitalWalletCallback(DigitalWalletCallbackDto digitalWalletCallbackDto);
}
