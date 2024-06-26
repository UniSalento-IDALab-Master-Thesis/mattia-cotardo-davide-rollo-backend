package it.unisalento.bric48.backend.restcontrollers;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.unisalento.bric48.backend.domain.Headphones;
import it.unisalento.bric48.backend.domain.Worker;
import it.unisalento.bric48.backend.dto.AuthenticationResponseDTO;
import it.unisalento.bric48.backend.dto.LoginDTO;
import it.unisalento.bric48.backend.dto.WorkerDTO;
import it.unisalento.bric48.backend.repositories.HeadphonesRepository;
import it.unisalento.bric48.backend.repositories.WorkerRepository;
import it.unisalento.bric48.backend.security.JwtUtilities;

import static it.unisalento.bric48.backend.configuration.SecurityConfig.passwordEncoder;

@RestController
@CrossOrigin
@RequestMapping("/worker")
public class WorkerRestController {

    @Autowired
    WorkerRepository workerRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtilities jwtUtilities;

    @Autowired
    HeadphonesRepository headphonesRepository;


    @PreAuthorize("hasRole('SECURITY_MANAGER')")
    @RequestMapping(value="/test", method = RequestMethod.GET)
    public String test() {
        return "suca";
    }

    // Get JWT Token
    @RequestMapping(value="/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginDTO loginDTO) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getEmail(),
                        loginDTO.getPassword()
                )
        );

        Optional<Worker> workerOpt = workerRepository.findByEmail(authentication.getName());

        Worker worker = new Worker();
        if(workerOpt.isPresent()){
            worker = workerOpt.get();
        }

        if(worker == null) {
            throw new UsernameNotFoundException(loginDTO.getEmail());
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final String jwt = jwtUtilities.generateToken(worker.getEmail());

        return ResponseEntity.ok(new AuthenticationResponseDTO(jwt));

    }

    // Add a new worker
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/add", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public WorkerDTO addWorker(@RequestBody WorkerDTO workerDTO) {

        Worker newWorker = new Worker();
        newWorker.setRollNumber(workerDTO.getRollNumber());
        newWorker.setName(workerDTO.getName());
        newWorker.setSurname(workerDTO.getSurname());
        newWorker.setEmail(workerDTO.getEmail());
        
        if (workerDTO.getPassword() != null && !workerDTO.getPassword().isEmpty()) {
            newWorker.setPassword(passwordEncoder().encode(workerDTO.getPassword()));
        }
        
        newWorker.setPhoneNumber(workerDTO.getPhoneNumber());
        newWorker.setRole(workerDTO.getRole());
        newWorker.setIdHeadphones(workerDTO.getIdHeadphones());

        newWorker = workerRepository.save(newWorker);

        workerDTO.setId(newWorker.getId());
        workerDTO.setPassword(null);

        if (workerDTO.getIdHeadphones() != null && !workerDTO.getIdHeadphones().isEmpty()) {
            Optional<Headphones> existingHeadphonesOpt = headphonesRepository.findBySerial(workerDTO.getIdHeadphones());

            if (existingHeadphonesOpt.isPresent()) {
                Headphones existingHeadphones = existingHeadphonesOpt.get();
                existingHeadphones.setIsAssociated("True");
                existingHeadphones = headphonesRepository.save(existingHeadphones);
            }
        }

        return workerDTO;
    }


    //Get all workers
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECURITY_MANAGER')")
    @RequestMapping(value="/getAll", method= RequestMethod.GET)
    public List<WorkerDTO> getAllWorkers() {

        List<WorkerDTO> workers = new ArrayList<>();

        for(Worker worker : workerRepository.findAll()) {

            WorkerDTO workerDTO = new WorkerDTO();

            workerDTO.setId(worker.getId());
            workerDTO.setName(worker.getName());
            workerDTO.setSurname(worker.getSurname());
            workerDTO.setRole(worker.getRole());
            workerDTO.setRollNumber(worker.getRollNumber());
            workerDTO.setEmail(worker.getEmail());
            workerDTO.setPhoneNumber(worker.getPhoneNumber());
            workerDTO.setIdHeadphones(worker.getIdHeadphones());

            workers.add(workerDTO);
        }

        return workers;
    }


    //Get workers from-to
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="/getWorkersFromTo", method= RequestMethod.GET)
    public List<WorkerDTO> getWorkersFromTo(@RequestParam("from") String from, @RequestParam("to") String to) {

        int c = 1;
        int i = Integer.parseInt(from);
        int j = Integer.parseInt(to);

        List<WorkerDTO> workers = new ArrayList<>();

        for(Worker worker : workerRepository.findAll()) {

            if(c >= i && c <= j){

                WorkerDTO workerDTO = new WorkerDTO();

                workerDTO.setId(worker.getId());
                workerDTO.setName(worker.getName());
                workerDTO.setSurname(worker.getSurname());
                workerDTO.setRole(worker.getRole());
                workerDTO.setRollNumber(worker.getRollNumber());
                workerDTO.setEmail(worker.getEmail());
                workerDTO.setPhoneNumber(worker.getPhoneNumber());
                workerDTO.setIdHeadphones(worker.getIdHeadphones());

                workers.add(workerDTO);
            }

            c++;
        }

        return workers;
    }


    //Get worker by idHeadphones
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECURITY_MANAGER')")
    @RequestMapping(value="/find/{idHeadphones}", method= RequestMethod.GET)
    public WorkerDTO getWorkerByIdHeadphones(@PathVariable("idHeadphones") String idHeadphones) {

        WorkerDTO workerDTO = new WorkerDTO();

        Worker worker = workerRepository.findByIdHeadphones(idHeadphones);

        workerDTO.setId(worker.getId());
        workerDTO.setRollNumber(worker.getRollNumber());
        workerDTO.setName(worker.getName());
        workerDTO.setSurname(worker.getSurname());
        workerDTO.setEmail(worker.getEmail());
        workerDTO.setPhoneNumber(worker.getPhoneNumber());
        workerDTO.setRole(worker.getRole());
        workerDTO.setIdHeadphones(worker.getIdHeadphones());

        return workerDTO;
    }

    //Get worker by email
    @RequestMapping(value="/findByEmail", method= RequestMethod.GET)
    public WorkerDTO getWorkerByEmail(@RequestParam("email") String email) {

        WorkerDTO workerDTO = new WorkerDTO();

        Optional<Worker> workerOpt = workerRepository.findByEmail(email);

        if(workerOpt.isPresent()){
            Worker worker = workerOpt.get();

            workerDTO.setId(worker.getId());
            workerDTO.setRollNumber(worker.getRollNumber());
            workerDTO.setName(worker.getName());
            workerDTO.setSurname(worker.getSurname());
            workerDTO.setEmail(worker.getEmail());
            workerDTO.setPhoneNumber(worker.getPhoneNumber());
            workerDTO.setRole(worker.getRole());
            workerDTO.setIdHeadphones(worker.getIdHeadphones());

        }

        return workerDTO;
        
    }

    //Get worker by rollNumber
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="/findByRollNumber", method= RequestMethod.GET)
    public WorkerDTO getWorkerByRollNumber(@RequestParam("rollNumber") String rollNumber) {

        WorkerDTO workerDTO = new WorkerDTO();

        Optional<Worker> workerOpt = workerRepository.findByRollNumber(rollNumber);

        if(workerOpt.isPresent()){
            Worker worker = workerOpt.get();

            workerDTO.setId(worker.getId());
            workerDTO.setRollNumber(worker.getRollNumber());
            workerDTO.setName(worker.getName());
            workerDTO.setSurname(worker.getSurname());
            workerDTO.setEmail(worker.getEmail());
            workerDTO.setPhoneNumber(worker.getPhoneNumber());
            workerDTO.setRole(worker.getRole());
            workerDTO.setIdHeadphones(worker.getIdHeadphones());

        }

        return workerDTO;
        
    }

    //Get worker by id
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECURITY_MANAGER')")
    @RequestMapping(value="/findById/{id}", method= RequestMethod.GET)
    public WorkerDTO getWorkerById(@PathVariable("id") String id) {

        WorkerDTO workerDTO = new WorkerDTO();

        Optional<Worker> workerOpt = workerRepository.findById(id);

        if (workerOpt.isPresent()) {

            Worker worker = workerOpt.get();

            workerDTO.setId(worker.getId());
            workerDTO.setRollNumber(worker.getRollNumber());
            workerDTO.setName(worker.getName());
            workerDTO.setSurname(worker.getSurname());
            workerDTO.setEmail(worker.getEmail());
            workerDTO.setPassword(worker.getPassword());
            workerDTO.setPhoneNumber(worker.getPhoneNumber());
            workerDTO.setRole(worker.getRole());
            workerDTO.setIdHeadphones(worker.getIdHeadphones());
        }

        return workerDTO;
    }

    // Delete worker by id
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteWorkerById(@PathVariable("id") String id) {

        Optional<Worker> workerOpt = workerRepository.findById(id);
        Worker worker = workerOpt.get();

        if(worker.getIdHeadphones() != ""){
            Optional<Headphones> existingHeadphonesOpt = headphonesRepository.findBySerial(worker.getIdHeadphones());

            if (existingHeadphonesOpt.isPresent()) {
                Headphones existingHeadphones = existingHeadphonesOpt.get();
                existingHeadphones.setIsAssociated("False");
                existingHeadphones = headphonesRepository.save(existingHeadphones);
            }
        }
  
        workerRepository.deleteById(id);

        // Verifica se l'entità è stata eliminata con successo
        Optional<Worker> deletedEntity = workerRepository.findById(id);
        if (!deletedEntity.isEmpty()) {
            return ResponseEntity.badRequest().body("ID not found");
        }

        return ResponseEntity.ok().build();
    }

    // Update idHeadphones
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="/updateIdHeadphones", method = RequestMethod.PUT)
    public ResponseEntity<String> updateIdHeadphones(@RequestParam("oldIdHeadphones") String oldIdHeadphones, @RequestParam("newIdHeadphones") String newIdHeadphones) {

        Worker existingWorker = workerRepository.findByIdHeadphones(oldIdHeadphones);

        if (existingWorker != null) {
            existingWorker.setIdHeadphones(newIdHeadphones);
            existingWorker = workerRepository.save(existingWorker); 
        }
        
        return ResponseEntity.ok().build();
    }

    // Update worker
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECURITY_MANAGER')")
    @RequestMapping(value="/updateWorker", method = RequestMethod.PUT)
    public ResponseEntity<String> updateWorker(@RequestBody Worker editedWorker) {

        Optional<Worker> existingWorkerOpt = workerRepository.findById(editedWorker.getId());
    
        if (existingWorkerOpt.isPresent()) {
            Worker existingWorker = existingWorkerOpt.get();
    
            if (editedWorker.getIdHeadphones() != null && !editedWorker.getIdHeadphones().isEmpty()) {
                Optional<Headphones> existingHeadphonesOpt = headphonesRepository.findBySerial(editedWorker.getIdHeadphones());
    
                if (existingHeadphonesOpt.isPresent()) {
                    Headphones existingHeadphones = existingHeadphonesOpt.get();
                    existingHeadphones.setIsAssociated("True");
                    headphonesRepository.save(existingHeadphones);
                    existingWorker.setIdHeadphones(editedWorker.getIdHeadphones());
                }
            }
    
            if (editedWorker.getPassword() != null && !editedWorker.getPassword().isEmpty()) {
                editedWorker.setPassword(passwordEncoder().encode(editedWorker.getPassword())); 
            }else{
                if(editedWorker.getRole().equals("Security Manager")){
                    editedWorker.setPassword(existingWorker.getPassword());
                }
            }
    
            workerRepository.save(editedWorker);
    
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body("ID not found");
        }
    }


}
