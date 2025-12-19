# +praTiHub - Backend

**🇧🇷 Português** | **[🇺🇸 English](README_EN.md)**

[![Java](https://img.shields.io/badge/Java-17-red?logo=java)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green?logo=spring)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green?logo=mongodb)](https://www.mongodb.com/)
[![Docker](https://img.shields.io/badge/Docker-3.8-blue?logo=docker)](https://www.docker.com/)

API RESTful construída com **Java + Spring Boot** e **MongoDB**, com autenticação JWT, perfis de ambiente e suporte a Docker.

---

## 🛠️ Tecnologias

- **Spring Boot 3.5** - Framework principal
- **MongoDB** - Banco de dados NoSQL
- **Spring Security + JWT** - Autenticação e autorização
- **BCrypt** - Hashing de senhas
- **Swagger/OpenAPI** - Documentação da API disponível em `http://localhost:8080/swagger-ui/index.html`
- **Docker** - Containerização
- **Lombok** - Redução de boilerplate
- **Maven** - Gerenciamento de dependências

---

## 🚀 Quick Start

### Pré-requisitos
- [Java 17](https://www.oracle.com/java/technologies/downloads/#java17)
- MongoDB Server
- [Docker](https://www.docker.com/) e Docker Compose (opcional)
- [MongoDB Compass](https://www.mongodb.com/try/download/compass) (opcional)


### Configuração Inicial

1. **Clone o repositório**
```bash
git clone https://github.com/flaviare1s/maisprati-hub-server.git
cd maisprati-hub-server
```

2. **Configure as variáveis de ambiente**

Crie um arquivo `.env` na raiz do projeto:
```env
JWT_SECRET=your_base64_encoded_secret_here
APP_FRONTEND_URL=http://localhost:3000
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
GMAIL_API_REFRESH_TOKEN=your_gmail_refresh_token
EMAIL_FROM=your_email@gmail.com
```

3. **Inicie o MongoDB

- Opção A - Usar Docker
```bash
docker-compose -f docker/docker-compose.dev.yml up -d
```
- Opção B - Usar o MongoDB Server instalado no seu PC

4. **Execute a aplicação**

**Perfil padrão (dev):**
```bash
# Linux/Mac
./mvnw spring-boot:run

# Windows
.\mvnw.cmd spring-boot:run
```

**Perfil dev (explícito):**
```bash
# Linux/Mac
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Windows
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

**Perfil prod:**
```bash
# Linux/Mac
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Windows
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=prod"
```

A API estará disponível em `http://localhost:8080`

**Documentação Swagger:** `http://localhost:8080/swagger-ui/index.html`

---

## 🏗️ Arquitetura

O projeto segue uma arquitetura em camadas limpa e bem definida:

```
src/main/java/com/maisprati/hub/
├── presentation/         # Controllers e DTOs de API
├── application/          # Serviços e lógica de negócio
├── domain/              # Entidades e enums
└── infrastructure/      # Repositórios, segurança e configurações
```

### Fluxo de Requisição
```
Cliente → Controller → Service → Repository → MongoDB
```

---

## 🔐 Autenticação

O sistema utiliza **JWT (JSON Web Tokens)** para autenticação.

### Papéis de Usuário
- **ADMIN** - Acesso administrativo completo
- **STUDENT** - Acesso de estudante

### Endpoints Públicos
- `POST /api/auth/register` - Registro de novo usuário
- `POST /api/auth/login` - Login e obtenção do token JWT

### Endpoints Protegidos
Requerem header de autorização:
```http
Authorization: Bearer <seu_token_jwt>
```

| Método | Endpoint           | Acesso                   | Descrição                    |
|--------|-------------------|--------------------------|------------------------------|
| GET    | /api/users        | ADMIN                    | Listar todos os usuários     |
| GET    | /api/users/{id}   | ADMIN ou próprio usuário | Obter usuário específico     |
| PUT    | /api/users/{id}   | ADMIN ou próprio usuário | Atualizar usuário            |
| DELETE | /api/users/{id}   | ADMIN                    | Remover usuário              |

---

## 🐳 Docker

### Comandos Úteis

```bash
# Iniciar containers
docker-compose -f docker/docker-compose.dev.yml up -d

# Parar containers (mantém dados)
docker-compose -f docker/docker-compose.dev.yml stop

# Reiniciar containers
docker-compose -f docker/docker-compose.dev.yml start

# Parar e remover containers (mantém volumes)
docker-compose -f docker/docker-compose.dev.yml down

# Parar e remover tudo (incluindo dados)
docker-compose -f docker/docker-compose.dev.yml down -v

# Acessar shell do MongoDB
docker exec -it mongodev mongosh -u admin -p admin123 --authenticationDatabase admin
```

### Conexão MongoDB

**Via MongoDB Compass:**
```
mongodb://admin:admin123@localhost:27017/maisprati-hub
```

**Configuração local (sem Docker):**
```
mongodb://localhost:27017/maisprati-hub
```

---

## 📝 Desenvolvimento

### Build do Projeto
```bash
./mvnw clean install
```

### Executar Testes
```bash
./mvnw test
```

### Perfis de Ambiente
- **dev** (padrão) - Desenvolvimento local
- **prod** - Produção (MongoDB Atlas)

### Lombok

O projeto utiliza Lombok para reduzir código boilerplate.

**VS Code:** Instale a extensão [Lombok Annotations Support](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-lombok)

**IntelliJ IDEA:**
1. Instale o plugin Lombok (Settings → Plugins → Marketplace)
2. Habilite Annotation Processing (Settings → Build, Execution, Deployment → Compiler → Annotation Processors)
