package natsystem.api.controller;

import lombok.AllArgsConstructor;
import natsystem.api.service.GeoJsonService;
import natsystem.geojson.entity.CommunesGeoJson;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/geoJson")
@CrossOrigin(origins = "http://localhost:4200")
public class GeoJsonController {
    private final GeoJsonService geoJsonService;

    @GetMapping("/codeInsee")
    public CommunesGeoJson byCodeInsee(@RequestParam String codeInsee) {
        return geoJsonService.findByCodeInsee(codeInsee);
    }

}
