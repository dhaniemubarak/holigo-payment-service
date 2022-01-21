INSERT INTO languages (id, locale, message_key,message_content, created_at, updated_at) VALUES
(1, 'en', 'payment_method.bank_transfer','Bank Transfer', now(), now()),
(2, 'en', 'payment_method.virtual_account','Virtual Account', now(), now()),
(3, 'en', 'payment_service.BT_BCA','BCA Bank Transfer', now(), now()),
(4, 'en', 'payment_service.BT_MANDIRI','Mandiri Bank Transfer', now(), now()),
(5, 'en', 'payment_service.BT_BRI','BRI Bank Transfer', now(), now()),
(6, 'en', 'payment_service.VA_BCA','BCA Virtual Account', now(), now()),
(7, 'en', 'payment_service.VA_MANDIRI','Mandiri Virtual Account', now(), now()),
(8, 'en', 'payment_service.VA_BNI','BNI Vitual Account', now(), now()),
(9, 'id', 'payment_method.bank_transfer','Transfer Bank', now(), now()),
(10, 'id', 'payment_method.virtual_account','Virtual Account', now(), now()),
(11, 'id', 'payment_service.BT_BCA','Transfer Bank BCA', now(), now()),
(12, 'id', 'payment_service.BT_MANDIRI','Transfer Bank Mandiri', now(), now()),
(13, 'id', 'payment_service.BT_BRI','Transfer Bank BRI', now(), now()),
(14, 'id', 'payment_service.VA_BCA','Virtual Account BCA', now(), now()),
(15, 'id', 'payment_service.VA_MANDIRI','Virtual Account Mandiri', now(), now()),
(16, 'id', 'payment_service.VA_BNI','Vitual Account BNI', now(), now()),
(17, 'id', 'holigo-transaction-service.not_found','Transaksi tidak ditemukan', now(), now()),
(18, 'en', 'holigo-transaction-service.not_found','Transaction not found.', now(), now()),
(19, 'id', 'payment.user_transaction_not_match','Transaksi Anda tidak valid.', now(), now()),
(20, 'en', 'payment.user_transaction_not_match','Your transaction not valid.', now(), now()),
(21, 'id', 'payment.waiting_payment','Transaksi sedang menunggu pembayaran.', now(), now()),
(22, 'en', 'payment.waiting_payment','Transaction is waiting for payment. .', now(), now()),
(23, 'id', 'payment.verifying_payment','Pembayaran sedang diverifikasi..', now(), now()),
(24, 'en', 'payment.verifying_payment','Payment is verifying process.', now(), now()),
(25, 'id', 'payment.payment_failed','Pembayaran gagal.', now(), now()),
(26, 'en', 'payment.payment_failed','Payment is failed.', now(), now()),
(27, 'id', 'payment.paid','Transaksi sudah dibayar.', now(), now()),
(28, 'en', 'payment.paid','Transaction already paid.', now(), now()),
(29, 'id', 'payment.payment_canceled','Transaksi telah dibatalkan.', now(), now()),
(30, 'en', 'payment.payment_canceled','Transaction has been canceled.', now(), now()),
(31, 'id', 'payment.payment_expired','Transaksi telah kadaluarsa', now(), now()),
(32, 'en', 'payment.payment_expired','Transaction has been expired.', now(), now()),
(33, 'id', 'payment.process_refund','Dana sedang diproses untuk dikembalikan.', now(), now()),
(34, 'en', 'payment.process_refund','Payment is being processed for refund.', now(), now()),
(35, 'id', 'payment.waiting_refund','Dana sedang dalam proses pengembalian.', now(), now()),
(36, 'en', 'payment.waiting_refund','Payment is in the process for refunded.', now(), now()),
(37, 'id', 'payment.refunded','Dana telah dikembalikan.', now(), now()),
(38, 'en', 'payment.refunded','Payment has been refunded.', now(), now());


INSERT INTO PAYMENT_METHOD (ID,CREATED_AT,INDEX_NAME,IS_SHOW, STATUS,UPDATED_AT) VALUES ('10', now(), 'payment_method.bank_transfer', 1,1, now());
INSERT INTO PAYMENT_METHOD (ID,CREATED_AT,INDEX_NAME,IS_SHOW, STATUS,UPDATED_AT) VALUES ('11', now(), 'payment_method.virtual_account', 1,1, now());
INSERT INTO PAYMENT_SERVICE (ID,CLOSE_TIME,CREATED_AT,FDS_AMOUNT,IMAGE_URL,INDEX_NAME,IS_SHOW,MDR_PERCENT,MINIMUM_AMOUNT,NOTE,OPEN_TIME,SERVICE_FEE,STATUS,UPDATED_AT,PAYMENT_METHOD_ID) VALUES ('BT_BCA', '21:00:00', now(), 0, 'https://api.smartinpays.com/storage/images/bank-bca.png', 'payment_service.BT_BCA', 1, 0, 10000, 'payment_service.note.BT_BCA', '00:15:00', 0, 1, now(), '10');
INSERT INTO PAYMENT_SERVICE (ID,CLOSE_TIME,CREATED_AT,FDS_AMOUNT,IMAGE_URL,INDEX_NAME,IS_SHOW,MDR_PERCENT,MINIMUM_AMOUNT,NOTE,OPEN_TIME,SERVICE_FEE,STATUS,UPDATED_AT,PAYMENT_METHOD_ID) VALUES ('BT_MANDIRI', '21:00:00', now(), 0, 'https://api.smartinpays.com/storage/images/bank-mandiri.png', 'payment_service.BT_MANDIRI', 1, 0, 10000, 'payment_service.note.BT_MANDIRI', '00:15:00', 0, 1, now(), '10');
INSERT INTO PAYMENT_SERVICE (ID,CLOSE_TIME,CREATED_AT,FDS_AMOUNT,IMAGE_URL,INDEX_NAME,IS_SHOW,MDR_PERCENT,MINIMUM_AMOUNT,NOTE,OPEN_TIME,SERVICE_FEE,STATUS,UPDATED_AT,PAYMENT_METHOD_ID) VALUES ('BT_BRI', '21:00:00', now(), 0, 'https://api.smartinpays.com/storage/images/bank-bri.png', 'payment_service.BT_BRI', 1, 0, 10000, 'payment_service.note.BT_BRI', '00:15:00', 0, 1, now(), '10');
INSERT INTO PAYMENT_SERVICE (ID,CLOSE_TIME,CREATED_AT,FDS_AMOUNT,IMAGE_URL,INDEX_NAME,IS_SHOW,MDR_PERCENT,MINIMUM_AMOUNT,NOTE,OPEN_TIME,SERVICE_FEE,STATUS,UPDATED_AT,PAYMENT_METHOD_ID) VALUES ('VA_BCA', '21:00:00', now(), 1500, 'https://api.smartinpays.com/storage/images/bank-bri.png', 'payment_service.VA_BCA', 1, 0, 10000, 'payment_service.note.BT_BRI', '00:15:00', 0, 1, now(), '11');
INSERT INTO PAYMENT_SERVICE (ID,CLOSE_TIME,CREATED_AT,FDS_AMOUNT,IMAGE_URL,INDEX_NAME,IS_SHOW,MDR_PERCENT,MINIMUM_AMOUNT,NOTE,OPEN_TIME,SERVICE_FEE,STATUS,UPDATED_AT,PAYMENT_METHOD_ID) VALUES ('VA_MANDIRI', '21:00:00', now(), 1500, 'https://api.smartinpays.com/storage/images/bank-mandiri.png', 'payment_service.VA_MANDIRI', 1, 0, 10000, 'payment_service.note.BT_BRI', '00:15:00', 0, 1, now(), '11');
INSERT INTO PAYMENT_SERVICE (ID,CLOSE_TIME,CREATED_AT,FDS_AMOUNT,IMAGE_URL,INDEX_NAME,IS_SHOW,MDR_PERCENT,MINIMUM_AMOUNT,NOTE,OPEN_TIME,SERVICE_FEE,STATUS,UPDATED_AT,PAYMENT_METHOD_ID) VALUES ('VA_BNI', '21:00:00', now(), 1500, 'https://api.smartinpays.com/storage/images/logo-bni-custom.png', 'payment_service.VA_BNI', 1, 0, 10000, 'payment_service.note.BT_BRI', '00:15:00', 0, 1, now(), '11');
INSERT INTO PAYMENT_BANK_TRANSFER(ID, PAYMENT_SERVICE_ID, TOTAL_AMOUNT, VAT_AMOUNT, FDS_AMOUNT, UNIQUE_CODE, SERVICE_FEE_AMOUNT, BILL_AMOUNT, STATUS, CREATED_AT, UPDATED_AT) VALUES ('ef9abf24-a4bd-45a6-9191-293d6ac61c0b', 'BT_BCA', 98500, 0, 0, 35, 35, 98535, 'WAITING_PAYMENT', now(), now());
INSERT INTO PAYMENT(ID,TRANSACTION_ID,USER_ID,PAYMENT_SERVICE_ID,FARE_AMOUNT,SERVICE_FEE_AMOUNT,DISCOUNT_AMOUNT,TOTAL_AMOUNT,PAYMENT_SERVICE_AMOUNT,IS_SPLIT_BILL,POINT_AMOUNT,REMAINING_AMOUNT,STATUS,DETAIL_TYPE,DETAIL_ID,CREATED_AT,UPDATED_AT) VALUES ('51f80e20-b573-447b-a365-040f53fb6e5b', '1ef35204-a505-455a-894a-b1b5cdf43b44',5,'BT_BCA', 98500, 35, 0, 98535, 98535, false, 0, 98535, 'WAITING_PAYMENT', 'bankTransfer', 'ef9abf24-a4bd-45a6-9191-293d6ac61c0b', now(), now());