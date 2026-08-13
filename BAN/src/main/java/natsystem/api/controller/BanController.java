package natsystem.api.controller;

import lombok.AllArgsConstructor;
import natsystem.ban.entity.Ban;
import natsystem.api.service.BanService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@AllArgsConstructor
@RestController
@RequestMapping("/api/ban")
@CrossOrigin(origins = "http://localhost:4200")
public class BanController {
    private final BanService banService;

    @GetMapping("/cp")
    public List<Ban> byCodePostal(@RequestParam int codePostal, @PageableDefault(size = 20) Pageable pageable) {
        return banService.searchByCodePostal(codePostal, pageable);
    }

    @GetMapping("/rue")
    public List<Ban> byRue(@RequestParam String rue, @PageableDefault(size = 20) Pageable pageable) {
        return banService.searchByRue(rue, pageable);
    }

    @GetMapping("/commune")
    public List<Ban> byCommune(@RequestParam String commune, @PageableDefault(size = 20) Pageable pageable) {
        return banService.searchByCommune(commune, pageable);
    }

    @GetMapping("/search")
    public List<Ban> search(@RequestParam(required = false) Integer codePostal,
                            @RequestParam(required = false) String rue,
                            @RequestParam(required = false) String commune,
                            @RequestParam(required = false) Integer numero,
                            @RequestParam(required = false) String rep,
                            @PageableDefault(size = 20) Pageable pageable){
        return banService.search(codePostal, rue, commune, numero, rep, pageable);
    }

    @GetMapping("/free-search")
    public List<Ban> freeSearch(@RequestParam String adresse) { return banService.freeSearch(adresse, 100); }

    @GetMapping("/autocomplete")
    public List<Ban> autocomplete (@RequestParam String adresse) { return banService.freeSearch(adresse,5); }

    @GetMapping("/reverse-search-lucene")
    public Ban reverseSearchLucene (@RequestParam double lat, @RequestParam double lon, @RequestParam Double radiusMeters){
        return banService.reverseSearchLucene(lat, lon, radiusMeters);
    }
}
