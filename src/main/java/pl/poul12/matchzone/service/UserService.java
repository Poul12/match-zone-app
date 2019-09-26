package pl.poul12.matchzone.service;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.poul12.matchzone.exception.ResourceNotFoundException;
import pl.poul12.matchzone.model.*;
import pl.poul12.matchzone.model.enums.RoleName;
import pl.poul12.matchzone.repository.*;
import pl.poul12.matchzone.security.forms.RegisterForm;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private EntityManager entityManager;

    private UserRepository userRepository;
    private RoleRepository roleRepository;

    private PersonalDetailsRepository personalDetailsRepository;
    private AppearanceRepository appearanceRepository;
    private VoteRepository voteRepository;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PersonalDetailsRepository personalDetailsRepository,
                       AppearanceRepository appearanceRepository, VoteRepository voteRepository, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.personalDetailsRepository = personalDetailsRepository;
        this.appearanceRepository = appearanceRepository;
        this.entityManager = entityManager;
        this.voteRepository = voteRepository;
    }

    public List<User> getAllUsers(){

        return userRepository.findAll();
    }

    public User createUser(RegisterForm registerUser){

        User user = buildUser(registerUser);

        Set<String> strRoles = registerUser.getRole();

        Set<Role> roles = new HashSet<>();
        for(String role : strRoles) {
            Role roleFound = roleRepository.findByName(RoleName.valueOf(role.toUpperCase())).orElseThrow(() -> new RuntimeException("Not found any role!"));
            roles.add(roleFound);
        }

        user.setRoles(roles);

        return userRepository.save(user);
    }

    public User getUserById(Long id) throws ResourceNotFoundException {

        Optional<User> userFound = userRepository.findById(id);

        return userFound.orElseThrow(() -> new ResourceNotFoundException("User not found for this id: " + id)
        );
    }

    public Optional<User> getUserByUsername(String username) throws UsernameNotFoundException {

        return userRepository.findUserByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) throws UsernameNotFoundException {

        return userRepository.findUserByEmail(email);
    }

    public ResponseEntity<String> savePhoto(Long id, MultipartFile file) throws ResourceNotFoundException {

        User user = getUserById(id);
        PersonalDetails personalDetails = personalDetailsRepository.findByUserId(user.getId()).orElseThrow(
                () -> new ResourceNotFoundException("PersonalDetails not found for this id: " + id)
        );

        try {
            personalDetails.setPhoto(file.getBytes());
            personalDetailsRepository.save(personalDetails);
            logger.info("Photo uploaded");
            return ResponseEntity.status(HttpStatus.OK).body("You successfully uploaded " + file.getOriginalFilename() + "!");
        }catch (IOException e){
            logger.error("Something went wrong with your file: {}", file.getOriginalFilename());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Something went wrong with your file" + file.getOriginalFilename());
        }
    }

    public User updateUser(Long id, User user) throws ResourceNotFoundException {

        //User userFound = getUserById(id);

        return userRepository.save(user);
    }

    public Map<String, Boolean> deleteUser(Long id) throws ResourceNotFoundException {

        User user = getUserById(id);

        userRepository.delete(user);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return response;
    }


    private User buildUser(RegisterForm registerUser){

        User user = new User();
        user.setUsername(registerUser.getUsername());
        user.setFirstName(registerUser.getName());
        user.setEmail(registerUser.getEmail());
        user.setPassword(passwordEncoder.encode(registerUser.getPassword()));
        PersonalDetails personalDetails = new PersonalDetails();
        personalDetails.setDateOfBirth(LocalDate.parse(registerUser.getDateOfBirth(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        personalDetails.setUser(user);
        Appearance appearance = new Appearance();
        appearance.setUser(user);
        Vote vote = new Vote();
        vote.setUser(user);

        personalDetailsRepository.save(personalDetails);
        appearanceRepository.save(appearance);
        voteRepository.save(vote);

        return user;
    }


}