package it.unisalento.bric48.backend.restcontrollers;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import it.unisalento.bric48.backend.domain.Beacon;
import it.unisalento.bric48.backend.dto.BeaconDTO;
import it.unisalento.bric48.backend.repositories.BeaconRepository;

@RestController
@CrossOrigin
@RequestMapping("/beacon")
public class BeaconRestController {

    @Autowired
    BeaconRepository beaconRepository;

    // Add a new beacon
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value="/add", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public BeaconDTO addBeacon(@RequestBody BeaconDTO beaconDTO) {

        Beacon newBeacon = new Beacon();
        newBeacon.setMac(beaconDTO.getMac());
        newBeacon.setMserial(beaconDTO.getMserial());

        newBeacon = beaconRepository.save(newBeacon);

        beaconDTO.setId(newBeacon.getId());

        return beaconDTO;
    }

    //Get beacons by mserial
    @RequestMapping(value="/find/{mserial}", method= RequestMethod.GET)
    public List<BeaconDTO> getBeaconByMserial(@PathVariable("mserial") String mserial) {

        List<BeaconDTO> beacons = new ArrayList<>();

        for(Beacon beacon : beaconRepository.findByMserial(mserial)) {
            BeaconDTO beaconDTO = new BeaconDTO();
            beaconDTO.setId(beacon.getId());
            beaconDTO.setMac(beacon.getMac());
            beaconDTO.setMserial(beacon.getMserial());

            beacons.add(beaconDTO);
        }

        return beacons;
    }
    

}

