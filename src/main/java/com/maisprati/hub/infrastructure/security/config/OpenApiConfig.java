package com.maisprati.hub.infrastructure.security.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "+PraTi Hub API",
                version = "v1",
                description = """
                        API de demonstração com autenticação JWT e recursos de usuários, times, agendamentos, 
                        horários, notificações, posts, comentários e quadros de progresso dos projetos.
                        
                        ## 🔐 Autenticação
                        Esta API usa **cookies HttpOnly** para autenticação JWT. 
                        
                        **Como testar:**
                        1. Faça login pelo endpoint `/api/auth/login` (sem precisar clicar no cadeado)
                        2. O cookie será armazenado automaticamente pelo navegador
                        3. Os próximos requests para endpoints protegidos usarão o cookie automaticamente
                        
                        ## 📋 Legenda de Permissões:
                        - **Público**: Não requer autenticação
                        - 🔒 **Autenticado**: Requer login (ADMIN ou STUDENT)
                        - 🔐 **Admin**: Apenas administradores
                        
                        **Nota:** Devido à limitação do Swagger UI com cookies HttpOnly, 
                        recomendamos usar Postman ou Thunder Client para testes mais completos.
                        """,
                contact = @Contact(name = "+praTiHub", email = "maisprati.hub@gmail.com")
        ),
        // Aplica segurança por padrão em todos os endpoints
        security = @SecurityRequirement(name = "cookieAuth")
)
@SecurityScheme(
        name = "cookieAuth",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.COOKIE,
        paramName = "access_token",
        description = """
                JWT token armazenado em cookie HttpOnly.
                
                **Para testar no Swagger:**
                1. NÃO clique no botão "Authorize" (cadeado)
                2. Vá direto ao endpoint POST /api/auth/login
                3. Insira email e senha
                4. Execute o login
                5. O cookie será salvo automaticamente
                6. Teste os endpoints protegidos normalmente
                """
)
public class OpenApiConfig {
}
