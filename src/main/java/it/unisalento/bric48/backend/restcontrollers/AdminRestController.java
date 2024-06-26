package it.unisalento.bric48.backend.restcontrollers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import it.unisalento.bric48.backend.domain.Admin;
import it.unisalento.bric48.backend.dto.AdminDTO;
import it.unisalento.bric48.backend.dto.AuthenticationResponseDTO;
import it.unisalento.bric48.backend.dto.LoginDTO;
import it.unisalento.bric48.backend.repositories.AdminRepository;
import it.unisalento.bric48.backend.security.JwtUtilities;

import static it.unisalento.bric48.backend.configuration.SecurityConfig.passwordEncoder;

import java.util.Optional;


@RestController
@CrossOrigin
@RequestMapping("/admin")
public class AdminRestController {

    @Autowired
    AdminRepository adminRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtilities jwtUtilities;


    // Get JWT Token
    @RequestMapping(value="/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginDTO loginDTO) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getEmail(),
                        loginDTO.getPassword()
                )
        );

        Admin admin = adminRepository.findByEmail(authentication.getName());

        if(admin == null) {
            throw new UsernameNotFoundException(loginDTO.getEmail());
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final String jwt = jwtUtilities.generateToken(admin.getEmail());

        return ResponseEntity.ok(new AuthenticationResponseDTO(jwt));

    }

    // Add a new admin
    @RequestMapping(value="/add", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public AdminDTO addAdmin(@RequestBody AdminDTO adminDTO) {

        Admin newAdmin = new Admin();
        newAdmin.setName(adminDTO.getName());
        newAdmin.setSurname(adminDTO.getSurname());
        newAdmin.setEmail(adminDTO.getEmail());
        newAdmin.setPhoneNumber(adminDTO.getPhoneNumber());
        newAdmin.setPassword(passwordEncoder().encode(adminDTO.getPassword()));

        newAdmin = adminRepository.save(newAdmin);

        adminDTO.setId(newAdmin.getId());
        adminDTO.setPassword(null);

        return adminDTO;
    }

    //Get admin by email
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="/find/{email}", method= RequestMethod.GET)
    public AdminDTO getAdminByEmail(@PathVariable("email") String email) {

        AdminDTO adminDTO = new AdminDTO();

        Admin admin = adminRepository.findByEmail(email);

        adminDTO.setId(admin.getId());
        adminDTO.setName(admin.getName());
        adminDTO.setSurname(admin.getSurname());
        adminDTO.setEmail(admin.getEmail());
        adminDTO.setPassword(admin.getPassword());
        adminDTO.setPhoneNumber(admin.getPhoneNumber());

        return adminDTO;
    }


    // Update admin
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="/updateAdmin", method = RequestMethod.PUT)
    public ResponseEntity<String> updateAdmin(@RequestBody Admin editedAdmin) {

        Optional<Admin> existingAdminOpt = adminRepository.findById(editedAdmin.getId());

        if (existingAdminOpt.isPresent()) {

            adminRepository.save(editedAdmin);
            
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body("ID not found");
        }
    }

}
