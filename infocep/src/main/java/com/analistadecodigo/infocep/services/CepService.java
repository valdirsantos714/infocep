package com.analistadecodigo.infocep.services;

import com.analistadecodigo.infocep.dtos.CepResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CepService {

    private static final Logger logger = LoggerFactory.getLogger(CepService.class);
    private static final String VIA_CEP_CB = "viaCep";

    private final RestTemplate restTemplate;

    public CepService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @CircuitBreaker(name = VIA_CEP_CB, fallbackMethod = "buscarPorCepFallback")
    public CepResponseDto buscarPorCep(String cep) {
        String url = "https://viacep.com.br/ws/" + cep + "/json/";
        logger.info("CepService - buscarPorCep: Realizando requisição para ViaCEP na URL: {}", url);

        try {
            CepResponseDto response = restTemplate.getForObject(url, CepResponseDto.class);
            logger.info("CepService - buscarPorCep: Resposta recebida com sucesso para CEP: {}", cep);
            return response;
        } catch (Exception e) {
            logger.warn("CepService - buscarPorCep - Exception: Erro ao fazer requisição para ViaCEP - CEP: {} - Erro: {}", cep, e.getMessage());
            throw e;
        }
    }

    /**
     * Fallback chamado quando:
     * - API fora
     * - Timeout
     * - Circuit aberto
     */
    private CepResponseDto buscarPorCepFallback(String cep, Throwable throwable) {
        logger.warn("CepService - buscarPorCepFallback: Fallback acionado para CEP: {} - Motivo: {}", cep, throwable.getMessage());
        logger.info("CepService - buscarPorCepFallback: Retornando dados padrão de fallback para CEP: {}", cep);

        return CepResponseDto.builder()
                .cep(cep)
                .logradouro("Indisponível")
                .bairro("Indisponível")
                .localidade("Indisponível")
                .uf("NA")
                .build();
    }
}

