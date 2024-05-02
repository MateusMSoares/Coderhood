package com.example.dadosmeteorologicos.Services;

import java.util.List;

import com.example.dadosmeteorologicos.db.EstacaoSQL;
import com.example.dadosmeteorologicos.model.Estacao;

public class EstacaoService {

   public List<Estacao> buscaEstacao(){
      EstacaoSQL estacaoSQL = new EstacaoSQL();
      List<Estacao> listaEstacao = estacaoSQL.buscaEstacaoBanco();
      estacaoSQL.fecharConexao();
      return listaEstacao;
   }

   public Boolean deletarEstacao(int id, String numero){
      EstacaoSQL estacaoSQL = new EstacaoSQL();
      estacaoSQL.deletarEstacaoBanco(id, numero);
      estacaoSQL.fecharConexao();
      return true;
   }

   // public Boolean adicionarNovaEstacao(String siglaCidadeNovaEstacao, String numeroNovaEstacao){
   //    EstacaoSQL estacaoSQL = new EstacaoSQL();
   //    estacaoSQL.adicionarEstacaoBanco(siglaCidadeNovaEstacao, numeroNovaEstacao);
   //    estacaoSQL.fecharConexao();
   //    return true;
   // }

   public static Boolean siglaValida(String sigla){
      EstacaoSQL estacaoSQL = new EstacaoSQL();
      Boolean siglaValida = estacaoSQL.siglaValidaBanco(sigla);
      estacaoSQL.fecharConexao();
      return siglaValida;
   }

   public Void criarEstacao(String estacao, String sigla){
      EstacaoSQL estacaoSQL = new EstacaoSQL();
      estacaoSQL.adicionarEstacaoBanco(estacao, sigla);
      estacaoSQL.fecharConexao();
      return null;
   }

}



