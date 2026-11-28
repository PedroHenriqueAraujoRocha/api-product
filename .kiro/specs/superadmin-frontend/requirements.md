# Requirements Document

## Introduction

O Easy Eats é uma plataforma SaaS para estabelecimentos de comida. Este documento especifica o **Painel Super Admin**, um front-end web interno usado pela equipe Easy Eats para administrar clientes (estabelecimentos), produtos/planos, categorias de estabelecimento e usuários do painel.

O painel é um repositório novo e independente (`C:\Users\pedro_ha_rocha\Documents\superadmin-frontend`, remoto `https://github.com/PedroHenriqueAraujoRocha/superadmin-frontend.git`), construído com Angular (versão estável atual, standalone components, signals e novo control flow), Angular Material (Material 3) e TypeScript. O painel consome microsserviços Spring Boot mantidos por pessoas diferentes:

- **Serviço_Product** (repositório `api-product`): categorias e produtos/planos.
- **Serviço_Client** (repositório `EasyEats-Project/client`): clientes.
- **Serviço_User** (repositório `joao-vitor1007/user`): autenticação JWT e usuários.
- **Serviço_Base** (repositório `joao-vitor1007/base`): esqueleto sem endpoints; fora do escopo de integração.

Os requisitos estão escritos no padrão EARS. Os títulos estruturais do documento (Requirements Document, Introduction, Glossary, Requirements, Acceptance Criteria) e as palavras-chave do padrão (WHEN, WHILE, IF, THEN, WHERE, THE, SHALL) foram mantidos em inglês para preservar a estrutura verificável; o restante do texto está em português do Brasil. Os contratos de API consumidos estão resumidos no **Anexo A** e são a referência para os modelos de dados do painel.

## Glossary

- **Painel**: a aplicação front-end Super Admin descrita neste documento, executada no navegador.
- **Usuário_Admin**: pessoa da equipe Easy Eats autenticada no Painel.
- **Visitante**: pessoa que acessa o Painel sem Sessão válida.
- **Sessão**: estado de autenticação do Painel composto pelo Token_JWT e pelo username do Usuário_Admin.
- **Token_JWT**: texto puro retornado pelo endpoint `POST /auth/login` do Serviço_User.
- **Guarda_De_Rotas**: mecanismo do Painel que decide se uma rota pode ser exibida conforme o estado da Sessão.
- **Camada_API**: conjunto de serviços HTTP do Painel, um por microsserviço, responsável por montar requisições e converter respostas nos modelos do Painel.
- **Normalizador_De_Erros**: componente do Painel que converte qualquer resposta de erro HTTP em um Erro_Normalizado.
- **Erro_Normalizado**: estrutura única de erro do Painel contendo status HTTP, título, mensagem para o usuário e um mapa opcional de erros por campo.
- **ProblemDetail**: corpo de erro no formato RFC 9457 (`application/problem+json`) com `title`, `status`, `detail` e `errors` opcional (mapa campo → mensagem), usado pelo Serviço_Client.
- **Erro_Padrão_Spring**: corpo de erro padrão do Spring Boot com `timestamp`, `status`, `error` e `path` (e `message` apenas quando habilitado no servidor), usado pelo Serviço_Product.
- **Shell**: estrutura de layout do Painel composta por Sidebar, Topbar e área de conteúdo.
- **Sidebar**: menu lateral de navegação do Shell.
- **Topbar**: barra superior do Shell.
- **Tema**: esquema de cores do Painel, com os valores `claro` e `escuro`.
- **Viewport_Móvel**: janela do navegador com largura inferior a 960 px.
- **Notificação**: mensagem temporária de feedback (snackbar) exibida pelo Painel.
- **Diálogo_De_Confirmação**: janela modal que exige confirmação explícita antes de uma ação destrutiva.
- **Skeleton**: marcador visual de carregamento com o formato aproximado do conteúdo esperado.
- **Estado_Vazio**: conteúdo exibido quando uma listagem não possui registros a mostrar.
- **Inativação**: exclusão lógica executada por `DELETE`, que altera o status do registro para `INATIVO` sem removê-lo.
- **Cliente**: empresa (estabelecimento) cliente da Easy Eats, gerenciada pelo Serviço_Client.
- **Categoria**: tipo de estabelecimento (ex.: Pizzaria, Food Truck, Sushi), gerenciado pelo Serviço_Product.
- **Produto**: plano Easy Eats oferecido para uma Categoria, gerenciado pelo Serviço_Product.
- **Plano**: identificador de Produto, com os valores `BASIC`, `PLUS` ou `PREMIUM`.
- **CNPJ**: identificador de empresa com 14 caracteres (12 alfanuméricos seguidos de 2 dígitos verificadores numéricos), aceito com ou sem a máscara `XX.XXX.XXX/XXXX-XX`.
- **CNPJ_Normalizado**: CNPJ sem caracteres de máscara e com letras em maiúsculas.
- **Validador_CNPJ**: componente do Painel que verifica formato e dígitos verificadores de um CNPJ.
- **Formatador_Monetário**: componente do Painel que converte valores numéricos em texto no formato BRL (ex.: `R$ 1.234,56`) e converte texto digitado em valor numérico.
- **Configuração_De_Ambiente**: conjunto de parâmetros do Painel que define a URL base de cada microsserviço.
- **Proxy_Dev**: proxy do servidor de desenvolvimento do Angular que encaminha requisições por prefixo de serviço.
- **Imagem_Do_Painel**: imagem de contêiner que contém o build de produção do Painel servido por nginx.
- **Proxy_Reverso**: configuração nginx da Imagem_Do_Painel que serve a SPA e encaminha requisições por prefixo de serviço.
- **Dashboard**: página inicial do Painel com indicadores e atalhos.
- **Indicador**: cartão do Dashboard que exibe um número derivado das listagens existentes.

## Requirements

### Requisito 1: Login

**User Story:** Como Usuário_Admin, quero entrar no Painel com meu username e senha, para que somente pessoas cadastradas acessem a administração interna.

#### Acceptance Criteria

1. THE Painel SHALL exibir uma tela de login com os campos username e senha, ambos obrigatórios, e um botão "Entrar".
2. WHEN o Visitante envia o formulário de login com username ou senha vazios, THE Painel SHALL exibir mensagem de campo obrigatório junto ao campo vazio e manter o formulário sem enviar requisição.
3. WHEN o Visitante envia o formulário de login válido, THE Camada_API SHALL enviar `POST /auth/login` ao Serviço_User com `username` e `password` como parâmetros de query e sem corpo JSON.
4. WHEN o Serviço_User responde ao login com status 200, THE Painel SHALL armazenar o texto da resposta como Token_JWT junto com o username informado e redirecionar para a rota originalmente solicitada ou, na ausência dela, para o Dashboard.
5. IF o Serviço_User responde ao login com status 401 ou 403, THEN THE Painel SHALL exibir a mensagem "Usuário ou senha inválidos" e manter o username preenchido com o campo senha limpo.
6. IF o Serviço_User está inacessível durante o login, THEN THE Painel SHALL exibir a mensagem "Serviço de autenticação indisponível. Tente novamente em instantes."
7. WHILE a requisição de login está em andamento, THE Painel SHALL desabilitar o botão "Entrar" e exibir um indicador de progresso.
8. THE Painel SHALL omitir o valor da senha de qualquer registro de log, mensagem de erro ou armazenamento local.
9. THE Painel SHALL oferecer um controle para mostrar e ocultar o texto digitado no campo senha.

### Requisito 2: Sessão, Guarda_De_Rotas e logout

**User Story:** Como Usuário_Admin, quero que minha sessão seja protegida e encerrada de forma previsível, para que páginas administrativas fiquem inacessíveis sem autenticação.

#### Acceptance Criteria

1. WHEN o Visitante acessa qualquer rota do Painel diferente da tela de login, THE Guarda_De_Rotas SHALL redirecionar para a tela de login preservando a rota solicitada para redirecionamento posterior.
2. WHEN o Usuário_Admin com Sessão válida acessa a tela de login, THE Guarda_De_Rotas SHALL redirecionar para o Dashboard.
3. WHEN o Painel é recarregado no navegador com uma Sessão válida armazenada, THE Painel SHALL restaurar a Sessão sem exigir novo login.
4. WHERE o Token_JWT contém a declaração `exp`, THE Painel SHALL tratar a Sessão como expirada a partir do instante indicado em `exp`.
5. WHEN a Sessão expira, THE Painel SHALL remover a Sessão armazenada, redirecionar para a tela de login e exibir a mensagem "Sua sessão expirou. Entre novamente."
6. WHILE existe Sessão válida, THE Camada_API SHALL incluir o cabeçalho `Authorization: Bearer <Token_JWT>` nas requisições ao Serviço_User que exigem autenticação.
7. IF uma requisição autenticada recebe status 401, THEN THE Painel SHALL remover a Sessão armazenada, redirecionar para a tela de login e exibir a mensagem "Sua sessão expirou. Entre novamente."
8. WHEN o Usuário_Admin aciona "Sair" na Topbar, THE Painel SHALL remover a Sessão armazenada e redirecionar para a tela de login.
9. WHEN o login é concluído, THE Camada_API SHALL consultar `GET /usuarios/listar-por-nome/{username}` com o Token_JWT para confirmar a validade da Sessão, descartando o campo `password` da resposta.

### Requisito 3: Shell de layout e navegação

**User Story:** Como Usuário_Admin, quero um layout com menu lateral e barra superior, para que eu navegue entre as áreas do Painel de forma consistente.

#### Acceptance Criteria

1. WHILE existe Sessão válida, THE Shell SHALL exibir a Sidebar com os itens Dashboard, Clientes, Produtos/Planos, Categorias e Usuários, cada um com ícone e rótulo textual.
2. THE Sidebar SHALL destacar visualmente o item correspondente à rota ativa.
3. WHEN o Usuário_Admin aciona o controle de recolher a Sidebar em viewport com largura de 960 px ou mais, THE Shell SHALL alternar a Sidebar entre o modo expandido (ícone e rótulo) e o modo recolhido (somente ícone com tooltip do rótulo).
4. THE Painel SHALL persistir no navegador a preferência de Sidebar expandida ou recolhida e reaplicar a preferência no próximo acesso.
5. WHILE o Painel é exibido em Viewport_Móvel, THE Shell SHALL apresentar a Sidebar como drawer sobreposto, fechado por padrão e aberto por um botão de menu na Topbar.
6. WHEN o Usuário_Admin seleciona um item de navegação em Viewport_Móvel, THE Shell SHALL fechar o drawer após a navegação.
7. THE Topbar SHALL exibir o título da página atual, o username do Usuário_Admin, o controle de alternância de Tema e a ação "Sair".
8. WHEN o Usuário_Admin acessa uma rota inexistente, THE Painel SHALL exibir uma página "Página não encontrada" com link para o Dashboard.

### Requisito 4: Tema claro e escuro

**User Story:** Como Usuário_Admin, quero alternar entre tema claro e escuro, para que eu use o Painel com conforto em diferentes ambientes.

#### Acceptance Criteria

1. WHEN o Usuário_Admin aciona o controle de alternância de Tema, THE Painel SHALL aplicar o outro Tema a todas as telas sem recarregar a página.
2. THE Painel SHALL persistir no navegador o Tema escolhido e reaplicar o Tema escolhido no próximo acesso, inclusive na tela de login.
3. WHILE nenhuma preferência de Tema está persistida, THE Painel SHALL aplicar o Tema correspondente à preferência `prefers-color-scheme` do sistema operacional.
4. THE Painel SHALL manter razão de contraste mínima de 4,5:1 para texto normal e 3:1 para texto grande e componentes de interface em ambos os Temas.

### Requisito 5: Dashboard

**User Story:** Como Usuário_Admin, quero uma visão geral com números da operação e atalhos, para que eu acompanhe a base de clientes e o catálogo sem abrir cada tela.

#### Acceptance Criteria

1. THE Dashboard SHALL exibir os Indicadores: total de Clientes ativos, total de Clientes inativos, quantidade de Produtos ativos por Plano (BASIC, PLUS e PREMIUM) e total de Categorias ativas.
2. THE Dashboard SHALL obter o total de Clientes ativos e o total de Clientes inativos a partir do campo `totalElements` de `GET /client` filtrado por `status=ATIVO` e `status=INATIVO`, respectivamente.
3. THE Dashboard SHALL calcular a quantidade de Produtos ativos por Plano e o total de Categorias ativas a partir das respostas de `GET /products` e `GET /categories`.
4. WHILE os dados de um Indicador estão sendo carregados, THE Dashboard SHALL exibir um Skeleton no lugar do valor desse Indicador.
5. IF a requisição de um Indicador falha, THEN THE Dashboard SHALL exibir no cartão desse Indicador uma mensagem de indisponibilidade com a ação "Tentar novamente", mantendo os demais Indicadores funcionais.
6. THE Dashboard SHALL exibir atalhos para "Novo cliente", "Novo produto" e "Nova categoria" que abrem os respectivos formulários de cadastro.
7. WHEN o Usuário_Admin aciona a ação "Atualizar" do Dashboard, THE Dashboard SHALL recarregar todos os Indicadores e exibir o horário da última atualização.
8. WHEN o Usuário_Admin aciona um Indicador de Clientes, THE Painel SHALL abrir a listagem de Clientes já filtrada pelo status correspondente.

### Requisito 6: Listagem de Clientes

**User Story:** Como Usuário_Admin, quero listar e filtrar clientes com paginação, para que eu encontre rapidamente um estabelecimento.

#### Acceptance Criteria

1. THE Painel SHALL exibir a listagem de Clientes com as colunas razão social, nome fantasia, CNPJ com máscara, e-mail, status e ações.
2. THE Camada_API SHALL obter a listagem por `GET /client` com os parâmetros `page`, `size` e, quando preenchidos, `nome` e `status`.
3. THE Painel SHALL oferecer tamanhos de página 10, 20, 50 e 100, com 20 como valor padrão.
4. WHEN o Usuário_Admin altera o texto do filtro por nome, THE Painel SHALL refazer a consulta 300 ms após a última digitação, retornando à primeira página.
5. WHEN o Usuário_Admin altera o filtro de status entre Todos, ATIVO e INATIVO, THE Painel SHALL refazer a consulta retornando à primeira página.
6. THE Painel SHALL refletir página, tamanho de página e filtros nos parâmetros de query da URL, de forma que recarregar a página ou compartilhar a URL reproduza a mesma consulta.
7. THE Painel SHALL exibir o total de registros e os controles de paginação com base em `totalElements` e `totalPages` da resposta.
8. WHILE a consulta de Clientes está em andamento, THE Painel SHALL exibir linhas em Skeleton no lugar dos registros.
9. WHEN a consulta retorna zero registros sem filtros ativos, THE Painel SHALL exibir um Estado_Vazio com a ação "Cadastrar cliente".
10. WHEN a consulta retorna zero registros com filtros ativos, THE Painel SHALL exibir um Estado_Vazio informando que nenhum Cliente corresponde aos filtros, com a ação "Limpar filtros".
11. THE Painel SHALL exibir o status de cada Cliente como etiqueta com texto e cor distintos para ATIVO e INATIVO.

### Requisito 7: Cadastro e edição de Clientes

**User Story:** Como Usuário_Admin, quero cadastrar e editar clientes com validação imediata, para que os dados enviados ao Serviço_Client sejam aceitos na primeira tentativa.

#### Acceptance Criteria

1. THE Painel SHALL exibir no formulário de Cliente os campos razão social (obrigatório, máximo 150 caracteres), nome fantasia (opcional, máximo 150 caracteres), CNPJ (obrigatório), e-mail (obrigatório, formato de e-mail, máximo 150 caracteres) e telefone (opcional, máximo 20 caracteres).
2. WHEN o Usuário_Admin sai de um campo do formulário de Cliente com valor inválido, THE Painel SHALL exibir a mensagem de validação abaixo do campo.
3. WHEN o Usuário_Admin digita no campo CNPJ, THE Painel SHALL aplicar a máscara `XX.XXX.XXX/XXXX-XX`, aceitando letras e dígitos nas 12 primeiras posições e somente dígitos nas 2 últimas, convertendo letras para maiúsculas.
4. IF o CNPJ informado não possui 14 caracteres válidos ou possui dígitos verificadores incorretos, THEN THE Validador_CNPJ SHALL marcar o campo como inválido com a mensagem "CNPJ inválido".
5. IF o telefone informado contém caracteres diferentes de dígitos, espaços, `+`, `(`, `)` e `-`, THEN THE Painel SHALL marcar o campo telefone como inválido com a mensagem "Use apenas dígitos, espaços e + ( ) -".
6. WHEN o Usuário_Admin envia um formulário de cadastro de Cliente válido, THE Camada_API SHALL enviar `POST /client` com o corpo ClientRequest contendo o CNPJ_Normalizado.
7. WHEN o Serviço_Client responde ao cadastro com status 201, THE Painel SHALL exibir a Notificação "Cliente cadastrado com sucesso" e abrir o detalhe do Cliente criado.
8. IF o Serviço_Client responde ao cadastro com status 409, THEN THE Painel SHALL marcar o campo CNPJ com a mensagem "Já existe um cliente com este CNPJ".
9. WHILE o formulário de Cliente está em modo edição, THE Painel SHALL exibir o CNPJ com máscara em campo somente leitura.
10. WHEN o Usuário_Admin envia um formulário de edição de Cliente válido, THE Camada_API SHALL enviar `PUT /client/{id}` com o corpo ClientUpdateRequest, sem o campo CNPJ.
11. WHEN o Serviço_Client responde à edição com status 200, THE Painel SHALL exibir a Notificação "Cliente atualizado com sucesso" e abrir o detalhe do Cliente.
12. IF o Serviço_Client responde ao cadastro ou à edição com status 400 contendo `errors`, THEN THE Painel SHALL exibir cada mensagem de `errors` junto ao campo correspondente do formulário.
13. WHILE a requisição de salvamento está em andamento, THE Painel SHALL desabilitar o botão "Salvar" para impedir envios duplicados.
14. FOR ALL CNPJs válidos, aplicar a máscara e em seguida normalizar SHALL produzir o mesmo CNPJ_Normalizado de origem (propriedade de ida e volta).
15. FOR ALL CNPJ_Normalizados com dígitos verificadores calculados pelo algoritmo oficial (módulo 11), THE Validador_CNPJ SHALL classificar o CNPJ como válido, e para todo CNPJ com um dígito verificador alterado THE Validador_CNPJ SHALL classificar o CNPJ como inválido.

### Requisito 8: Detalhe e Inativação de Clientes

**User Story:** Como Usuário_Admin, quero ver os dados completos de um cliente e inativá-lo com segurança, para que eu encerre o relacionamento sem perder o histórico.

#### Acceptance Criteria

1. THE Painel SHALL exibir no detalhe do Cliente razão social, nome fantasia, CNPJ com máscara, e-mail, telefone, status, data de cadastro e data de atualização.
2. THE Painel SHALL exibir `dataCadastro` e `dataAtualizacao` no fuso horário do navegador no formato `dd/MM/yyyy HH:mm`.
3. IF `GET /client/{id}` responde com status 404, THEN THE Painel SHALL exibir a mensagem "Cliente não encontrado" com link para a listagem de Clientes.
4. WHILE o Cliente exibido possui status ATIVO, THE Painel SHALL oferecer as ações "Editar" e "Inativar" no detalhe e na listagem.
5. WHILE o Cliente exibido possui status INATIVO, THE Painel SHALL ocultar a ação "Inativar" e exibir a informação de que o Serviço_Client não oferece reativação.
6. WHEN o Usuário_Admin aciona "Inativar" em um Cliente, THE Painel SHALL exibir um Diálogo_De_Confirmação com a razão social do Cliente e os botões "Cancelar" e "Inativar".
7. WHEN o Usuário_Admin confirma o Diálogo_De_Confirmação de Inativação de Cliente, THE Camada_API SHALL enviar `DELETE /client/{id}`.
8. WHEN o Serviço_Client responde à Inativação com status 204, THE Painel SHALL exibir a Notificação "Cliente inativado" e atualizar o status exibido para INATIVO.

### Requisito 9: Categorias

**User Story:** Como Usuário_Admin, quero listar, cadastrar, editar e inativar categorias de estabelecimento, para que os planos sejam organizados por tipo de negócio.

#### Acceptance Criteria

1. THE Painel SHALL exibir a listagem de Categorias obtida por `GET /categories` com as colunas nome, descrição, status, data de atualização e ações.
2. THE Painel SHALL oferecer na listagem de Categorias busca textual por nome, filtro por status e paginação executados no navegador, já que o Serviço_Product retorna a lista completa.
3. THE Painel SHALL exibir no formulário de Categoria os campos nome (obrigatório) e descrição (opcional), e no modo edição também o campo status (ATIVO ou INATIVO).
4. WHEN o Usuário_Admin envia um formulário de cadastro de Categoria válido, THE Camada_API SHALL enviar `POST /categories` com o corpo CategoryDto sem os campos somente leitura.
5. WHEN o Usuário_Admin envia um formulário de edição de Categoria válido, THE Camada_API SHALL enviar `PUT /categories/{id}` com o corpo CategoryDto sem os campos somente leitura.
6. IF o Serviço_Product responde ao cadastro ou à edição de Categoria com status 409, THEN THE Painel SHALL marcar o campo nome com a mensagem "Já existe uma categoria com este nome".
7. WHEN o Usuário_Admin aciona "Inativar" em uma Categoria ATIVA, THE Painel SHALL exibir um Diálogo_De_Confirmação com o nome da Categoria.
8. WHEN o Usuário_Admin confirma a Inativação de Categoria, THE Camada_API SHALL enviar `DELETE /categories/{id}`.
9. WHEN o Serviço_Product responde à Inativação de Categoria com status 204, THE Painel SHALL exibir a Notificação "Categoria inativada" e atualizar o status exibido para INATIVO.
10. IF o Serviço_Product responde à Inativação de Categoria com status 409, THEN THE Painel SHALL exibir a mensagem "Não é possível inativar a categoria {nome} porque existem produtos ativos vinculados. Inative ou mova esses produtos antes." com link para a listagem de Produtos filtrada por essa Categoria.
11. WHEN o Serviço_Product responde ao cadastro ou à edição de Categoria com sucesso, THE Painel SHALL exibir a Notificação correspondente e retornar à listagem de Categorias.
12. IF `GET /categories/{id}` responde com status 404, THEN THE Painel SHALL exibir a mensagem "Categoria não encontrada" com link para a listagem de Categorias.

### Requisito 10: Listagem de Produtos/Planos

**User Story:** Como Usuário_Admin, quero listar os planos por categoria e por tipo de plano, para que eu visualize o catálogo oferecido a cada tipo de estabelecimento.

#### Acceptance Criteria

1. THE Painel SHALL exibir a listagem de Produtos com as colunas Plano, nome, Categoria (`categoriaNome`), preço em BRL, status e ações.
2. WHEN o Usuário_Admin seleciona uma Categoria no filtro de Categoria, THE Camada_API SHALL obter os Produtos por `GET /products?categoriaId={id}`.
3. WHILE nenhuma Categoria está selecionada no filtro, THE Camada_API SHALL obter os Produtos por `GET /products` sem parâmetros.
4. THE Painel SHALL oferecer filtro por Plano (Todos, BASIC, PLUS, PREMIUM), filtro por status, busca textual por nome e paginação executados no navegador sobre a resposta recebida.
5. THE Painel SHALL refletir os filtros de Categoria, Plano, status e busca nos parâmetros de query da URL.
6. THE Formatador_Monetário SHALL exibir o preço no formato BRL com duas casas decimais (ex.: `R$ 1.234,56`).
7. WHILE a consulta de Produtos está em andamento, THE Painel SHALL exibir linhas em Skeleton no lugar dos registros.
8. WHEN o resultado filtrado de Produtos não contém registros, THE Painel SHALL exibir um Estado_Vazio com a ação "Cadastrar produto" ou "Limpar filtros", conforme existam filtros ativos.

### Requisito 11: Cadastro, edição e Inativação de Produtos/Planos

**User Story:** Como Usuário_Admin, quero cadastrar, editar e inativar planos, para que o catálogo de cada categoria fique correto.

#### Acceptance Criteria

1. THE Painel SHALL exibir no formulário de Produto os campos Plano (seleção obrigatória entre BASIC, PLUS e PREMIUM), nome (obrigatório), descrição (obrigatório), preço (obrigatório, maior ou igual a zero, até duas casas decimais) e Categoria (seleção obrigatória), e no modo edição também o campo status.
2. THE Painel SHALL listar no campo Categoria do formulário de Produto somente as Categorias com status ATIVO.
3. WHILE o formulário de Produto está em modo edição e a Categoria atual do Produto está INATIVA, THE Painel SHALL exibir a Categoria atual identificada como inativa e exigir a seleção de uma Categoria ATIVA para salvar.
4. WHEN o Usuário_Admin digita no campo preço, THE Formatador_Monetário SHALL aceitar vírgula como separador decimal e converter o texto em valor numérico.
5. WHEN o Usuário_Admin envia um formulário de cadastro de Produto válido, THE Camada_API SHALL enviar `POST /products` com o corpo ProductDto sem os campos somente leitura.
6. WHEN o Usuário_Admin envia um formulário de edição de Produto válido, THE Camada_API SHALL enviar `PUT /products/{id}` com o corpo ProductDto sem os campos somente leitura.
7. IF o Serviço_Product responde ao cadastro ou à edição de Produto com status 409, THEN THE Painel SHALL exibir a mensagem "Já existe o plano {Plano} na categoria {nome da Categoria}" junto aos campos Plano e Categoria.
8. IF o Serviço_Product responde ao cadastro ou à edição de Produto com status 404, THEN THE Painel SHALL exibir a mensagem "A categoria selecionada não existe mais" e recarregar a lista de Categorias do formulário.
9. IF o Serviço_Product responde ao cadastro ou à edição de Produto com status 400 após a validação do Painel ter sido aprovada, THEN THE Painel SHALL exibir a mensagem "A categoria selecionada está inativa ou os dados foram recusados pelo serviço" e recarregar a lista de Categorias do formulário.
10. WHEN o Usuário_Admin aciona "Inativar" em um Produto ATIVO, THE Painel SHALL exibir um Diálogo_De_Confirmação com o Plano e a Categoria do Produto.
11. WHEN o Usuário_Admin confirma a Inativação de Produto, THE Camada_API SHALL enviar `DELETE /products/{id}`.
12. WHEN o Serviço_Product responde à Inativação de Produto com status 204, THE Painel SHALL exibir a Notificação "Produto inativado" e atualizar o status exibido para INATIVO.
13. IF `GET /products/{id}` responde com status 404, THEN THE Painel SHALL exibir a mensagem "Produto não encontrado" com link para a listagem de Produtos.
14. FOR ALL valores monetários maiores ou iguais a zero com até duas casas decimais, formatar com o Formatador_Monetário e em seguida converter de volta SHALL produzir o valor de origem (propriedade de ida e volta).

### Requisito 12: Usuários

**User Story:** Como Usuário_Admin, quero listar e cadastrar usuários do Painel, para que novos membros da equipe Easy Eats possam acessar a administração.

#### Acceptance Criteria

1. THE Painel SHALL exibir a listagem de usuários obtida por `GET /usuarios` com a coluna username.
2. THE Camada_API SHALL descartar o campo `password` de cada item da resposta de `GET /usuarios` antes de entregar os dados às telas.
3. FOR ALL respostas de `GET /usuarios`, os modelos entregues pela Camada_API às telas SHALL conter o username de cada item e nenhum campo de senha (invariante).
4. THE Painel SHALL oferecer na listagem de usuários busca textual por username e paginação executadas no navegador.
5. THE Painel SHALL exibir no formulário de cadastro de usuário os campos username (obrigatório), senha (obrigatório) e confirmação de senha (obrigatório).
6. IF a confirmação de senha difere da senha, THEN THE Painel SHALL marcar o campo confirmação de senha como inválido com a mensagem "As senhas não conferem".
7. WHEN o Usuário_Admin envia um formulário de cadastro de usuário válido, THE Camada_API SHALL enviar `POST /usuarios` com o corpo JSON `{ username, password }`.
8. WHEN o Serviço_User responde ao cadastro de usuário com status 201, THE Painel SHALL exibir a Notificação "Usuário cadastrado com sucesso" e retornar à listagem de usuários.
9. THE Painel SHALL exibir na tela de usuários o aviso "Edição e exclusão de usuários ainda não estão disponíveis no serviço de usuários" e omitir as ações de editar e excluir.

### Requisito 13: Tratamento padronizado de erros

**User Story:** Como Usuário_Admin, quero mensagens de erro claras e consistentes independentemente do serviço que falhou, para que eu entenda o problema e saiba o que fazer.

#### Acceptance Criteria

1. WHEN uma resposta HTTP de erro é recebida de qualquer microsserviço, THE Normalizador_De_Erros SHALL produzir um Erro_Normalizado com status, título, mensagem e mapa de erros por campo.
2. WHEN o corpo de erro é um ProblemDetail, THE Normalizador_De_Erros SHALL usar `detail` como mensagem, `title` como título e `errors` como mapa de erros por campo.
3. WHEN o corpo de erro é um Erro_Padrão_Spring, THE Normalizador_De_Erros SHALL usar `message` como mensagem quando o campo estiver presente e não vazio e, na ausência dele, a mensagem padrão do status HTTP.
4. WHEN o corpo de erro é texto puro ou vazio, THE Normalizador_De_Erros SHALL usar a mensagem padrão do status HTTP.
5. THE Normalizador_De_Erros SHALL aplicar as mensagens padrão: 400 "Dados inválidos. Revise os campos destacados."; 401 "Sua sessão expirou. Entre novamente."; 403 "Você não tem permissão para esta ação."; 404 "Registro não encontrado."; 409 "A operação conflita com dados existentes."; 5xx "Erro interno no serviço. Tente novamente mais tarde.".
6. IF a requisição falha sem resposta do servidor ou com status 502, 503 ou 504, THEN THE Normalizador_De_Erros SHALL produzir a mensagem "Serviço {nome do serviço} indisponível. Tente novamente em instantes."
7. FOR ALL respostas de erro com status entre 400 e 599 e corpo arbitrário (JSON válido, JSON malformado, texto ou vazio), THE Normalizador_De_Erros SHALL produzir um Erro_Normalizado com mensagem não vazia e status igual ao da resposta (propriedade de totalidade).
8. WHEN um Erro_Normalizado sem tratamento específico da tela ocorre, THE Painel SHALL exibir a mensagem em Notificação de erro que permanece visível até o Usuário_Admin acioná-la para fechar.
9. WHEN uma operação de cadastro, edição ou Inativação é concluída com sucesso, THE Painel SHALL exibir Notificação de sucesso por 4 segundos.
10. WHEN o Erro_Normalizado contém erros por campo correspondentes a campos do formulário aberto, THE Painel SHALL exibir cada mensagem junto ao campo correspondente.

### Requisito 14: Formulários e ações destrutivas

**User Story:** Como Usuário_Admin, quero formulários previsíveis e proteção contra perda de dados, para que eu não perca alterações nem execute ações por engano.

#### Acceptance Criteria

1. WHILE um formulário possui campos inválidos, THE Painel SHALL manter o botão "Salvar" habilitado e, ao ser acionado, destacar todos os campos inválidos e mover o foco para o primeiro campo inválido.
2. WHEN o Usuário_Admin tenta sair de um formulário com alterações não salvas, THE Painel SHALL exibir um Diálogo_De_Confirmação com as opções "Continuar editando" e "Descartar alterações".
3. THE Diálogo_De_Confirmação SHALL posicionar o foco inicial no botão "Cancelar" e apresentar o botão da ação destrutiva com cor de alerta.
4. WHEN o Usuário_Admin pressiona Esc em um Diálogo_De_Confirmação, THE Painel SHALL fechar o diálogo sem executar a ação.
5. WHILE uma Inativação está em andamento, THE Painel SHALL desabilitar o botão de confirmação do Diálogo_De_Confirmação.

### Requisito 15: Responsividade

**User Story:** Como Usuário_Admin, quero usar o Painel em telas de diferentes tamanhos, para que eu consiga consultar e agir também pelo celular.

#### Acceptance Criteria

1. THE Painel SHALL exibir todas as telas em viewports de 360 px a 1920 px de largura sem rolagem horizontal da página.
2. WHILE o Painel é exibido em Viewport_Móvel, THE Painel SHALL apresentar cada registro das listagens como cartão empilhado contendo os campos principais e as ações.
3. WHILE o Painel é exibido em Viewport_Móvel, THE Painel SHALL apresentar os campos de formulário em coluna única.
4. THE Painel SHALL oferecer áreas de toque de no mínimo 44 × 44 px para botões e controles interativos em Viewport_Móvel.

### Requisito 16: Acessibilidade

**User Story:** Como Usuário_Admin com necessidades de acessibilidade, quero operar todo o Painel por teclado e leitor de tela, para que o Painel atenda ao WCAG 2.1 nível AA.

#### Acceptance Criteria

1. THE Painel SHALL permitir executar todas as funcionalidades somente pelo teclado, em ordem de foco coerente com a ordem visual.
2. THE Painel SHALL exibir indicador de foco visível em todos os elementos interativos em ambos os Temas.
3. THE Painel SHALL fornecer nome acessível para todos os botões compostos somente por ícone.
4. THE Painel SHALL associar cada mensagem de validação ao respectivo campo de formulário de modo que leitores de tela anunciem a mensagem.
5. THE Painel SHALL anunciar as Notificações por meio de região viva (live region) para leitores de tela.
6. THE Painel SHALL oferecer um link "Pular para o conteúdo" como primeiro elemento focável do Shell.
7. WHEN uma navegação entre páginas é concluída, THE Painel SHALL atualizar o título do documento com o nome da página e mover o foco para o título principal da página.
8. THE Painel SHALL obter zero violações de nível A e AA em verificação automatizada de acessibilidade (axe-core) nas telas de login, Dashboard, listagens e formulários.

### Requisito 17: Idioma e formatação regional

**User Story:** Como Usuário_Admin, quero o Painel inteiramente em português do Brasil, para que textos, datas e valores sigam o padrão local.

#### Acceptance Criteria

1. THE Painel SHALL exibir todos os textos de interface em português do Brasil.
2. THE Painel SHALL formatar datas no padrão `dd/MM/yyyy HH:mm`, números com separador de milhar `.` e separador decimal `,`, e valores monetários em BRL.
3. THE Painel SHALL exibir os valores de status ATIVO e INATIVO com os rótulos "Ativo" e "Inativo".

### Requisito 18: Configuração por ambiente e camada de integração

**User Story:** Como desenvolvedor, quero que as URLs de cada microsserviço sejam configuráveis por ambiente, para que o Painel se conecte aos backends sem alterar código-fonte e sem depender de CORS.

#### Acceptance Criteria

1. THE Configuração_De_Ambiente SHALL definir uma URL base independente para o Serviço_Product, o Serviço_Client e o Serviço_User.
2. THE Configuração_De_Ambiente SHALL usar por padrão os prefixos relativos `/api/product`, `/api/client` e `/api/user`, de forma que o navegador envie requisições somente para a mesma origem do Painel.
3. THE Camada_API SHALL montar todas as URLs de requisição a partir da URL base do microsserviço correspondente na Configuração_De_Ambiente.
4. THE Camada_API SHALL representar cada DTO com os campos, tipos e valores enumerados listados no Anexo A.
5. WHEN o servidor de desenvolvimento do Angular é iniciado, THE Proxy_Dev SHALL encaminhar cada prefixo de serviço ao endereço do microsserviço correspondente, removendo o prefixo do caminho.
6. THE Proxy_Dev SHALL aceitar o endereço de cada microsserviço por variável de ambiente, com padrões `http://localhost:8081` (Serviço_User), `http://localhost:8082` (Serviço_Product) e `http://localhost:8084` (Serviço_Client), seguindo a convenção de portas do projeto.
7. WHEN o endereço de um microsserviço é alterado por variável de ambiente, THE Proxy_Dev SHALL usar o novo endereço sem alteração de arquivos versionados.
8. THE Camada_API SHALL consultar a listagem de Clientes com `size` limitado a 100.

### Requisito 19: Empacotamento em contêiner e docker compose

**User Story:** Como desenvolvedor, quero uma imagem de contêiner do Painel com proxy reverso para os serviços, para que o Painel rode junto aos microsserviços via docker compose.

#### Acceptance Criteria

1. THE Imagem_Do_Painel SHALL ser construída a partir do repositório por um Dockerfile multi-estágio que gera o build de produção do Angular e o serve com nginx.
2. THE Proxy_Reverso SHALL responder com o `index.html` da aplicação para qualquer rota que não corresponda a um arquivo estático nem a um prefixo de serviço.
3. THE Proxy_Reverso SHALL encaminhar `/api/product`, `/api/client` e `/api/user` aos respectivos microsserviços, removendo o prefixo do caminho e preservando o cabeçalho `Authorization`.
4. THE Imagem_Do_Painel SHALL ler o endereço de cada microsserviço de variáveis de ambiente no início do contêiner, sem necessidade de novo build.
5. THE Proxy_Reverso SHALL servir arquivos estáticos com hash no nome com cache de longa duração e servir `index.html` sem cache.
6. THE repositório SHALL conter um arquivo docker compose que inicia o Painel com as variáveis de endereço dos microsserviços e a porta do Painel parametrizadas.
7. IF um microsserviço configurado está inacessível, THEN THE Proxy_Reverso SHALL responder com status 502, 503 ou 504 e continuar servindo a SPA e os demais prefixos.

### Requisito 20: Qualidade, testes e documentação

**User Story:** Como desenvolvedor do projeto, quero testes, lint e documentação, para que colegas consigam rodar, avaliar e evoluir o Painel.

#### Acceptance Criteria

1. THE repositório SHALL conter testes unitários para cada serviço da Camada_API verificando método HTTP, URL, parâmetros, cabeçalhos e corpo das requisições.
2. THE repositório SHALL conter testes unitários para o Normalizador_De_Erros, o Validador_CNPJ, o Formatador_Monetário, a Guarda_De_Rotas e o mecanismo de anexação do Token_JWT.
3. THE repositório SHALL conter testes baseados em propriedades para as propriedades de ida e volta do CNPJ e do Formatador_Monetário, para a validação de dígitos verificadores do CNPJ, para a totalidade do Normalizador_De_Erros e para o descarte do campo `password`.
4. THE repositório SHALL conter testes de componente para login, listagem e formulário de Clientes, formulário de Produto e Inativação de Categoria com resposta 409.
5. THE suíte de testes SHALL executar em modo de execução única, sem modo de observação, por um comando documentado.
6. THE repositório SHALL atingir no mínimo 80% de cobertura de linhas nos serviços da Camada_API, no Normalizador_De_Erros, no Validador_CNPJ e no Formatador_Monetário.
7. THE repositório SHALL conter configuração de lint executável por comando documentado, e a verificação de lint SHALL terminar sem erros.
8. THE repositório SHALL conter um README em português do Brasil que descreve pré-requisitos, instalação, execução em desenvolvimento com Proxy_Dev, variáveis de ambiente, execução dos testes e do lint, build e execução com Docker e docker compose, e a lista de dependências de outros times.

## Fora de escopo / dependências de outros times

Itens que o Painel não implementa nesta entrega ou que dependem de mudanças em repositórios de colegas:

| # | Item | Responsável | Impacto no Painel |
|---|------|-------------|-------------------|
| D1 | **CORS ou API Gateway**: nenhum backend configura CORS e o gateway (porta 8080 reservada) não existe. | Todos os serviços / time do gateway | O Painel depende do Proxy_Dev e do Proxy_Reverso. Quando houver gateway, basta apontar as URLs base para ele. |
| D2 | **Porta do Serviço_User** não configurada (padrão 8080, colide com o Serviço_Product atual e com o gateway reservado). | Repositório `joao-vitor1007/user` | Proposta: fixar 8081 conforme convenção. Até lá, a porta é ajustada por variável de ambiente no Proxy_Dev. |
| D3 | **Porta do Serviço_Product** hoje é 8080 por padrão; a convenção prevê 8082. | Repositório `api-product` | Ajustar `SERVER_PORT` para 8082 ou sobrescrever a variável do Proxy_Dev. |
| D4 | **`GET /usuarios` devolve `password`** em rota pública. | Repositório `joao-vitor1007/user` | O Painel descarta o campo (Requisito 12), mas o dado continua trafegando. Solicitar remoção do campo do DTO e proteção da rota. |
| D5 | **Login com senha em query string** (`POST /auth/login?username=&password=`). | Repositório `joao-vitor1007/user` | Senha pode aparecer em logs de proxy e servidor. Solicitar aceite de corpo JSON; o Painel adaptará a Camada_API quando disponível. |
| D6 | **Sem endpoints de edição, exclusão, papéis (roles) ou perfil super admin** de usuários; `POST /usuarios` é público. | Repositório `joao-vitor1007/user` | Qualquer usuário cadastrado acessa o Painel. Telas de edição/exclusão e controle por papel ficam pendentes. |
| D7 | **Serviço_Product e Serviço_Client não validam o Token_JWT** (sem autenticação). | Repositórios `api-product` e `EasyEats-Project/client` | A Guarda_De_Rotas protege apenas a navegação no Painel; as APIs continuam abertas. |
| D8 | **Erros do Serviço_Product sem mensagem no corpo**: o padrão do Spring Boot omite `message` e erros de validação por campo. | Repositório `api-product` | O Painel usa mensagens padrão por status e mensagens contextuais por tela. Sugestão: habilitar `server.error.include-message` ou adotar ProblemDetail. |
| D9 | **Contrato do Serviço_Base** inexistente (sem endpoints nem propósito definido). | Repositório `joao-vitor1007/base` | Nenhuma tela integrada; item de menu e telas serão especificados quando houver contrato. |
| D10 | **Vendas, assinaturas e métricas agregadas** não possuem endpoints. | A definir | O Dashboard exibe apenas números derivados das listagens existentes (Requisito 5). Receita, assinaturas e gráficos históricos ficam pendentes. |
| D11 | **Reativação de Cliente**: `ClientUpdateRequest` não possui `status`. | Repositório `EasyEats-Project/client` | Clientes inativados não podem ser reativados pelo Painel. |
| D12 | **Paginação no servidor** para `GET /products`, `GET /categories` e `GET /usuarios`. | Repositórios `api-product` e `joao-vitor1007/user` | Busca, filtros e paginação dessas listas são feitos no navegador. |

Também fora de escopo nesta entrega: internacionalização para outros idiomas, recuperação de senha, auditoria de ações, upload de arquivos e publicação (deploy) em nuvem.

## Anexo A: Contratos de API consumidos

Campos marcados com (RO) são somente leitura e não são enviados em `POST`/`PUT`.

### Serviço_Product (URL base padrão `/api/product`)

| Método e caminho | Sucesso | Erros tratados |
|---|---|---|
| `GET /products?categoriaId={opcional}` | 200 `ProductDto[]` | — |
| `GET /products/{id}` | 200 `ProductDto` | 404 |
| `GET /products/identifier/{plano}` | 200 `ProductDto[]` | 404 quando não há registros |
| `POST /products` | 201 `ProductDto` | 400 validação ou categoria inativa, 404 categoria inexistente, 409 Plano já existe na Categoria |
| `PUT /products/{id}` | 200 `ProductDto` | 400, 404, 409 |
| `DELETE /products/{id}` | 204 (Inativação) | 404 |
| `GET /categories` | 200 `CategoryDto[]` | — |
| `GET /categories/{id}` | 200 `CategoryDto` | 404 |
| `POST /categories` | 201 `CategoryDto` | 400, 409 nome duplicado (sem distinção de maiúsculas) |
| `PUT /categories/{id}` | 200 `CategoryDto` | 400, 404, 409 |
| `DELETE /categories/{id}` | 204 (Inativação) | 404, 409 há Produtos ATIVOS na Categoria |

- **ProductDto**: `id: number` (RO), `productIdentifier: "BASIC" | "PLUS" | "PREMIUM"` (obrigatório), `nome: string` (obrigatório), `descricao: string` (obrigatório), `preco: number` (obrigatório, ≥ 0), `categoriaId: number` (obrigatório), `categoriaNome: string` (RO), `status?: "ATIVO" | "INATIVO"` (padrão ATIVO; nulo no `PUT` mantém o atual), `criadoEm: string` ISO LocalDateTime (RO), `atualizadoEm: string` ISO LocalDateTime (RO).
- **CategoryDto**: `id: number` (RO), `nome: string` (obrigatório), `descricao?: string`, `status?: "ATIVO" | "INATIVO"`, `criadoEm: string` (RO), `atualizadoEm: string` (RO).
- Formato de erro: Erro_Padrão_Spring.

### Serviço_Client (URL base padrão `/api/client`)

| Método e caminho | Sucesso | Erros tratados |
|---|---|---|
| `POST /client` | 201 `ClientResponse` (sempre ATIVO) | 400, 409 CNPJ duplicado |
| `GET /client?status=&nome=&page=0&size=20` | 200 `PageResponse<ClientResponse>` (ordenado por `razaoSocial`, `size` ≤ 100) | 400 |
| `GET /client/{id}` (UUID) | 200 `ClientResponse` (inclui INATIVOS) | 404 |
| `PUT /client/{id}` | 200 `ClientResponse` | 400, 404 |
| `DELETE /client/{id}` | 204 (Inativação) | 404 |

- **ClientRequest**: `razaoSocial: string` (obrigatório, ≤ 150), `nomeFantasia?: string` (≤ 150), `cnpj: string` (obrigatório, numérico ou alfanumérico, com ou sem máscara), `email: string` (obrigatório, e-mail válido, ≤ 150), `telefone?: string` (≤ 20; dígitos, espaços e `+ ( ) -`).
- **ClientUpdateRequest**: mesmos campos de ClientRequest, exceto `cnpj`.
- **ClientResponse**: `id: string` (UUID), `razaoSocial`, `nomeFantasia`, `cnpj`, `email`, `telefone`, `status: "ATIVO" | "INATIVO"`, `dataCadastro: string` ISO Instant, `dataAtualizacao: string` ISO Instant.
- **PageResponse<T>**: `content: T[]`, `page: number`, `size: number`, `totalElements: number`, `totalPages: number`.
- Formato de erro: ProblemDetail (`title`, `status`, `detail`, `errors?: Record<campo, mensagem>`).

### Serviço_User (URL base padrão `/api/user`)

| Método e caminho | Autenticação | Sucesso | Erros tratados |
|---|---|---|---|
| `POST /auth/login?username=&password=` | Pública | 200 Token_JWT em texto puro | 401, 403 |
| `POST /usuarios` corpo `{ username, password }` | Pública | 201 sem corpo | 400, 409 (se existir) |
| `GET /usuarios` | Pública | 200 `UsuarioDTO[]` `{ username, password }` (o Painel descarta `password`) | — |
| `GET /usuarios/listar-por-nome/{username}` | `Authorization: Bearer` | 200 `Usuario { id, username, password }` (o Painel descarta `password`) | 401, 403, 404 |

- Formato de erro: texto puro ou corpo vazio.

### Serviço_Base

Sem endpoints. Fora de escopo de integração (ver D9).
