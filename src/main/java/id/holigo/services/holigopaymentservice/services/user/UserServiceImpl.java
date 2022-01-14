package id.holigo.services.holigopaymentservice.services.user;

import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Override
    public boolean checkPin(Long userId, String pin) {
        return false;
    }
    
}
