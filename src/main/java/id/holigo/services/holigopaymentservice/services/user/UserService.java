package id.holigo.services.holigopaymentservice.services.user;

public interface UserService {
    
    boolean checkPin(Long userId, String pin);
}
