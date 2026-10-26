package br.edu.ifma.imobiliaria.controller;
import br.edu.ifma.imobiliaria.dto.*;
import br.edu.ifma.imobiliaria.model.Aluguel;
import br.edu.ifma.imobiliaria.service.AluguelService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/alugueis")
public class AluguelController {
    private final AluguelService service;
    public AluguelController(AluguelService service) { this.service = service; }
    @GetMapping
    public List<AluguelResponse> listar() {
        return service.listar().stream().map(AluguelResponse::from).toList();
    }
    @GetMapping("/{id}")
    public AluguelResponse buscar(@PathVariable Long id) { return AluguelResponse.from(service.buscar(id)); }
    @PostMapping
    public ResponseEntity<AluguelResponse> criar(@Valid @RequestBody AluguelRequest dados) {
        Aluguel criado = service.criar(dados);
        return ResponseEntity.created(URI.create("/api/alugueis/" + criado.getId())).body(AluguelResponse.from(criado));
    }
    @PutMapping("/{id}")
    public AluguelResponse atualizar(@PathVariable Long id, @Valid @RequestBody AluguelRequest dados) {
        return AluguelResponse.from(service.atualizar(id, dados));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
