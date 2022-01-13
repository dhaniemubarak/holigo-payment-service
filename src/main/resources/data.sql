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
(16, 'id', 'payment_service.VA_BNI','Vitual Account BNI', now(), now());

INSERT INTO PAYMENT_METHOD (ID,CREATED_AT,INDEX_NAME,IS_SHOW, STATUS,UPDATED_AT) VALUES ('10', now(), 'payment_method.bank_transfer', 1,1, now());
INSERT INTO PAYMENT_METHOD (ID,CREATED_AT,INDEX_NAME,IS_SHOW, STATUS,UPDATED_AT) VALUES ('11', now(), 'payment_method.virtual_account', 1,1, now());
INSERT INTO PAYMENT_SERVICE (ID,CLOSE_TIME,CREATED_AT,FDS_AMOUNT,IMAGE_URL,INDEX_NAME,IS_SHOW,MDR_PERCENT,MINIMUM_AMOUNT,NOTE,OPEN_TIME,SERVICE_FEE,STATUS,UPDATED_AT,PAYMENT_METHOD_ID) VALUES ('BT_BCA', '21:00:00', now(), 0, 'https://api.smartinpays.com/storage/images/bank-bca.png', 'payment_service.BT_BCA', 1, 0, 10000, 'payment_service.note.BT_BCA', '00:15:00', 0, 1, now(), '10');
INSERT INTO PAYMENT_SERVICE (ID,CLOSE_TIME,CREATED_AT,FDS_AMOUNT,IMAGE_URL,INDEX_NAME,IS_SHOW,MDR_PERCENT,MINIMUM_AMOUNT,NOTE,OPEN_TIME,SERVICE_FEE,STATUS,UPDATED_AT,PAYMENT_METHOD_ID) VALUES ('BT_MANDIRI', '21:00:00', now(), 0, 'https://api.smartinpays.com/storage/images/bank-mandiri.png', 'payment_service.BT_MANDIRI', 1, 0, 10000, 'payment_service.note.BT_MANDIRI', '00:15:00', 0, 1, now(), '10');
INSERT INTO PAYMENT_SERVICE (ID,CLOSE_TIME,CREATED_AT,FDS_AMOUNT,IMAGE_URL,INDEX_NAME,IS_SHOW,MDR_PERCENT,MINIMUM_AMOUNT,NOTE,OPEN_TIME,SERVICE_FEE,STATUS,UPDATED_AT,PAYMENT_METHOD_ID) VALUES ('BT_BRI', '21:00:00', now(), 0, 'https://api.smartinpays.com/storage/images/bank-bri.png', 'payment_service.BT_BRI', 1, 0, 10000, 'payment_service.note.BT_BRI', '00:15:00', 0, 1, now(), '10');
INSERT INTO PAYMENT_SERVICE (ID,CLOSE_TIME,CREATED_AT,FDS_AMOUNT,IMAGE_URL,INDEX_NAME,IS_SHOW,MDR_PERCENT,MINIMUM_AMOUNT,NOTE,OPEN_TIME,SERVICE_FEE,STATUS,UPDATED_AT,PAYMENT_METHOD_ID) VALUES ('VA_BCA', '21:00:00', now(), 1500, 'https://api.smartinpays.com/storage/images/bank-bri.png', 'payment_service.VA_BCA', 1, 0, 10000, 'payment_service.note.BT_BRI', '00:15:00', 0, 1, now(), '11');
INSERT INTO PAYMENT_SERVICE (ID,CLOSE_TIME,CREATED_AT,FDS_AMOUNT,IMAGE_URL,INDEX_NAME,IS_SHOW,MDR_PERCENT,MINIMUM_AMOUNT,NOTE,OPEN_TIME,SERVICE_FEE,STATUS,UPDATED_AT,PAYMENT_METHOD_ID) VALUES ('VA_MANDIRI', '21:00:00', now(), 1500, 'https://api.smartinpays.com/storage/images/bank-mandiri.png', 'payment_service.VA_MANDIRI', 1, 0, 10000, 'payment_service.note.BT_BRI', '00:15:00', 0, 1, now(), '11');
INSERT INTO PAYMENT_SERVICE (ID,CLOSE_TIME,CREATED_AT,FDS_AMOUNT,IMAGE_URL,INDEX_NAME,IS_SHOW,MDR_PERCENT,MINIMUM_AMOUNT,NOTE,OPEN_TIME,SERVICE_FEE,STATUS,UPDATED_AT,PAYMENT_METHOD_ID) VALUES ('VA_BNI', '21:00:00', now(), 1500, 'https://api.smartinpays.com/storage/images/logo-bni-custom.png', 'payment_service.VA_BNI', 1, 0, 10000, 'payment_service.note.BT_BRI', '00:15:00', 0, 1, now(), '11');