package natsystem.api.controller;

import lombok.AllArgsConstructor;
import natsystem.dvf.entity.CommuneTarifDTO;
import natsystem.api.service.DvfService;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/dvf")
@CrossOrigin(origins = "http://localhost:4200")
public class DvfController {
    private final DvfService dvfService;

    @GetMapping("/communes/{codeInsee}/tarif")
    public CommuneTarifDTO communesTarif (@PathVariable() String codeInsee){
        return dvfService.tarif(codeInsee);
    }
}
