package br.edu.ifma.imobiliaria.service;

import br.edu.ifma.imobiliaria.model.*;
import br.edu.ifma.imobiliaria.repository.*;
import br.edu.ifma.imobiliaria.dto.LocacaoRequest;
import br.edu.ifma.imobiliaria.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class LocacaoService {
    private final LocacaoRepository repository;
    private final ClienteService clientes;
    private final ImovelService imoveis;
    private final AluguelRepository alugueis;

    public LocacaoService(LocacaoRepository repository, ClienteService clientes,
            ImovelService imoveis, AluguelRepository alugueis) {
        this.repository = repository;
        this.clientes = clientes;
        this.imoveis = imoveis;
        this.alugueis = alugueis;
    }

    @Transactional(readOnly=true)
    public List<Locacao> listar() { return repository.findAll(Sort.by("id")); }

    @Transactional(readOnly=true)
    public Locacao buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Locação não encontrada: " + id));
    }

    public Locacao buscarParaAtualizacao(Long id) {
        return repository.buscarParaAtualizacao(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Locação não encontrada: " + id));
    }

    public Locacao criar(LocacaoRequest dados) {
        Locacao entidade = new Locacao();
        preencher(entidade, dados);
        return repository.saveAndFlush(entidade);
    }

    public Locacao atualizar(Long id, LocacaoRequest dados) {
        Locacao entidade = buscarParaAtualizacao(id);
        preencher(entidade, dados);
        return repository.saveAndFlush(entidade);
    }

    public void excluir(Long id) {
        Locacao entidade = buscarParaAtualizacao(id);
        if (alugueis.existsByLocacaoId(id)) {
            throw new RegistroEmUsoException("A locação possui aluguéis vinculados. Exclua os aluguéis primeiro.");
        }
        repository.delete(entidade);
        repository.flush();
    }

    private void preencher(Locacao entidade, LocacaoRequest dados) {
        if (!dados.dataFim().isAfter(dados.dataInicio())) {
            throw new RegraNegocioException("A data final deve ser posterior à data inicial.");
        }
        // O bloqueio serializa a verificação e a gravação para o mesmo imóvel.
        Imovel imovel = imoveis.buscarParaAtualizacao(dados.idImovel());
        Cliente inquilino = clientes.buscar(dados.idInquilino());
        if (dados.ativo() && repository.existeConflito(imovel.getId(),
                dados.dataInicio(), dados.dataFim(), entidade.getId())) {
            throw new RegistroEmUsoException("Imóvel indisponível: já existe locação confirmada em período conflitante.");
        }
        long diarias = ChronoUnit.DAYS.between(dados.dataInicio(), dados.dataFim());
        // Preserva o preço contratado em edições de observação/cancelamento.
        boolean mesmoContrato = entidade.getId() != null
            && entidade.getImovel().getId().equals(dados.idImovel())
            && entidade.getDataInicio().equals(dados.dataInicio())
            && entidade.getDataFim().equals(dados.dataFim());
        BigDecimal diaria = mesmoContrato ? entidade.getValorDiariaAplicada() : imovel.getValorDiaria();
        BigDecimal total = diaria.multiply(BigDecimal.valueOf(diarias));
        if (total.compareTo(new BigDecimal("9999999999.99")) > 0) {
            throw new RegraNegocioException("O valor total excede o limite permitido para a locação.");
        }
        entidade.setImovel(imovel);
        entidade.setInquilino(inquilino);
        entidade.setAtivo(dados.ativo());
        entidade.setDataFim(dados.dataFim());
        entidade.setDataInicio(dados.dataInicio());
        entidade.setDiaVencimento(dados.diaVencimento());
        entidade.setPercMulta(dados.percMulta());
        entidade.setValorDiariaAplicada(diaria);
        entidade.setValorAluguel(total);
        entidade.setObs(dados.obs());
    }
}
