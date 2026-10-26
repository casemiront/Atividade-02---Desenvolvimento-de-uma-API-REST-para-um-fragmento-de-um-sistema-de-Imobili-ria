# API Imobiliária - Atividade 2

Java 21, Spring Boot, Spring Data JPA, Bean Validation, H2 e Flyway.

## Execução

Execute `ImobiliariaApplication` ou use `mvnw.cmd spring-boot:run` no Windows.
Testes: `mvnw.cmd test`. Endereço padrão: `http://localhost:8080`.

## Recursos

GET, POST, PUT e DELETE em `/api/clientes`, `/api/imoveis`, `/api/locacoes` e `/api/alugueis`.
As consultas individuais e alterações usam `/{id}`.

- Paginação: `/api/imoveis/paginados?page=0&size=5`.
- Query by Example: `/api/imoveis/pesquisa?tipo=apar&cidade=luí&quartos=2`.
- Ordenação: `ordenar=id|tipoImovel|cidade|dormitorios|valorDiaria` e `direcao=asc|desc`.
- Tipo/cidade: correspondência parcial, sem diferenciar maiúsculas/minúsculas; acentos são significativos.
- Quartos corresponde a dormitorios; filtros informados são combinados por E.

## Regras

- `ativo=true` representa locação confirmada; `false`, cancelada/inativa.
- Somente confirmadas ocupam o imóvel. Criar ou atualizar uma confirmação exige disponibilidade; sobreposição retorna 409.
- Intervalo `[dataInicio, dataFim)`: a data final é saída, deve ser posterior à inicial e pode coincidir com a entrada da próxima locação.
- Total: `valorDiaria * quantidadeDiarias`, com BigDecimal, devolvido no campo `valorAluguel`. O cliente não envia o total.
- `valorAluguelSug` foi preservado do modelo anterior e não é usado no cálculo por diárias.
- Edições sem mudança de imóvel/período preservam a diária contratada. Mudança de imóvel/período usa a diária atual.
- Não é permitido excluir imóvel com locação confirmada ativa ou futura. Usa-se a data atual de America/Sao_Paulo; fim exclusivo.
- Histórico de locações encerradas ou canceladas também bloqueia exclusão física enquanto houver vínculos. Não há cascata: remover aluguéis, locações, imóvel e cliente, nessa ordem.
- Aluguel aceita pagamento zero, não aceita valor negativo e não pode repetir locação + vencimento.
- Erros de entrada retornam 400; recurso ausente, 404; conflitos e vínculos, 409.

V1 preserva o esquema original, V2 adiciona os novos campos e aluguéis, V3 inclui 12 imóveis e 2 clientes fictícios. O banco de demonstração é próprio, em `dados/imobiliaria-atividade02.mv.db`. Não reutilize diretamente contratos mensais da atividade 1: converter valores para diárias exige uma política financeira própria.
