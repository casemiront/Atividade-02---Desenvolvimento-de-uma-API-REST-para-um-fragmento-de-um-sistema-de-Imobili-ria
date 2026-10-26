package br.edu.ifma.imobiliaria.service;
import br.edu.ifma.imobiliaria.model.Aluguel;
import br.edu.ifma.imobiliaria.model.Locacao;
import br.edu.ifma.imobiliaria.repository.AluguelRepository;
import br.edu.ifma.imobiliaria.repository.LocacaoRepository;
import br.edu.ifma.imobiliaria.dto.AluguelRequest;
import br.edu.ifma.imobiliaria.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import java.util.List;

@Service
@Transactional
public class AluguelService {
    private final AluguelRepository repository;
    private final LocacaoRepository locacoes;
    public AluguelService(AluguelRepository repository, LocacaoRepository locacoes) {
        this.repository = repository;
        this.locacoes = locacoes;
    }
    @Transactional(readOnly=true)
    public List<Aluguel> listar() { return repository.findAll(Sort.by("id")); }
    @Transactional(readOnly=true)
    public Aluguel buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Aluguel não encontrado: " + id));
    }
    public Aluguel criar(AluguelRequest dados) {
        Aluguel entidade = new Aluguel();
        preencher(entidade, dados);
        return repository.saveAndFlush(entidade);
    }
    public Aluguel atualizar(Long id, AluguelRequest dados) {
        Aluguel entidade = buscar(id);
        preencher(entidade, dados);
        return repository.saveAndFlush(entidade);
    }
    public void excluir(Long id) { repository.delete(buscar(id)); repository.flush(); }
    private void preencher(Aluguel entidade, AluguelRequest dados) {
        Locacao locacao = locacoes.buscarParaAtualizacao(dados.idLocacao())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Locação não encontrada: " + dados.idLocacao()));
        Long ignorar = entidade.getId() == null ? 0L : entidade.getId();
        if (repository.existsByLocacaoIdAndDtVencimentoAndIdNot(dados.idLocacao(), dados.dtVencimento(), ignorar)) {
            throw new RegistroEmUsoException("Já existe aluguel para esta locação e data de vencimento.");
        }
        entidade.setLocacao(locacao);
        entidade.setDtVencimento(dados.dtVencimento());
        entidade.setValorPago(dados.valorPago());
        entidade.setObs(dados.obs());
    }
}
