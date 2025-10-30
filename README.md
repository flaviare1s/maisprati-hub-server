# 🚀 +PraTi Hub - Backend

[![Java](https://img.shields.io/badge/Java-17-red?logo=java)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green?logo=spring)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green?logo=mongodb)](https://www.mongodb.com/)
[![Docker](https://img.shields.io/badge/Docker-3.8-blue?logo=docker)](https://www.docker.com/)

> Bem-vindo ao backend do **+PraTi Hub** 🎓  
Este projeto é uma **API RESTful** construída com **Java + Spring Boot**, usando **MongoDB** como banco de dados.  
Conta com suporte a **Docker** para rodar o banco localmente, perfis de configuração (`dev` e `prod`) e senhas seguras com **hash BCrypt**.
---

## 💡 Conceitos Básicos
- **Spring Boot**: framework Java para criar APIs rapidamente
- **MongoDB**: banco NoSQL que armazena dados em documentos JSON
- **Docker**: cria contêineres para rodar serviços sem instalar nada no PC
- **Profiles do Spring**: diferentes configurações para `dev` e `prod`
- **BCrypt**: criptografia de senhas para maior segurança
- **Swagger**: documentação d API. Disponível em: http://localhost:8080/swagger-ui/index.html
---

## 🔧 Pré-requisitos
- [Java 17](https://www.oracle.com/java/technologies/downloads/#java17)
- [Docker](https://www.docker.com/) (necessário para subir o MongoDB local)
- [MongoDB Compass](https://www.mongodb.com/try/download/compass) (opcional, interface gráfica para visualizar dados)

> 💡 Observação: no Windows/Mac pode ser mais prático usar o **Docker Desktop**.  
No Linux basta ter o Docker Engine instalado.

### ⚠️ Lombok no VS Code e IntelliJ
- O Lombok já está configurado no `pom.xml`, então o projeto compila em qualquer máquina.
- **VS Code**: instale a extensão [Lombok Annotations Support](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-lombok). Sem ela, o editor pode mostrar erros falsos em métodos gerados automaticamente(`getters`, `setters`, `@Builder`), mas o build funciona normalmente.
- **IntelliJ IDEA**: instale o **plugin Lombok** `(Settings → Plugins → Marketplace → Lombok → Install)` e **habilite o Annotation Processing** `(Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable annotation processing)`.
  - Sem habilitar o Annotation Processing, o IntelliJ também mostrará erros falsos nos métodos gerados automaticamente, mesmo com o plugin instalado.
---

## 🗂 Estrutura do Projeto (Camadas)
```markdown
maisprati-hub-server/
├── docker/                             # Arquivos Docker e Compose
├── src/main/java/com/maisprati/hub
│   ├── application/                    # Camada de aplicação: serviços e DTOs
│   │   ├── dto                         # Objetos de transferência de dados
│   │   └── service                     # Lógica de negócio
│   ├── domain/                         # Camada de domínio: regras e modelos principais
│   │   ├── enums                       # Tipos de enumeração
│   │   └── model                       # Entidades do sistema
│   ├── infrastructure/                 # Camada de infraestrutura: acesso a dados e segurança
│   │   ├── config                      # Configurações do sistema e inicialização de dados
│   │   ├── persistence                 # Repositórios e mapeamentos para MongoDB
│   │   └── security                    # Autenticação, JWT, filtros e SecurityConfig
│   └── presentation/                   # Camada de apresentação: controllers e exceptions
│   ├── controller                      # Endpoints REST
│   ├── dto                             # DTOs específicos de entrada/saída
│   └── exception                       # Tratamento de erros
├── resources/                          # Configurações e arquivos estáticos
│   ├── application.properties          # config base
│   ├── application-dev.properties      # ambiente dev
│   └── application-prod.properties     # ambiente prod
└── pom.xml                             # Dependências do projeto
```
- 📝 Cada pasta está bem organizada para facilitar a navegação, mesmo para iniciantes.
---

## 🔄 Fluxo da API (Camadas)

```mermaid
flowchart LR
    Cliente --> Presentation[Controller]
    Presentation --> Application[Service]
    Application --> Domain[Model / Entities]
    Application --> Infrastructure[Repositories / Security]
    Infrastructure --> MongoDB[(MongoDB)]
    Domain --> Infrastructure
```
- **Cliente** → envia requisição HTTP
- **Controller** → recebe e valida entrada
- **Service** (Application) → lógica de negócio, regras do sistema
- **Domain** → entidades e enums principais
- **Infrastructure** → repositórios acessam MongoDB, JWT e filtros de segurança
- **MongoDB** → armazena dados persistentes
---

## 👥 Usuários e Perfis
- `ADMIN` – acesso administrativo e gerenciamento acadêmico
- `STUDENT` – estudantes do programa

## 🛡️ Segurança
- Senhas com **hash BCrypt**
- E-mails duplicados não são permitidos
- Variáveis sensíveis carregadas via **.env**
---

## 🛡️ Autenticação e Autorização (JWT)
O sistema usa **Spring Security** com **JWT** (**JSON Web Tokens**) para proteger rotas privadas.

### 1️⃣ Chave secreta
- Crie um arquivo `.env` na raiz do projeto com:
  ```dotenv
   JWT_SECRET=SuaChaveSecretaBase64
  ```
  - ⚠️ `JWT_SECRET` deve estar em Base64.

Configuração já pronta no `application.properties`
```properties
# Chave importada do .env
jwt.secret=${JWT_SECRET}

# Expiração do token em segundos (ex: 3600 = 1 hora)
jwt.expiration-seconds=3600
```
### 2️⃣ Endpoints
- **POST /api/auth/register** – registra usuário com role `STUDENT`.
- **POST /api/auth/login** – retorna token JWT no JSON de resposta.

### 3️⃣ Rotas privadas
- Todas exigem header:
  ```http request
  Authorization: Bearer <token_do_login>
  ```
- Token inválido ou ausente → **401 Unauthorized**
- Token válido, mas papel não permitido → **403 Forbidden**

### 4️⃣ Papéis (Roles)
- `ADMIN` – acesso total
- `STUDENT` – acesso limitado ao próprio perfil (a implementar)
---

## 🎲 Usando MongoDB local (sem Docker)
- Instale o [MongoDB Compass](https://www.mongodb.com/try/download/compass) no seu computador.
- No `src/main/resources/application-dev.properties`, já está configurado:
  ```properties
  # MongoDB local (sem autenticação)
  spring.data.mongodb.uri=mongodb://localhost:27017/maisprati-hub
  spring.data.mongodb.database=maisprati-hub
  ```
- Abra o MongoDB Compass e conecte usando a URI acima:
  ```text
  mongodb://localhost:27017/maisprati-hub
  ```
---

## 🐳 Guia Completo: MongoDB com Docker
- Caso queira usar Docker, com autenticação, configure no `application-dev.properties`:
  ```properties
  # MongoDB local via Docker (com autenticação)
  #spring.data.mongodb.uri=mongodb://admin:admin123@localhost:27017/maisprati-hub?authSource=admin
  ```
  > ⚠️ Lembre-se de **comentar a URI local e descomentar a do Docker** antes de subir o container.

### 1️⃣ Criar e subir o container
```bash
  docker-compose -f docker/docker-compose.dev.yml up -d
```
Explicando os parâmetros:
- `-f docker/docker-compose.dev.yml` → indica qual arquivo Compose usar
- `up` → cria os containers definidos no arquivo
- `-d` → roda em background

### 2️⃣ Conferir se o container está rodando
```bash
  docker ps
```
- 👉 O nome do container será `mongodev`.

### 3️⃣ Parar o container (sem apagar dados)
```bash
  docker-compose -f docker/docker-compose.dev.yml stop
```
- Isso apenas **pausa** o container. Os dados continuam salvos no volume.

### 4️⃣ Rodar novamente
```bash
  docker-compose -f docker/docker-compose.dev.yml start
```

### 5️⃣ Parar e remover o container (mantendo os dados)
```bash
  docker-compose -f docker/docker-compose.dev.yml down
```
- Isso apaga o container, mas o volume `mongo_data` com os dados fica guardado.
  Quando rodar `up -d` de novo, os dados estarão lá.

### 6️⃣ Parar e remover container + apagar todos os dados
```bash
  docker-compose -f docker/docker-compose.dev.yml down -v
```
- ⚠️ **Atenção**: o `-v` remove também os volumes → **apaga todos os dados do banco**.
  Use isso apenas se quiser começar com o banco zerado.

### 7️⃣ Acessar Mongo pelo terminal (admin)
```bash
  docker exec -it mongodev mongosh -u admin -p admin123 --authenticationDatabase admin
```
- Isso abre o shell do Mongo dentro do container.
- Exemplo de comandos úteis:
  ```bash
  show dbs
  use maisprati-hub
  db.users.find()
  ```

### 8️⃣ Visualizar dados no MongoDB Compass
1. Instale o [MongoDB Compass](https://www.mongodb.com/try/download/compass)
2. Conecte-se com a URI:
    ```bash
    mongodb://admin:admin123@localhost:27017/maisprati-hub
    ```
3. Agora você pode ver coleções, documentos e até rodar queries com interface gráfica.
---

## ⚙️ Build e Execução

> 💡 Observação: o projeto usa o **Maven Wrapper (`./mvnw`)**, então você não precisa ter o Maven instalado localmente.  
Se já tiver Maven instalado, pode usar `mvn` normalmente.

### 1️⃣ Compilar o projeto
Na raiz do repositório:
```bash
  ./mvnw clean install
```
- O artefato será gerado em: `target/hub-0.0.1-SNAPSHOT.jar`

### 2️⃣ Rodar com Maven (perfil dev)
```bash
  ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
  
  # Ou, como dev já é o perfil padrão, apenas:
  ./mvnw spring-boot:run
```

### 3️⃣ Rodar com java -jar (perfil dev)
```bash
  java -jar target/hub-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### Se a conexão estiver OK, você verá:
```yaml
✅ Conexão com MongoDB estabelecida! Banco: maisprati-hub
```

### ⚠️ Avançado / Opcional: Rodando com Atlas (prod)
> Apenas para testes de produção. Usar **somente se tiver acesso à string de conexão Atlas** (já configurada).
```bash
  java -jar target/hub-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```
- As credenciais estão no `.env` e não ficam no código.
---

## 🛠 Endpoints (MVP)

| Método | Endpoint          | Acesso                   | Descrição                                     |
|--------|-------------------|--------------------------|-----------------------------------------------|
| POST   | api/auth/register | Público                  | Registrar novo usuário (role padrão: STUDENT) |
| POST   | api/auth/login    | Público                  | Login de usuário e retorno do token JWT       |
| GET    | api/users         | ADMIN                    | Listar todos os usuários                      |
| GET    | api/users/{id}    | ADMIN ou próprio usuário | Obter dados de um usuário específico          |
| PUT    | api/users/{id}    | ADMIN ou próprio usuário | Atualizar dados de um usuário                 |
| PUT    | api/users/admin   | ADMIN                    | Atualizar dados do admin                      |
| DELETE | api/users/{id}    | ADMIN                    | Remover usuário                               |


## 📌 Roadmap (MVP)
1. [x] Estrutura inicial (Spring Boot + MongoDB)
2. [x] Criação de usuários (com hashing)
3. [x] Separação por papéis (student, professor, admin)
4. [x] Inicialização do Admin
5. [x] Endpoints REST para autenticação e cadastro
6. [x] Integração JWT
7. [ ] Integração com frontend (CORS, porta, URLs e envio de JWT)
8. [ ] Testes unitários e de integração
9. [ ] Deploy inicial (Render)

---

## 📖 Guia de Contribuição e Git Básico
### 1️⃣ Clonar repositório e trocar para develop
```bash
  git clone https://github.com/flaviare1s/maisprati-hub-server.git
  cd maisprati-hub-server
  
  git checkout develop
  git pull origin develop
```

### 2️⃣ Criar branch para nova feature a partir da develop
```bash
  git checkout -b feature/nome-da-feature
```
- **Todas as novas features devem começar da develop**, evitando conflitos e garantindo que a branch esteja sempre atualizada.

### 3️⃣ Commit de alterações
```bash
  git add .
  git commit -m "feat: descrição da feature"
```

### 4️⃣ Subir branch para o remoto
```bash
  git push origin feature/nome-da-feature
```

### 5️⃣ Abrir Pull Request
- Sempre a partir de `develop`
- Revisão de colegas antes do merge

### 6️⃣ Atualizar branch local com develop
```bash
  git checkout develop
  git pull origin develop
```

### 💡 Dicas de git
- `git status` → vê arquivos modificados
- `git log --oneline` → histórico de commits
- `git diff` → ver diferenças antes do commit
