package com.analistadecodigo.infocep.services;

import com.analistadecodigo.infocep.dtos.CepResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários do CepService")
class CepServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    private CepService cepService;

    private CepResponseDto cepResponseDtoValido;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        cepService = new CepService(restTemplateBuilder);

        cepResponseDtoValido = CepResponseDto.builder()
                .cep("01310100")
                .logradouro("Avenida Paulista")
                .complemento("lado par")
                .bairro("Bela Vista")
                .localidade("São Paulo")
                .uf("SP")
                .ibge("3550308")
                .gia("1004947")
                .ddd("11")
                .siafi("7107")
                .build();
    }

    @Test
    @DisplayName("Deve buscar CEP com sucesso via API ViaCEP")
    void testBuscarPorCepComSucesso() {
        // Arrange
        String cep = "01310100";
        String urlEsperada = "https://viacep.com.br/ws/01310100/json/";
        when(restTemplate.getForObject(urlEsperada, CepResponseDto.class))
                .thenReturn(cepResponseDtoValido);

        // Act
        CepResponseDto resultado = cepService.buscarPorCep(cep);

        // Assert
        assertNotNull(resultado);
        assertEquals("01310100", resultado.getCep());
        assertEquals("Avenida Paulista", resultado.getLogradouro());
        assertEquals("São Paulo", resultado.getLocalidade());
        assertEquals("SP", resultado.getUf());
        verify(restTemplate, times(1)).getForObject(urlEsperada, CepResponseDto.class);
    }

    @Test
    @DisplayName("Deve construir URL corretamente com CEP")
    void testConstruirUrlCorretamente() {
        // Arrange
        String cep = "12345678";
        String urlEsperada = "https://viacep.com.br/ws/12345678/json/";
        when(restTemplate.getForObject(urlEsperada, CepResponseDto.class))
                .thenReturn(cepResponseDtoValido);

        // Act
        cepService.buscarPorCep(cep);

        // Assert
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).getForObject(urlCaptor.capture(), eq(CepResponseDto.class));
        assertEquals(urlEsperada, urlCaptor.getValue());
    }

    @Test
    @DisplayName("Deve retornar objeto com todos os campos preenchidos")
    void testValidarRespostaComTodosCamposPreenchidos() {
        // Arrange
        String cep = "01310100";
        String url = "https://viacep.com.br/ws/01310100/json/";
        when(restTemplate.getForObject(url, CepResponseDto.class))
                .thenReturn(cepResponseDtoValido);

        // Act
        CepResponseDto resultado = cepService.buscarPorCep(cep);

        // Assert
        assertNotNull(resultado);
        assertNotNull(resultado.getCep());
        assertNotNull(resultado.getLogradouro());
        assertNotNull(resultado.getComplemento());
        assertNotNull(resultado.getBairro());
        assertNotNull(resultado.getLocalidade());
        assertNotNull(resultado.getUf());
        assertNotNull(resultado.getIbge());
        assertNotNull(resultado.getGia());
        assertNotNull(resultado.getDdd());
        assertNotNull(resultado.getSiafi());
    }

    @Test
    @DisplayName("Deve validar valores específicos dos campos")
    void testValidarValoresEspecificosDosCampos() {
        // Arrange
        String cep = "01310100";
        String url = "https://viacep.com.br/ws/01310100/json/";
        when(restTemplate.getForObject(url, CepResponseDto.class))
                .thenReturn(cepResponseDtoValido);

        // Act
        CepResponseDto resultado = cepService.buscarPorCep(cep);

        // Assert
        assertEquals("01310100", resultado.getCep());
        assertEquals("Avenida Paulista", resultado.getLogradouro());
        assertEquals("lado par", resultado.getComplemento());
        assertEquals("Bela Vista", resultado.getBairro());
        assertEquals("São Paulo", resultado.getLocalidade());
        assertEquals("SP", resultado.getUf());
        assertEquals("3550308", resultado.getIbge());
        assertEquals("1004947", resultado.getGia());
        assertEquals("11", resultado.getDdd());
        assertEquals("7107", resultado.getSiafi());
    }

    @Test
    @DisplayName("Deve retornar null quando CEP não encontrado")
    void testBuscarCepNaoEncontrado() {
        // Arrange
        String cepNaoEncontrado = "00000000";
        String url = "https://viacep.com.br/ws/00000000/json/";
        when(restTemplate.getForObject(url, CepResponseDto.class))
                .thenReturn(null);

        // Act
        CepResponseDto resultado = cepService.buscarPorCep(cepNaoEncontrado);

        // Assert
        assertNull(resultado);
        verify(restTemplate, times(1)).getForObject(url, CepResponseDto.class);
    }

    @Test
    @DisplayName("Deve lançar exceção quando API indisponível (500)")
    void testBuscarCepComErroDeServidor500() {
        // Arrange
        String cep = "01310100";
        String url = "https://viacep.com.br/ws/01310100/json/";
        HttpStatusCode statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
        when(restTemplate.getForObject(url, CepResponseDto.class))
                .thenThrow(new HttpServerErrorException(statusCode, "Erro 500"));

        // Act & Assert
        assertThrows(HttpServerErrorException.class, () -> {
            cepService.buscarPorCep(cep);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção quando CEP inválido (400)")
    void testBuscarCepInvalidoComErro400() {
        // Arrange
        String cep = "INVALIDO";
        String url = "https://viacep.com.br/ws/INVALIDO/json/";
        HttpStatusCode statusCode = HttpStatus.BAD_REQUEST;
        when(restTemplate.getForObject(url, CepResponseDto.class))
                .thenThrow(new HttpClientErrorException(statusCode, "Erro 400"));

        // Act & Assert
        assertThrows(HttpClientErrorException.class, () -> {
            cepService.buscarPorCep(cep);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção quando timeout na conexão")
    void testBuscarCepComTimeout() {
        // Arrange
        String cep = "01310100";
        String url = "https://viacep.com.br/ws/01310100/json/";
        when(restTemplate.getForObject(url, CepResponseDto.class))
                .thenThrow(new ResourceAccessException("Timeout"));

        // Act & Assert
        assertThrows(ResourceAccessException.class, () -> {
            cepService.buscarPorCep(cep);
        });
    }

    @Test
    @DisplayName("Deve lançar exceção genérica quando erro desconhecido")
    void testBuscarCepComErroDesconhecido() {
        // Arrange
        String cep = "01310100";
        String url = "https://viacep.com.br/ws/01310100/json/";
        when(restTemplate.getForObject(url, CepResponseDto.class))
                .thenThrow(new RuntimeException("Erro desconhecido"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            cepService.buscarPorCep(cep);
        });
    }

    @Test
    @DisplayName("Deve construir RestTemplate na inicialização")
    void testConstruirRestTemplateNaInicializacao() {
        // Assert
        verify(restTemplateBuilder, times(1)).build();
    }

    @Test
    @DisplayName("Deve fazer múltiplas requisições sem estado")
    void testMultiplasRequisicoesSemEstado() {
        // Arrange
        String cep1 = "01310100";
        String cep2 = "20040020";
        String url1 = "https://viacep.com.br/ws/01310100/json/";
        String url2 = "https://viacep.com.br/ws/20040020/json/";

        CepResponseDto response1 = CepResponseDto.builder()
                .cep("01310100")
                .localidade("São Paulo")
                .uf("SP")
                .build();

        CepResponseDto response2 = CepResponseDto.builder()
                .cep("20040020")
                .localidade("Rio de Janeiro")
                .uf("RJ")
                .build();

        when(restTemplate.getForObject(url1, CepResponseDto.class)).thenReturn(response1);
        when(restTemplate.getForObject(url2, CepResponseDto.class)).thenReturn(response2);

        // Act
        CepResponseDto resultado1 = cepService.buscarPorCep(cep1);
        CepResponseDto resultado2 = cepService.buscarPorCep(cep2);

        // Assert
        assertNotNull(resultado1);
        assertNotNull(resultado2);
        assertEquals("São Paulo", resultado1.getLocalidade());
        assertEquals("Rio de Janeiro", resultado2.getLocalidade());
        verify(restTemplate, times(1)).getForObject(url1, CepResponseDto.class);
        verify(restTemplate, times(1)).getForObject(url2, CepResponseDto.class);
    }

    @Test
    @DisplayName("Deve validar chamada correta do RestTemplate")
    void testValidarChamadaDoRestTemplate() {
        // Arrange
        String cep = "01310100";
        String url = "https://viacep.com.br/ws/01310100/json/";
        when(restTemplate.getForObject(url, CepResponseDto.class))
                .thenReturn(cepResponseDtoValido);

        // Act
        cepService.buscarPorCep(cep);

        // Assert
        verify(restTemplate, times(1)).getForObject(url, CepResponseDto.class);
        verify(restTemplate, atLeastOnce()).getForObject(anyString(), any());
    }

    @Test
    @DisplayName("Deve não chamar RestTemplate mais de uma vez por requisição")
    void testNaoChamarRestTemplateMultiplaVezes() {
        // Arrange
        String cep = "01310100";
        String url = "https://viacep.com.br/ws/01310100/json/";
        when(restTemplate.getForObject(url, CepResponseDto.class))
                .thenReturn(cepResponseDtoValido);

        // Act
        cepService.buscarPorCep(cep);

        // Assert
        verify(restTemplate, times(1)).getForObject(anyString(), any());
    }

    @ParameterizedTest(name = "CEP: {0}")
    @ValueSource(strings = {"01310100", "20040020", "12345678", "99999999"})
    @DisplayName("Deve buscar múltiplos CEPs válidos")
    void testBuscarMultiplosCepsValidos(String cep) {
        // Arrange
        String url = "https://viacep.com.br/ws/" + cep + "/json/";
        when(restTemplate.getForObject(url, CepResponseDto.class))
                .thenReturn(cepResponseDtoValido);

        // Act
        CepResponseDto resultado = cepService.buscarPorCep(cep);

        // Assert
        assertNotNull(resultado);
        assertEquals(cepResponseDtoValido, resultado);
    }

    @Test
    @DisplayName("Deve retornar resposta diferente para CEPs diferentes")
    void testRetornarRespostaDiferenteParaCepsDiferentes() {
        // Arrange
        String url1 = "https://viacep.com.br/ws/01310100/json/";
        String url2 = "https://viacep.com.br/ws/20040020/json/";

        CepResponseDto response1 = CepResponseDto.builder().cep("01310100").uf("SP").build();
        CepResponseDto response2 = CepResponseDto.builder().cep("20040020").uf("RJ").build();

        when(restTemplate.getForObject(url1, CepResponseDto.class)).thenReturn(response1);
        when(restTemplate.getForObject(url2, CepResponseDto.class)).thenReturn(response2);

        // Act
        CepResponseDto resultado1 = cepService.buscarPorCep("01310100");
        CepResponseDto resultado2 = cepService.buscarPorCep("20040020");

        // Assert
        assertNotEquals(resultado1, resultado2);
        assertEquals("SP", resultado1.getUf());
        assertEquals("RJ", resultado2.getUf());
    }

    @Test
    @DisplayName("Deve usar classe correta para desserializar resposta")
    void testUsarClasseCorretaParaDesserializar() {
        // Arrange
        String cep = "01310100";
        String url = "https://viacep.com.br/ws/01310100/json/";
        when(restTemplate.getForObject(url, CepResponseDto.class))
                .thenReturn(cepResponseDtoValido);

        // Act
        cepService.buscarPorCep(cep);

        // Assert
        ArgumentCaptor<Class<?>> classCaptor = ArgumentCaptor.forClass(Class.class);
        verify(restTemplate).getForObject(anyString(), classCaptor.capture());
        assertEquals(CepResponseDto.class, classCaptor.getValue());
    }

    @Test
    @DisplayName("Deve ignorar campos extras na resposta da API")
    void testIgnorarCamposExtrasNaResposta() {
        // Arrange
        String cep = "01310100";
        String url = "https://viacep.com.br/ws/01310100/json/";

        // Simular resposta com campos extras (que serão ignorados na desserialização)
        CepResponseDto response = CepResponseDto.builder()
                .cep("01310100")
                .logradouro("Avenida Paulista")
                .build();

        when(restTemplate.getForObject(url, CepResponseDto.class))
                .thenReturn(response);

        // Act
        CepResponseDto resultado = cepService.buscarPorCep(cep);

        // Assert
        assertNotNull(resultado);
        assertEquals("01310100", resultado.getCep());
    }

    @Test
    @DisplayName("Deve validar tipo de retorno")
    void testValidarTipoDeRetorno() {
        // Arrange
        String cep = "01310100";
        String url = "https://viacep.com.br/ws/01310100/json/";
        when(restTemplate.getForObject(url, CepResponseDto.class))
                .thenReturn(cepResponseDtoValido);

        // Act
        Object resultado = cepService.buscarPorCep(cep);

        // Assert
        assertInstanceOf(CepResponseDto.class, resultado);
    }

    @Test
    @DisplayName("Deve usar método getForObject do RestTemplate")
    void testUsarMetodoGetForObject() {
        // Arrange
        String cep = "01310100";
        String url = "https://viacep.com.br/ws/01310100/json/";
        when(restTemplate.getForObject(url, CepResponseDto.class))
                .thenReturn(cepResponseDtoValido);

        // Act
        cepService.buscarPorCep(cep);

        // Assert
        verify(restTemplate).getForObject(url, CepResponseDto.class);
        verify(restTemplate, never()).getForEntity(anyString(), any());
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }
}

