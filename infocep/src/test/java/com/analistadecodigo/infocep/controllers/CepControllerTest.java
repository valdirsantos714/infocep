package com.analistadecodigo.infocep.controllers;

import com.analistadecodigo.infocep.dtos.CepResponseDto;
import com.analistadecodigo.infocep.services.CepService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do CepController")
class CepControllerTest {

    @Mock
    private CepService cepService;

    @InjectMocks
    private CepController cepController;

    private CepResponseDto cepResponseDto;

    @BeforeEach
    void setUp() {
        cepResponseDto = CepResponseDto.builder()
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
    @DisplayName("Deve buscar CEP com sucesso")
    void testBuscarCepComSucesso() {
        // Arrange
        String cep = "01310100";
        when(cepService.buscarPorCep(cep)).thenReturn(cepResponseDto);

        // Act
        CepResponseDto resultado = cepController.buscarCep(cep);

        // Assert
        assertNotNull(resultado);
        assertEquals("01310100", resultado.getCep());
        assertEquals("Avenida Paulista", resultado.getLogradouro());
        assertEquals("São Paulo", resultado.getLocalidade());
        assertEquals("SP", resultado.getUf());
        verify(cepService, times(1)).buscarPorCep(cep);
    }

    @Test
    @DisplayName("Deve retornar erro quando CEP inválido")
    void testBuscarCepInvalido() {
        // Arrange
        String cepInvalido = "00000000";
        when(cepService.buscarPorCep(cepInvalido))
                .thenThrow(new IllegalArgumentException("CEP inválido"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            cepController.buscarCep(cepInvalido);
        });

        verify(cepService, times(1)).buscarPorCep(cepInvalido);
    }

    @Test
    @DisplayName("Deve retornar fallback quando serviço indisponível")
    void testBuscarCepComFallback() {
        // Arrange
        String cep = "01310100";
        CepResponseDto fallbackResponse = CepResponseDto.builder()
                .cep(cep)
                .logradouro("Indisponível")
                .bairro("Indisponível")
                .localidade("Indisponível")
                .uf("NA")
                .build();

        when(cepService.buscarPorCep(cep)).thenReturn(fallbackResponse);

        // Act
        CepResponseDto resultado = cepController.buscarCep(cep);

        // Assert
        assertNotNull(resultado);
        assertEquals("Indisponível", resultado.getLogradouro());
        assertEquals("Indisponível", resultado.getLocalidade());
        assertEquals("NA", resultado.getUf());
        verify(cepService, times(1)).buscarPorCep(cep);
    }

    @Test
    @DisplayName("Deve validar chamada do serviço")
    void testValidarChamadaServico() {
        // Arrange
        String cep = "12345678";
        when(cepService.buscarPorCep(anyString())).thenReturn(cepResponseDto);

        // Act
        cepController.buscarCep(cep);

        // Assert
        verify(cepService, times(1)).buscarPorCep(cep);
    }
}