package co.develhope.login.controllers;

import co.develhope.login.models.Role;
import co.develhope.login.models.RoleEnum;
import co.develhope.login.models.User;
import co.develhope.login.notification.MailNotificationService;
import co.develhope.login.payload.request.LoginRequest;
import co.develhope.login.payload.request.SignupActivationDto;
import co.develhope.login.payload.request.SignupRequest;
import co.develhope.login.payload.response.JwtResponse;
import co.develhope.login.repositories.RoleRepository;
import co.develhope.login.repositories.UserRepository;
import co.develhope.login.security.jwt.JwtUtils;
import co.develhope.login.security.services.UserActivationImpl;
import co.develhope.login.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private MailNotificationService mailNotificationService;



    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> signin(@Valid @RequestBody LoginRequest loginRequest){

        User userControl = userRepository.findByUsername(loginRequest.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),loginRequest.getPassword()));

        if (!userControl.isActive()){
           throw new RuntimeException("user not active");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(user->user.getAuthority())
                .collect(Collectors.toList());


        JwtResponse jwtResponse = new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);

        return new ResponseEntity<JwtResponse>(jwtResponse, HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) throws Exception {
        User userMail = new User();
        userMail.setEmail(signupRequest.getEmail());
        if (userRepository.findByEmail(userMail.getEmail()) != null) {
            return ResponseEntity
                    .badRequest()
                    .body("Email is already in use");
        }

        User userInDB = userRepository.findByUsername(signupRequest.getUsername());
        if (userInDB != null )  throw new Exception("User Already exist");


        User user = new User(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                encoder.encode(signupRequest.getPassword()));

        Set<String> strRoles = signupRequest.getRole();
        List<Role> roles = new ArrayList<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(RoleEnum.ROLE_USER);
            if (userRole == null) new RuntimeException("Role not found");
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(RoleEnum.ROLE_ADMIN);
                        if (adminRole == null) new RuntimeException("Role not found");
                        roles.add(adminRole);
                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(RoleEnum.ROLE_MODERATOR);
                        if (modRole == null) new RuntimeException("Role not found");
                        roles.add(modRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(RoleEnum.ROLE_USER);
                        if (userRole == null) new RuntimeException("Role not found");
                        roles.add(userRole);
                }
            });
            user.setActivationCode(UUID.randomUUID().toString());
            mailNotificationService.sendActivationEmail(user);
        }

        user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity
                .ok()
                .body("Utente creato");
    }

    @Autowired
    private UserActivationImpl activation;
    @PostMapping("/activation")
    public void signup(@RequestBody SignupActivationDto signupActivationDto)throws Exception{

        activation.activate(signupActivationDto);

    }
}
