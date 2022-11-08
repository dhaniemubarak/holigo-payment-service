package id.holigo.services.holigopaymentservice.services;

import id.holigo.services.holigopaymentservice.web.model.DigitalWalletCallbackDto;

public interface DigitalWalletCallbackService {
    void newDigitalWallet(DigitalWalletCallbackDto digitalWalletCallbackDto);

    void findTransaction(Long digitalWalletCallbackId);

    void issuedTransaction(Long digitalWalletCallbackId);

    void failedTransaction(Long digitalWalletCallbackId);
}
