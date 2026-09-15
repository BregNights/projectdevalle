# Especificação Funcional — Plataforma de Conexão Direta entre Produtores e Restaurantes

**Escopo geográfico inicial:** Vale do Itajaí e litoral norte de Santa Catarina
**Perfis de usuário:** Produtor Rural / Pescador, Restaurante, Administrador da Plataforma, Operador Logístico (opcional/parceiro)

---

## 1. Requisitos Funcionais (RF)

### 1.1 Cadastro e Verificação de Usuários

- **RF01** — O sistema deve permitir o cadastro de produtores rurais e pescadores, com dados pessoais/empresariais, localização da propriedade ou porto de origem, tipo de produção (agricultura, pesca, pecuária, processamento artesanal) e documentos de comprovação (CPF/CNPJ, DAP/CAF para agricultura familiar, registro de pesca quando aplicável).
- **RF02** — O sistema deve permitir o cadastro de restaurantes, com dados do estabelecimento, CNPJ, endereço de entrega, responsável pelas compras e categoria do estabelecimento (ex.: alta gastronomia, bistrô, rede).
- **RF03** — O sistema deve ter um fluxo de verificação/aprovação de cadastro, no qual um administrador valida os documentos antes de liberar o perfil para operar na plataforma.
- **RF04** — O sistema deve permitir que produtores anexem certificações (orgânico, selo de origem, boas práticas de pesca, etc.) que fiquem visíveis no perfil.
- **RF05** — O sistema deve permitir a edição de perfil, dados bancários (para recebimento) e áreas de entrega atendidas.
- **RF06** — O sistema deve suportar autenticação segura (login/senha e, opcionalmente, login social) e recuperação de acesso.

### 1.2 Catálogo e Oferta de Produtos

- **RF07** — O produtor deve poder cadastrar produtos disponíveis, informando nome, categoria, unidade de medida, quantidade disponível, preço, data/janela de disponibilidade e fotos.
- **RF08** — O sistema deve permitir cadastro de ofertas recorrentes (ex.: "toda quinta-feira, X kg de tilápia") e ofertas pontuais (safra, pescado do dia).
- **RF09** — O sistema deve permitir que o produtor pause, edite ou remova uma oferta em tempo real quando o estoque se esgotar.
- **RF10** — O sistema deve exibir para o restaurante um catálogo pesquisável e filtrável por categoria, região, produtor, certificação, preço e prazo de entrega.
- **RF11** — O sistema deve informar a distância aproximada entre produtor e restaurante para cada item do catálogo.

### 1.3 Demanda Futura (Planejamento)

- **RF12** — O restaurante deve poder publicar uma "necessidade futura", indicando produto, quantidade estimada, janela de tempo (ex.: próximas 2 a 4 semanas) e frequência (única ou recorrente).
- **RF13** — O sistema deve notificar produtores compatíveis (por categoria e região) quando uma nova necessidade futura for publicada.
- **RF14** — O produtor deve poder responder a uma necessidade futura com uma proposta de fornecimento (quantidade, preço, data de entrega).
- **RF15** — O sistema deve consolidar as necessidades futuras publicadas em um painel agregado de demanda por região e por produto, visível aos produtores, para apoiar o planejamento de plantio/produção/pesca.

### 1.4 Pedidos e Negociação

- **RF16** — O restaurante deve poder montar um pedido combinando itens de um ou mais produtores em um único carrinho.
- **RF17** — O sistema deve permitir comparação de ofertas equivalentes (mesmo produto, produtores diferentes) lado a lado, exibindo preço, prazo, avaliação e origem.
- **RF18** — O sistema deve permitir negociação assistida (proposta de preço/quantidade pelo restaurante, aceite ou contraproposta do produtor) antes da confirmação do pedido.
- **RF19** — O sistema deve gerar confirmação de pedido com status rastreável (pendente, confirmado, em preparo, em transporte, entregue, cancelado).
- **RF20** — O sistema deve permitir cancelamento de pedido dentro de regras de prazo, com registro do motivo.
- **RF21** — O sistema deve permitir pedidos recorrentes automatizados (ex.: repetir semanalmente sem necessidade de recriar manualmente).

### 1.5 Pagamentos

- **RF22** — O sistema deve processar pagamentos entre restaurante e produtor por meio de gateway de pagamento integrado (cartão, boleto, PIX).
- **RF23** — O sistema deve suportar retenção de pagamento (escrow) até confirmação de entrega, liberando o valor ao produtor após aceite do restaurante.
- **RF24** — O sistema deve gerar automaticamente nota fiscal ou documento fiscal equivalente, ou orientar o produtor a emiti-la, conforme sua situação tributária.
- **RF25** — O sistema deve calcular e reter a taxa de comissão da plataforma sobre cada transação.
- **RF26** — O sistema deve disponibilizar extrato financeiro para produtores e restaurantes.

### 1.6 Logística e Entrega

- **RF27** — O sistema deve apresentar, para cada pedido, opções de logística: entrega pelo próprio produtor, retirada pelo restaurante, ou transporte por operador logístico parceiro.
- **RF28** — O sistema deve permitir o rastreamento do pedido em trânsito (status e, quando disponível, localização).
- **RF29** — O sistema deve permitir o agrupamento de pedidos de múltiplos produtores próximos em uma mesma rota de entrega (roteirização/consolidação de carga).
- **RF30** — O sistema deve registrar horário de coleta e horário de entrega para fins de avaliação de cumprimento de prazo.

#### 1.6.1 Geolocalização e Roteirização

- **RF30.1** — O sistema deve capturar e armazenar a localização geográfica (latitude/longitude) do produtor no momento do cadastro, vinculada ao endereço da propriedade, barco/porto de desembarque ou ponto de coleta informado.
- **RF30.2** — O sistema deve capturar e armazenar a localização geográfica do restaurante, vinculada ao endereço de entrega cadastrado. O restaurante deve poder cadastrar mais de um endereço de entrega (ex.: filiais) e escolher qual usar em cada pedido.
- **RF30.3** — O sistema deve calcular automaticamente a distância e o tempo estimado de deslocamento entre produtor e restaurante para cada oferta exibida no catálogo, usando a localização de ambos.
- **RF30.4** — O sistema deve exibir em mapa a localização do produtor (aproximada, por questão de privacidade/segurança — ver RN24) e do restaurante ao visualizarem um pedido em conjunto.
- **RF30.5** — Ao montar um pedido com múltiplos produtores, o sistema deve sugerir automaticamente o agrupamento por proximidade geográfica e propor uma rota de coleta otimizada (sequência de paradas) para minimizar distância total percorrida.
- **RF30.6** — O sistema deve permitir que o produtor ou o operador logístico visualize, em um mapa, todas as coletas e entregas do dia, organizadas por rota sugerida.
- **RF30.7** — O sistema deve permitir o ajuste manual da rota sugerida (reordenar paradas) pelo responsável pela entrega, quando necessário.

#### 1.6.2 Rastreamento em Tempo Real

- **RF30.8** — Durante o transporte, o sistema deve atualizar periodicamente a localização do veículo/entregador (quando o app de entrega estiver ativo), exibindo-a em mapa para o restaurante que aguarda o pedido.
- **RF30.9** — O sistema deve calcular e exibir ao restaurante uma estimativa de horário de chegada (ETA), atualizada conforme o deslocamento avança.
- **RF30.10** — O sistema deve notificar automaticamente o restaurante quando a entrega estiver a uma distância/tempo configurável da chegada (ex.: "a 10 minutos de distância").
- **RF30.11** — O sistema deve registrar a localização exata do momento de confirmação de coleta e do momento de confirmação de entrega, para fins de auditoria e resolução de disputas.
- **RF30.12** — Em caso de indisponibilidade de sinal de GPS/internet durante o trajeto (comum em áreas rurais), o sistema deve manter o último ponto conhecido e sinalizar ao restaurante que o rastreamento está temporariamente indisponível, sem interromper o pedido.

### 1.7 Avaliação e Reputação

- **RF31** — O restaurante deve poder avaliar cada entrega recebida, considerando pontualidade, qualidade do produto e conformidade com o combinado.
- **RF32** — O produtor deve poder avaliar o restaurante quanto a cumprimento de pagamento e clareza do pedido.
- **RF33** — O sistema deve calcular uma nota de reputação consolidada por produtor e por restaurante, visível no perfil.
- **RF34** — O sistema deve permitir sinalização de disputa/reclamação sobre um pedido, com abertura de chamado para mediação da plataforma.

### 1.8 Histórico e Inteligência de Preço

- **RF35** — O sistema deve manter histórico de preços por produto, por produtor e por região, disponível em gráfico de evolução.
- **RF36** — O sistema deve sugerir uma faixa de preço de referência ao produtor no momento de cadastrar uma oferta, com base no histórico da categoria e da região.
- **RF37** — O sistema deve permitir que o restaurante compare o preço atual de um item com a média histórica antes de comprar.

### 1.9 Comunicação e Notificações

- **RF38** — O sistema deve enviar notificações (push, e-mail e/ou WhatsApp) sobre novos pedidos, mudanças de status, novas ofertas relevantes e necessidades futuras compatíveis.
- **RF39** — O sistema deve disponibilizar canal de mensagens direto entre produtor e restaurante vinculado a um pedido específico.

### 1.10 Administração da Plataforma

- **RF40** — O administrador deve poder aprovar, suspender ou remover cadastros.
- **RF41** — O administrador deve poder mediar disputas e, quando necessário, reverter ou ajustar transações.
- **RF42** — O administrador deve ter acesso a painel com indicadores da plataforma (volume transacionado, número de produtores/restaurantes ativos, ticket médio, taxa de cancelamento, tempo médio de entrega).
- **RF43** — O administrador deve poder configurar parâmetros globais (percentual de comissão, regiões atendidas, categorias de produto).

---

## 2. Requisitos Não Funcionais (RNF)

### 2.1 Desempenho
- **RNF01** — Páginas de catálogo e busca devem carregar em até 2 segundos sob condição normal de uso.
- **RNF02** — O sistema deve suportar picos de acesso simultâneo (ex.: início de semana, quando restaurantes fazem reposição) sem degradação perceptível.

### 2.2 Disponibilidade e Confiabilidade
- **RNF03** — A plataforma deve ter disponibilidade mínima de 99,5% em horário comercial.
- **RNF04** — O sistema deve ter rotina de backup diário dos dados transacionais e de cadastro.
- **RNF05** — Falhas no processamento de pagamento não podem resultar em pedido confirmado sem cobrança correspondente registrada (consistência transacional).

### 2.3 Segurança
- **RNF06** — Dados sensíveis (dados bancários, documentos pessoais) devem ser armazenados criptografados em repouso e em trânsito (TLS).
- **RNF07** — O sistema deve seguir a LGPD (Lei Geral de Proteção de Dados), incluindo consentimento explícito, direito de exclusão e portabilidade de dados.
- **RNF08** — O sistema deve implementar controle de acesso por perfil (produtor, restaurante, administrador, operador logístico), restringindo funcionalidades por tipo de usuário.
- **RNF09** — Transações financeiras devem ser processadas por gateway de pagamento certificado (PCI-DSS), sem que a plataforma armazene diretamente dados completos de cartão.

### 2.4 Usabilidade
- **RNF10** — A interface deve ser utilizável por produtores com baixo letramento digital, priorizando fluxos simples, ícones e, quando possível, suporte por voz ou WhatsApp para cadastro de ofertas.
- **RNF11** — O aplicativo/plataforma deve funcionar de forma aceitável em conexões de internet instáveis, comuns em áreas rurais e litorâneas (modo offline parcial para cadastro de oferta, sincronizando quando a conexão voltar).
- **RNF12** — A plataforma deve ser responsiva, funcionando em smartphone, tablet e desktop.

### 2.5 Escalabilidade
- **RNF13** — A arquitetura deve permitir expansão para novas regiões de Santa Catarina e, futuramente, outros estados, sem redesenho estrutural.
- **RNF14** — O modelo de dados deve suportar crescimento do catálogo e do volume de pedidos sem perda de desempenho relevante.

### 2.6 Compatibilidade e Integração
- **RNF15** — O sistema deve expor integração via API para eventuais sistemas de gestão de restaurante (ERP, PDV) que queiram automatizar pedidos.
- **RNF16** — O sistema deve ser compatível com principais navegadores e versões recentes de Android/iOS.

### 2.7 Manutenibilidade e Observabilidade
- **RNF17** — O sistema deve registrar logs de transações e erros para auditoria e suporte técnico.
- **RNF18** — O sistema deve permitir atualização de módulos (ex.: logística, pagamentos) sem indisponibilidade total da plataforma.

### 2.8 Conformidade Regulatória Setorial
- **RNF19** — O sistema deve considerar exigências sanitárias e de rastreabilidade aplicáveis a alimentos (ex.: registro de origem, controle de validade), especialmente para pescado e produtos perecíveis.

### 2.9 Geolocalização e Mapas
- **RNF20** — O cálculo de distância e a atualização de localização em rastreamento devem ter precisão suficiente para uso prático em rota (margem de erro aceitável de dezenas de metros em área urbana e de algumas centenas de metros em área rural, onde a triangulação de sinal é mais fraca).
- **RNF21** — A atualização de localização durante o transporte deve ocorrer em intervalo que equilibre precisão e consumo de dados/bateria do dispositivo do entregador (ex.: a cada 30–60 segundos, ajustável).
- **RNF22** — O provedor de mapas/geocodificação utilizado deve ter cobertura adequada de estradas rurais e vicinais da região do Vale do Itajaí e litoral norte de SC, não apenas de vias urbanas principais.
- **RNF23** — Dados de localização devem ser tratados como dado pessoal sensível conforme a LGPD, com acesso restrito a quem participa diretamente do pedido em curso.

---

## 3. Regras de Negócio (RN)

### 3.1 Cadastro e Elegibilidade
- **RN01** — Somente produtores e restaurantes com cadastro aprovado pela administração podem publicar ofertas, publicar necessidades futuras ou fechar pedidos.
- **RN02** — O produtor deve estar localizado dentro da área de cobertura definida (inicialmente Vale do Itajaí e litoral norte de SC) para ser listado.
- **RN03** — Certificações exibidas no perfil (orgânico, selo de origem, etc.) só podem ser mantidas visíveis mediante comprovação documental válida; a plataforma pode suspender a exibição do selo se a validade expirar.

### 3.2 Ofertas e Preços
- **RN04** — Toda oferta publicada deve ter quantidade disponível maior que zero; ao atingir zero, a oferta é automaticamente marcada como esgotada.
- **RN05** — O preço da oferta é definido livremente pelo produtor; a faixa de referência do sistema é apenas sugestiva, não impositiva.
- **RN06** — Produtos perecíveis (pescado, hortifruti fresco) devem ter data de disponibilidade/validade obrigatória; ofertas vencidas são removidas automaticamente do catálogo.

### 3.3 Necessidades Futuras
- **RN07** — Uma necessidade futura publicada pelo restaurante não obriga compra; é uma sinalização de demanda, não um contrato.
- **RN08** — Quando um produtor responde a uma necessidade futura com proposta, o restaurante deve confirmar explicitamente para que se torne pedido vinculante.

### 3.4 Pedidos e Cancelamento
- **RN09** — Um pedido só é considerado confirmado após pagamento processado (ou retido em escrow) e aceite do produtor quanto à quantidade e ao prazo.
- **RN10** — Cancelamento pelo restaurante após o produtor já ter iniciado a colheita/pesca/preparo do pedido pode gerar cobrança de multa proporcional, definida em política de cancelamento.
- **RN11** — Cancelamento pelo produtor por indisponibilidade (ex.: quebra de safra, mau tempo impedindo pesca) não gera multa ao produtor, mas impacta negativamente sua taxa de cumprimento no perfil.
- **RN12** — Pedidos recorrentes automatizados podem ser suspensos pelo restaurante com aviso mínimo definido (ex.: 48h antes da próxima execução).

### 3.5 Pagamento e Comissão
- **RN13** — A plataforma retém uma comissão percentual sobre cada transação concluída, definida e ajustável pela administração.
- **RN14** — O valor pago pelo restaurante fica retido (escrow) até a confirmação de recebimento; caso o restaurante não confirme nem abra disputa dentro do prazo estipulado, o recebimento é considerado automaticamente confirmado e o valor liberado ao produtor.
- **RN15** — Em caso de disputa, o valor permanece retido até resolução pela mediação da plataforma.

### 3.6 Logística
- **RN16** — Quando a entrega é feita por operador logístico parceiro, o custo do frete é exibido separadamente do preço do produto, sendo transparente para ambas as partes.
- **RN17** — A responsabilidade pela integridade do produto durante o transporte é do agente que executa a entrega (produtor, restaurante ou operador logístico), conforme opção escolhida no pedido.

### 3.6.1 Localização e Rastreamento
- **RN24** — A localização exata do produtor não é exibida publicamente no catálogo; apenas a distância aproximada e o município/região são visíveis antes da compra. A localização precisa só é compartilhada com o restaurante após a confirmação do pedido, e apenas enquanto o pedido estiver ativo.
- **RN25** — O histórico de localização de um pedido concluído (pontos de coleta e entrega) é mantido pelo prazo mínimo definido para fins de auditoria e resolução de disputas, e não pode ser usado para outra finalidade sem consentimento.
- **RN26** — O ETA (horário estimado de chegada) exibido é uma estimativa; atrasos decorrentes de fatores fora do controle do produtor/entregador (condição de estrada, clima) não geram penalização automática na nota de pontualidade, ficando sujeitos a análise em caso de disputa.
- **RN27** — Quando o rastreamento em tempo real estiver indisponível por falha de sinal, o prazo de entrega continua sendo contado a partir dos horários registrados de coleta e confirmação de entrega, não pela ausência de sinal.

### 3.7 Avaliação e Reputação
- **RN18** — Somente pedidos efetivamente concluídos (entregues e confirmados) geram direito a avaliação.
- **RN19** — Um produtor ou restaurante com nota de reputação abaixo de um limite mínimo definido pela administração pode ter o perfil temporariamente restringido, até revisão.
- **RN20** — Avaliações só podem ser contestadas por meio do canal de disputa, não podem ser removidas unilateralmente por quem a recebeu.

### 3.8 Disputas
- **RN21** — Toda disputa aberta deve ser respondida pela outra parte dentro de um prazo definido (ex.: 48h), sob risco de decisão automática favorável à parte reclamante, conforme política da plataforma.
- **RN22** — A administração tem poder de decisão final sobre disputas não resolvidas entre as partes.

### 3.9 Dados e Transparência de Origem
- **RN23** — Todo pedido entregue deve manter registro de rastreabilidade (produtor de origem, data de colheita/captura, lote quando aplicável), para uso do restaurante na comunicação ao cliente final.

---

*Documento estruturado como ponto de partida para elicitação com stakeholders, elaboração de backlog e especificação técnica detalhada (casos de uso, wireframes e modelo de dados).*
