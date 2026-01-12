# InfoCEP - API de Informações de CEP

Uma API Spring Boot moderna e resiliente que fornece informações detalhadas sobre endereços a partir do CEP brasileiro, integrando-se com a API ViaCEP com padrões robustos de tratamento de falhas e observabilidade.

---

## 📋 Visão Geral do Projeto

O **InfoCEP** é uma aplicação backend que consulta dados de endereços através do CEP (Código de Endereçamento Postal), implementando boas práticas de desenvolvimento enterprise e garantindo alta disponibilidade através de padrões de resiliência.

---

## 🚀 Tecnologias Utilizadas

### **Core Framework**
- **Spring Boot 4.0.1** - Framework web moderno e produção-ready
- **Java 17** - LTS com recursos modernos (records, sealed classes, pattern matching)
- **Maven 3.9.6** - Gerenciamento de dependências e build

### **Resiliência e Tolerância a Falhas**
- **Resilience4j Circuit Breaker** - Implementação do padrão Circuit Breaker para proteção contra cascata de falhas
  - Janela deslizante de 10 chamadas
  - Taxa de falha de 50% para abertura
  - Timeout de 30 segundos em estado aberto
  - Transição automática para semi-aberto
  - Fallback automático com dados padrão

### **Cliente HTTP**
- **Spring Boot RestClient** - Cliente HTTP moderno (substitui RestTemplate)
- **RestTemplate Builder** - Configuração fluente de cliente HTTP

### **Observabilidade e Monitoring**
- **Spring Boot Actuator** - Endpoints de health check e métricas
- **Health Probes** - Liveness e readiness probes para Kubernetes-ready
- **Metrics** - Coleta de métricas da aplicação

### **Validação e Data Transfer**
- **Spring Validation** - Validação de dados com Jakarta Bean Validation
- **Lombok** - Redução de boilerplate com anotações (@Data, @Builder)

### **Containerização**
- **Docker** - Multi-stage build otimizado
  - Build stage com Maven e Java 17
  - Runtime stage com Alpine (imagem otimizada)
  - Usuário non-root para segurança

---

## ⚙️ Arquitetura e Padrões de Design

### **Padrões Implementados**

#### 1. **Circuit Breaker Pattern**
```
Normal → Falha consecutivas → OPEN (bloqueia chamadas)
         ↓                     ↓
      Threshold             Timeout
                            ↓
                        HALF-OPEN
                            ↓
                    Tenta requisição
                     ↙              ↘
                Sucesso             Falha
                   ↓                   ↓
                CLOSED              OPEN
```

O Circuit Breaker monitora a ViaCEP API e:
- **Bloqueia chamadas** quando a taxa de erro excede 50%
- **Ativa fallback** retornando dados padrão
- **Recupera automaticamente** após 30 segundos

#### 2. **Fallback Pattern**
- Retorna resposta padrão quando ViaCEP está indisponível
- Mantém aplicação funcional mesmo com falhas de dependência externa
- Melhora UX degradando graciosamente

#### 3. **REST API Pattern**
- Endpoints RESTful com convenção padrão
- HTTP methods apropriados (GET para consultas)
- DTOs para transferência de dados

#### 4. **Dependency Injection**
- Constructor injection (melhor para testes)
- Gerenciamento automático pelo Spring

### **Estrutura do Projeto**

```
infocep/
├── src/main/java/com/analistadecodigo/infocep/
│   ├── InfocepApplication.java          # Main class
│   ├── controllers/
│   │   └── CepController.java            # Endpoint REST
│   ├── services/
│   │   └── CepService.java               # Lógica de negócio + Circuit Breaker
│   └── dtos/
│       ├── CepRequestDto.java            # DTO de entrada
│       └── CepResponseDto.java           # DTO de resposta
├── src/test/java/                        # Testes unitários
├── src/main/resources/
│   └── application.yaml                  # Configuração Resilience4j
├── Dockerfile                            # Multi-stage build
├── pom.xml                               # Dependências Maven
└── mvnw / mvnw.cmd                       # Maven Wrapper
```

---

## 🏗️ CI/CD e DevOps

### **GitHub Actions Workflows**

#### 1. **Build Backend (Automatizado)**
- **Trigger**: Push e Pull Request na branch `main`
- **Ações**:
  - Build da aplicação Java
  - Execução de testes
  - Build de imagem Docker
  - Push para Docker Hub **apenas em push na main** (produção)

```yaml
Triggers: push e PR → Maven build → Testes → Docker build → 
→ Push condicional (apenas main)
```

**Arquivo**: `.github/workflows/build-backend.yml`

#### 2. **Manual Verification (Manual)**
- **Trigger**: `workflow_dispatch` (botão manual no GitHub)
- **Opções**:
  - `Build Only` - Apenas compila e testa
  - `Build + Push to Docker Hub` - Build + publicação da imagem
- **Caso de uso**: Deploy manual, verificações antes de CI automático

```yaml
Manual trigger → Modo selecionado → Build + Push condicional
```

**Arquivo**: `.github/workflows/manual-verification.yml`

### **Workflows Reutilizáveis**
- Utiliza workflows compartilhados do repositório: `valdirsantos714/reusable_workflows`
- Permite padronização entre múltiplos projetos
- Mantém DRY principle em pipelines CI/CD

---

## 📊 Configuração Resilience4j

```yaml
resilience4j:
  circuitbreaker:
    instances:
      viaCep:
        slidingWindowSize: 10              # Monitora últimas 10 chamadas
        minimumNumberOfCalls: 5            # Mín. chamadas para avaliar
        failureRateThreshold: 50           # Taxa de falha para abrir
        waitDurationInOpenState: 30s       # Tempo antes de tentar half-open
        permittedNumberOfCallsInHalfOpenState: 3  # Tentativas em half-open
        automaticTransitionFromOpenToHalfOpenEnabled: true
        slowCallDurationThreshold: 2s      # Considera lenta após 2s
        slowCallRateThreshold: 50          # Taxa de chamadas lentas
        recordExceptions:                  # Exceções que disparam circuit
          - org.springframework.web.client.HttpServerErrorException
          - java.io.IOException
          - java.net.ConnectException
```

---

## 🩺 Observabilidade e Health Check

### **Endpoints Atualizados**
- `/actuator/health` - Status geral da aplicação
- `/actuator/health/liveness` - Verificação de vitalidade
- `/actuator/health/readiness` - Verificação de prontidão
- `/actuator/metrics` - Métricas coletadas

**Ideal para**:
- Kubernetes probes
- Load balancer health checks
- Monitoramento em tempo real

---

## 🐳 Docker

### **Multi-stage Build**

**Stage 1: Build**
```dockerfile
FROM maven:3.9.6-eclipse-temurin-17
- Compila código
- Executa testes
- Gera artifact (JAR)
```

**Stage 2: Runtime**
```dockerfile
FROM eclipse-temurin:17-jre-alpine
- Somente JRE (sem ferramentas de build)
- Alpine Linux (imagem mínima ~100MB)
- Usuário non-root (segurança)
- EXPOSE 8080
```

**Vantagens**:
- ✅ Reduz tamanho da imagem (~80%)
- ✅ Sem ferramentas de build em produção
- ✅ User non-root por segurança
- ✅ Reproducible builds

---

## 📝 Logging e Debugging

### **Logs Estruturados**
A aplicação implementa logs em diferentes níveis:

```java
logger.info()   // Eventos importantes (startup, requisições)
logger.warn()   // Fallback acionado
logger.error()  // Erros não tratados
```

**Exemplo de rastreamento**:
```
INFO  - buscarPorCep: Iniciando busca de CEP: 12345-678
INFO  - buscarPorCep: Requisição para ViaCEP na URL: https://viacep.com.br/...
INFO  - buscarPorCep: Resposta recebida com sucesso
```

---

## 🧪 Testes

### **Cobertura de Testes**
- `CepControllerTest` - Testes da API REST
- `CepServiceTest` - Testes da lógica de negócio e Circuit Breaker
- `InfocepApplicationTests` - Testes de contexto

**Frameworks**:
- JUnit 5 (incluído no Spring Boot)
- Spring Boot Test (MockMvc, MockRestTemplate)

---

## 🚦 Como Executar

### **Pré-requisitos**
- Java 17+
- Maven 3.9+ (ou usar mvnw)
- Docker (opcional)

### **Executar Localmente**

```bash
# Com Maven Wrapper (Windows)
mvnw.cmd spring-boot:run

# Com Maven Wrapper (Linux/Mac)
./mvnw spring-boot:run

# Ou com Maven instalado
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

### **Exemplo de Requisição**

```bash
curl "http://localhost:8080/infocep/01310100"
```

**Resposta com sucesso**:
```json
{
  "cep": "01310-100",
  "logradouro": "Avenida Paulista",
  "complemento": "lado ímpar",
  "bairro": "Bela Vista",
  "localidade": "São Paulo",
  "uf": "SP",
  "ibge": "3550308",
  "gia": "1004947",
  "ddd": "11",
  "siafi": "7107"
}
```

**Resposta em fallback** (API indisponível):
```json
{
  "cep": "12345-678",
  "logradouro": "Indisponível",
  "bairro": "Indisponível",
  "localidade": "Indisponível",
  "uf": "NA"
}
```

### **Executar com Docker**

```bash
# Build da imagem
docker build -f infocep/Dockerfile -t infocep-backend:latest ./infocep

# Executar container
docker run -p 8080:8080 infocep-backend:latest
```

---

## 🔐 Segurança

### **Implementações de Segurança**

✅ **Container Security**
- Usuário non-root em runtime
- Imagem Alpine (menor superfície de ataque)
- Sem ferramentas de build em produção

✅ **Input Validation**
- Validação de dados com Jakarta Validation

✅ **Dependency Management**
- Spring Boot gerencia automaticamente CVE patches
- Maven ensures reproducible builds

✅ **Logging Seguro**
- Logs estruturados sem dados sensíveis
- Integração com observabilidade

---

## 📈 Performance e Scalability

### **Otimizações**

| Aspecto | Implementação |
|---------|---------------|
| **Resiliência** | Circuit Breaker previne cascata de falhas |
| **Timeout** | Slow call detection (2s threshold) |
| **Caching** | Fallback cache em memória |
| **Health Checks** | Kubernetes-ready probes |
| **Containerização** | Alpine + multi-stage para deployments rápidos |

### **Métricas Monitoradas**
- Taxa de sucesso/falha
- Tempo de resposta
- Status do Circuit Breaker
- Saúde geral da aplicação

---

## 🛠️ Deploy Contínuo

### **Pipeline de Deploy**

```
1. Developer push na main
   ↓
2. GitHub Actions triggers build-backend.yml
   ↓
3. Maven compila e testa
   ↓
4. Docker image é buildada
   ↓
5. Image pushida para Docker Hub (apenas em push)
   ↓
6. Pronto para deploy em produção
```

### **Manual Deploy**
Através do workflow `manual-verification.yml`, é possível realizar deploys e builds manuais com verificação prévia.

---

## 🤝 Contribuindo

1. Clone o repositório
2. Crie uma branch feature: `git checkout -b feature/sua-feature`
3. Commit suas mudanças: `git commit -m 'Adiciona nova feature'`
4. Push para branch: `git push origin feature/sua-feature`
5. Abra um Pull Request
   - CI/CD será executado automaticamente
   - Revisão de código é obrigatória antes de merge

---

## 📚 Referências e Recursos

### **Documentação Oficial**
- [Spring Boot 4.0 Docs](https://spring.io/projects/spring-boot)
- [Spring Cloud Resilience4j](https://spring.io/projects/spring-cloud-resilience4j)
- [Resilience4j Circuit Breaker](https://resilience4j.readme.io/docs/circuitbreaker)
- [Java 17 Features](https://openjdk.java.net/projects/jdk/17/)

### **Padrões de Design**
- [Circuit Breaker Pattern - Martin Fowler](https://martinfowler.com/bliki/CircuitBreaker.html)
- [Fallback Pattern](https://docs.microsoft.com/en-us/azure/architecture/patterns/bulkhead)

### **DevOps**
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)

---


## ✨ Highlights do Projeto

- ✅ **Java 17 LTS** - Segurança e performance
- ✅ **Circuit Breaker** - Resiliência contra falhas
- ✅ **Observabilidade** - Health checks e métricas
- ✅ **CI/CD Avançado** - Workflows automáticos e manuais
- ✅ **Docker Otimizado** - Multi-stage builds
- ✅ **Código Testável** - Estrutura preparada para testes
- ✅ **Logging Estruturado** - Rastreabilidade completa
- ✅ **Security First** - Boas práticas de segurança