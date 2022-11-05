package id.holigo.services.holigopaymentservice.services.logs;

import id.holigo.services.holigopaymentservice.web.model.SupplierLogDto;

public interface LogService {
    void sendSupplierLog(SupplierLogDto supplierLogDto);
}
