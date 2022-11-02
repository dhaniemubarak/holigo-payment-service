package id.holigo.services.holigopaymentservice.web.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import id.holigo.services.holigopaymentservice.services.BankTransferCallbackService;
import id.holigo.services.holigopaymentservice.services.VirtualAccountCallbackService;
import id.holigo.services.holigopaymentservice.web.mappers.BankTransferCallbackMapper;
import id.holigo.services.holigopaymentservice.web.mappers.VirtualAccountCallbackMapper;
import id.holigo.services.holigopaymentservice.web.model.BankTransferCallbackDto;
import id.holigo.services.holigopaymentservice.web.model.VirtualAccountCallbackDto;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class PaymentCallbackController {

    @Autowired
    private final BankTransferCallbackService bankTransferCallbackService;

    private final BankTransferCallbackMapper bankTransferCallbackMapper;

    @Autowired
    private final VirtualAccountCallbackService virtualAccountCallbackService;

    private final VirtualAccountCallbackMapper virtualAccountCallbackMapper;

    @PostMapping("/api/v1/bankTransferCallback")
    public ResponseEntity<?> bankTransfer(@Valid @RequestBody BankTransferCallbackDto bankTransferCallbackDto) {
        bankTransferCallbackService.newBankTransfer(
                bankTransferCallbackMapper.bankTransferCallbackDtoToBankTransferCallback(bankTransferCallbackDto));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/api/v1/digitalWalletCallback")
    public ResponseEntity<?> digitalWallet(@Valid @RequestBody VirtualAccountCallbackDto virtualAccountCallbackDto) {
        virtualAccountCallbackService.newVirtualAccount(virtualAccountCallbackMapper
                .virtualAccountCallbackDtoToVirtualAccountCallback(virtualAccountCallbackDto));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/api/v1/virtualAccountCallback")
    public ResponseEntity<?> virtualAccount(@Valid @RequestBody VirtualAccountCallbackDto virtualAccountCallbackDto) {
        virtualAccountCallbackService.newVirtualAccount(virtualAccountCallbackMapper
                .virtualAccountCallbackDtoToVirtualAccountCallback(virtualAccountCallbackDto));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
