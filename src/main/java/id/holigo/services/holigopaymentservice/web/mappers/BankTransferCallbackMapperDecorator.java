package id.holigo.services.holigopaymentservice.web.mappers;

import org.springframework.beans.factory.annotation.Autowired;

import id.holigo.services.holigopaymentservice.domain.BankTransferCallback;
import id.holigo.services.holigopaymentservice.web.model.BankTransferCallbackDto;

public abstract class BankTransferCallbackMapperDecorator implements BankTransferCallbackMapper {

    private BankTransferCallbackMapper bankTransferCallbackMapper;

    @Autowired
    public void setBankTransferCallbackMapper(BankTransferCallbackMapper bankTransferCallbackMapper) {
        this.bankTransferCallbackMapper = bankTransferCallbackMapper;
    }

    @Override
    public BankTransferCallback bankTransferCallbackDtoToBankTransferCallback(
            BankTransferCallbackDto bankTransferCallbackDto) {
        BankTransferCallback bankTransferCallback = bankTransferCallbackMapper
                .bankTransferCallbackDtoToBankTransferCallback(bankTransferCallbackDto);
        bankTransferCallback.setPaymentMerchant(paymentServiceId(bankTransferCallbackDto.getPaymentMerchant()));
        return bankTransferCallback;
    }

    @Override
    public BankTransferCallbackDto bankTransferCallbackToBankTransferCallbackDto(
            BankTransferCallback bankTransferCallback) {
        return bankTransferCallbackMapper.bankTransferCallbackToBankTransferCallbackDto(bankTransferCallback);
    }

    private String paymentServiceId(String paymentMerchant) {
        String paymentServiceId = paymentMerchant;
        switch (paymentMerchant) {
            case "BCA":
                paymentServiceId = "BT_BCA";
                break;
        }
        return paymentServiceId;
    }
}
