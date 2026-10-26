package br.edu.ifma.imobiliaria.controller;


import br.edu.ifma.imobiliaria.model.Imovel;
import br.edu.ifma.imobiliaria.dto.*;
import br.edu.ifma.imobiliaria.service.ImovelService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/imoveis")
public class ImovelController {
    private final ImovelService service;
    public ImovelController(ImovelService service) { this.service = service; }

    @GetMapping
    public List<Imovel> listar() { return service.listar(); }

    @GetMapping({"/paginados", "/pesquisa"})
    public PaginaResponse<Imovel> pesquisar(
            @RequestParam(required=false) String tipo,
            @RequestParam(required=false) String cidade,
            @RequestParam(required=false) Integer quartos,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="5") int size,
            @RequestParam(defaultValue="id") String ordenar,
            @RequestParam(defaultValue="asc") String direcao) {
        return PaginaResponse.from(service.pesquisar(tipo, cidade, quartos, page, size, ordenar, direcao));
    }

    @GetMapping("/{id}")
    public Imovel buscar(@PathVariable Long id) { return service.buscar(id); }

    @PostMapping
    public ResponseEntity<Imovel> criar(@Valid @RequestBody ImovelRequest dados) {
        Imovel criado = service.criar(dados);
        return ResponseEntity.created(URI.create("/api/imoveis/" + criado.getId())).body(criado);
    }

    @PutMapping("/{id}")
    public Imovel atualizar(@PathVariable Long id, @Valid @RequestBody ImovelRequest dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
