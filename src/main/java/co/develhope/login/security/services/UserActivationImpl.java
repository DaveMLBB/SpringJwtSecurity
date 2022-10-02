package co.develhope.login.security.services;

import co.develhope.login.models.User;
import co.develhope.login.payload.request.SignupActivationDto;
import co.develhope.login.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserActivationImpl {

    @Autowired
    private UserRepository userRepository;

    public void activate(SignupActivationDto signupActivationDto) throws RuntimeException{
        User user = userRepository.findByActivationCode(signupActivationDto.getActivationCode());
        if (user == null) throw new RuntimeException("user not found");
        user.setActive(true);
        user.setActivationCode(null);
        userRepository.save(user);
    }
}
