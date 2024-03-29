package it.unisalento.bric48.backend.restcontrollers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import it.unisalento.bric48.backend.domain.Headphones;
import it.unisalento.bric48.backend.dto.HeadphonesDTO;
import it.unisalento.bric48.backend.repositories.HeadphonesRepository;

@RestController
@CrossOrigin
@RequestMapping("/headphones")
public class HeadphonesRestController {

    @Autowired
    HeadphonesRepository headphonesRepository;

    // Add a new headphones
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="/add", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public HeadphonesDTO addHeadphones(@RequestBody HeadphonesDTO headphonesDTO) {

        Headphones newHeadphones = new Headphones();
        newHeadphones.setSerial(headphonesDTO.getSerial());
        newHeadphones.setIsAssociated(headphonesDTO.getIsAssociated());

        newHeadphones = headphonesRepository.save(newHeadphones);

        headphonesDTO.setId(newHeadphones.getId());

        return headphonesDTO;
    }

    //Get all headphones
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="/getAll", method= RequestMethod.GET)
    public List<HeadphonesDTO> getAllHeadphones() {

        List<HeadphonesDTO> headphones = new ArrayList<>();

        for(Headphones h : headphonesRepository.findAll()) {

            HeadphonesDTO headphonesDTO = new HeadphonesDTO();

            headphonesDTO.setId(h.getId());
            headphonesDTO.setSerial(h.getSerial());
            headphonesDTO.setIsAssociated(h.getIsAssociated());

            headphones.add(headphonesDTO);
        }

        return headphones;
    }

    // Delete headphones by serial
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/delete/{serial}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteHeadphonesBySerial(@PathVariable("serial") String serial) {
  
        headphonesRepository.deleteBySerial(serial);

        // Verifica se l'entità è stata eliminata con successo
        Optional<Headphones> deletedEntity = headphonesRepository.findBySerial(serial);
        if (!deletedEntity.isEmpty()) {
            return ResponseEntity.badRequest().body("ID not found");
        }
        return ResponseEntity.ok().build();
    }

}
