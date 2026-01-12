package com.analistadecodigo.infocep.controllers;

import com.analistadecodigo.infocep.dtos.CepResponseDto;
import com.analistadecodigo.infocep.services.CepService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/infocep")
public class CepController {

    private static final Logger logger = LoggerFactory.getLogger(CepController.class);
    private final CepService cepService;

    public CepController(CepService cepService) {
        this.cepService = cepService;
    }

    @GetMapping("/{cep}")
    public CepResponseDto buscarCep(@PathVariable String cep) {
        logger.info("CepController - buscarCep: Iniciando busca de CEP: {}", cep);

        try {
            CepResponseDto response = cepService.buscarPorCep(cep);
            logger.info("CepController - buscarCep: Busca de CEP {} concluída com sucesso. Endereço: {}, {}",
                    cep, response.getLogradouro(), response.getLocalidade());
            return response;
        } catch (Exception e) {
            logger.warn("CepController - buscarCep - Exception: Erro ao buscar CEP: {} - Erro: {}", cep, e.getMessage());
            throw e;
        }
    }
}

