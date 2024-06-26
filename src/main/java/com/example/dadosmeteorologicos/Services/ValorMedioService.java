package com.example.dadosmeteorologicos.Services;

import java.sql.Connection;
import java.sql.Date;
import java.util.List;

import com.example.dadosmeteorologicos.db.ValorMedioSQL;
import com.example.dadosmeteorologicos.model.Cidade;
import com.example.dadosmeteorologicos.model.RegistroValorMedio;
  


public class ValorMedioService {

  private ValorMedioSQL banco;

  public ValorMedioService(){
    this.banco = new ValorMedioSQL();
  }

  public ValorMedioService(Connection conn){
    this.banco = new ValorMedioSQL(conn);
  }
  
  // Método para obter uma lista de cidades do banco de dados
  public List<Cidade> getCidadesDoBancoDeDados(){
    // Chama o método getCidadesMenuItem para obter a lista de cidades
    List<Cidade> cidades = banco.getCidadesMenuItem();
    // Fecha a conexão com o banco de dados
    banco.fecharConexao();
    // Retorna a lista de cidades
    return cidades;
  }
  
  // Método para consultar uma cidade por ID e datas
  public List<RegistroValorMedio> getValorMedio(String siglaCidade, Date dataInicial, Date dataFinal){
    // Chama o método getRelatorioValorMedio para obter a lista de registros da cidade
    List<RegistroValorMedio> listaRegistrosBD = banco.getRelatorioValorMedio(siglaCidade, dataInicial, dataFinal);
    // for (RegistroValorMedio registro : listaRegistrosBD) {
    //   System.out.println(registro.getData() + " " + registro.getEstacao() + " " + registro.getSiglaCidade() + " " + registro.getValorMedioInfos());
    // }
    banco.fecharConexao();
    // Retorna a lista de registros da cidade
    return listaRegistrosBD;
  }

}
