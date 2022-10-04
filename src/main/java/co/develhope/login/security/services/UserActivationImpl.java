package co.develhope.login.security.services;

import co.develhope.login.models.Role;
import co.develhope.login.models.RoleEnum;
import co.develhope.login.models.User;
import co.develhope.login.payload.request.SignupActivationDto;
import co.develhope.login.repositories.RoleRepository;
import co.develhope.login.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserActivationImpl {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    public void activate(SignupActivationDto signupActivationDto) throws RuntimeException{
        User user = userRepository.findByActivationCode(signupActivationDto.getActivationCode());
        if (user == null) throw new RuntimeException("user not found");
        user.setActive(true);
        user.setActivationCode(null);

        List<Role> roles = new ArrayList<>();

        roles.add(roleRepository.findByName(RoleEnum.ROLE_USER));
        user.setRoles(roles);
        userRepository.save(user);
    }
}
