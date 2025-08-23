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
---

## 🔧 Pré-requisitos
- [Java 17](https://www.oracle.com/java/technologies/downloads/#java17)
- [Docker](https://www.docker.com/) (necessário para subir o MongoDB local)
- [MongoDB Compass](https://www.mongodb.com/try/download/compass) (opcional, interface gráfica para visualizar dados)

> 💡 Observação: no Windows/Mac pode ser mais prático usar o **Docker Desktop**.  
No Linux basta ter o Docker Engine instalado.

---

## 🗂 Estrutura do Projeto
```markdown
maisprati-hub-server/
├── docker/                             # Arquivos Docker e Compose
├── src/main/java/com/maisprati/hub
│   ├── config/                         # Configurações do Mongo e segurança
│   ├── controller/                     # Endpoints da API
│   ├── model/                          # Entidades e enums
│   ├── repository/                     # Interface com MongoDB
│   ├── service/                        # Lógica de negócio
│   ├── utils/                          # Classes utilitárias
│   └── HubServerApplication.java
│
├── resources/ 
│   ├── application.properties          # config base
│   ├── application-dev.properties      # ambiente dev
│   └── application-prod.properties     # ambiente prod
└── pom.xml                             # Dependências do projeto
```
- 📝 Cada pasta está bem organizada para facilitar a navegação, mesmo para iniciantes.
---

## 🔄 Fluxo da API

```mermaid
flowchart LR
    A[Cliente] --> B[Controller]
    B --> C[Service]
    C --> D[Repository]
    D --> E[(MongoDB)]

    B:::controller
    C:::service
    D:::repository
```
* **Controller**: recebe e responde requisições
* **Service**: lógica de negócio
* **Repository**: acessa o MongoDB
---

## 👥 Usuários e Perfis
- `ADMIN` – acesso administrativo
- `PROFESSOR` – gerenciamento acadêmico
- `STUDENT` – estudantes do programa

## 🛡️ Segurança
- Senhas com **hash BCrypt**
- E-mails duplicados não são permitidos
- Variáveis sensíveis carregadas via **.env**
---

## 🐳 Guia Completo: MongoDB com Docker
### 1️⃣ Criar e subir o container
```bash
  docker-compose -f docker-compose.dev.yml up -d
```
Explicando os parâmetros:
- `-f docker-compose.dev.yml` → indica qual arquivo Compose usar
- `up` → cria os containers definidos no arquivo
- `-d` → roda em background

### 2️⃣ Conferir se o container está rodando
```bash
  docker ps
```
- 👉 O nome do container será `mongodev`.

### 3️⃣ Parar o container (sem apagar dados)
```bash
  docker-compose -f docker-compose.dev.yml stop
```
- Isso apenas **pausa** o container. Os dados continuam salvos no volume.

### 4️⃣ Rodar novamente
```bash
  docker-compose -f docker-compose.dev.yml start
```

### 5️⃣ Parar e remover o container (mantendo os dados)
```bash
  docker-compose -f docker-compose.dev.yml down
```
- Isso apaga o container, mas o volume `mongo_data` com os dados fica guardado.
  Quando rodar `up -d` de novo, os dados estarão lá.

### 6️⃣ Parar e remover container + apagar todos os dados
```bash
  docker-compose -f docker-compose.dev.yml down -v
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

### 8️⃣ Visualizar dados no MongoDB Compass (opcional)
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
> Em andamento — por enquanto implementamos apenas a camada de serviço.

## 📌 Roadmap (MVP)
1. [x] Estrutura inicial (Spring Boot + MongoDB)
2. [x] Criação de usuários (com hashing)
3. [x] Separação por papéis (student, professor, admin)
4. [ ] Inicialização do Admin
5. [ ] Endpoints REST para autenticação e cadastro
6. [ ] Integração JWT
7. [ ] Integração com frontend (CORS, porta, URLs e envio de JWT)
8. [ ] Testes unitários e de integração
9. [ ] Deploy inicial (Render)

---

## 📖 Guia de Contribuição e Git Básico
### 1️⃣ Clonar repositório
```bash
  git clone https://github.com/flaviare1s/maisprati-hub-server.git
  cd maisprati-hub-server
```

### 2️⃣ Criar branch para nova feature
```bash
  git checkout -b feature/nome-da-feature
```

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
