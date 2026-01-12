package com.analistadecodigo.infocep.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CepResponseDto {
    private String cep;
    private String logradouro;
    private String complemento;
    private String bairro;
    private String localidade;
    private String uf;
    private String ibge;
    private String gia;
    private String ddd;
    private String siafi;
}

