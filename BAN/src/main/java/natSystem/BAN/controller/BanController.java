package natSystem.BAN.controller;

import lombok.AllArgsConstructor;
import natSystem.BAN.entity.Ban;
import natSystem.BAN.repository.BanRepository;
import natSystem.BAN.service.BanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@AllArgsConstructor
@RestController
@RequestMapping("/api/ban")
public class BanController {
    private final BanService banService;

    @GetMapping("/cp")
    public List<Ban> byCodePostal(@RequestParam int codePostal) {
        return banService.searchByCodePostal(codePostal);
    }

    @GetMapping("/rue")
    public List<Ban> byRue(@RequestParam String rue) {
        return banService.searchByRue(rue);
    }

    @GetMapping("/commune")
    public List<Ban> byCommune(@RequestParam String commune) {
        return banService.searchByCommune(commune);
    }

    @GetMapping("/search")
    public List<Ban> search(@RequestParam(required = false) Integer codePostal, @RequestParam(required = false) String rue, @RequestParam(required = false) String commune){
        return banService.search(codePostal, rue, commune);
    }
}
