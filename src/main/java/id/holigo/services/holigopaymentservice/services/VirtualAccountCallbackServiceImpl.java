package id.holigo.services.holigopaymentservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import id.holigo.services.holigopaymentservice.domain.PaymentCallbackStatusEnum;
import id.holigo.services.holigopaymentservice.domain.VirtualAccountCallback;
import id.holigo.services.holigopaymentservice.repositories.VirtualAccountCallbackRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class VirtualAccountCallbackServiceImpl implements VirtualAccountCallbackService {

    public static final String VIRTUAL_ACCOUNT_CALLBACK_HEADER = "virtual_account_callback_id";

    @Autowired
    private final VirtualAccountCallbackRepository virtualAccountCallbackRepository;

    @Override
    public VirtualAccountCallback newVirtualAccount(VirtualAccountCallback virtualAccountCallback) {
        virtualAccountCallback.setProcessStatus(PaymentCallbackStatusEnum.RECEIVED);
        VirtualAccountCallback savedVirtualAccountCallback = virtualAccountCallbackRepository
                .save(virtualAccountCallback);
        return savedVirtualAccountCallback;
    }

}
