package id.holigo.services.holigopaymentservice.domain;

public enum BankTransferStatusEvent {
    FIND_TRANSACTION, TRANSACTION_NOT_FOUND, PROCESS_ISSUED, ISSUED, ISSUED_FAILED;
}
