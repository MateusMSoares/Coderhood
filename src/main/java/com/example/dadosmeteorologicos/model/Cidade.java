package com.example.dadosmeteorologicos.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Cidade {
    private String nome;
    private String sigla;
    private int id;
    CidadeDetalhes cidadeDetalhes;
        
    public Cidade(String nome, String sigla) {
        this.nome = nome;
        this.sigla = sigla;
    }

    public Cidade(int id, String nome, String sigla) {
        this.id = id;
        this.nome = nome;
        this.sigla = sigla;
    }

    public Cidade(String nome, String sigla, CidadeDetalhes cidadeDetalhes) {
        this.nome = nome;
        this.sigla = sigla;
        this.cidadeDetalhes = cidadeDetalhes;
    }

}
