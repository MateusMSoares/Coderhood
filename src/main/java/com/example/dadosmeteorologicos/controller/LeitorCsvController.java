package com.example.dadosmeteorologicos.controller;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.apache.commons.text.WordUtils;

import com.example.dadosmeteorologicos.Services.CSVResolve;
import com.example.dadosmeteorologicos.Services.LeitorCsvService;
import com.example.dadosmeteorologicos.Services.VariavelClimaticaService;
import com.example.dadosmeteorologicos.exceptions.CSVInvalidoException;
import com.example.dadosmeteorologicos.model.Registro;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class LeitorCsvController {
    private String caminhoArquivo;

    @FXML
    protected Button salvarCsvButton;
    @FXML
    private Button selecionarArquivo;
    @FXML
    private Label infoLabel;

    private CSVResolve leitor;
    private LeitorCsvService service;

    private String siglaCidadeInserida;
    private String numeroEstacaoInserido;
    private String nomeCidadeInserido;
    private String nomeCidade;
    private String siglaCidade;
    private String numeroEstacao;
    private boolean cidadeEstacaoValida;
    private List<Registro> listaRegistro;
    private int registrosSuspeitos;
    private int[] salvoDuplicado;
    private int salvos;
    private int duplicados;


    @FXML
    public void initialize() {
        System.out.println("Iniciado Leitor CSV");
        infoLabel.setVisible(false);
        salvarCsvButton.setVisible(false);
        Platform.runLater(() -> {
            VariavelClimaticaService variavelClimaticaService = new VariavelClimaticaService();
            if(variavelClimaticaService.celulasDaTabelaEstaoNulas()){
                selecionarArquivo.setDisable(true);
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Configuração necessária");
                alert.setHeaderText(null);
                alert.setContentText("Insira as faixas de valores na aba de configuração.");
                alert.showAndWait();
            }
        });
        service = new LeitorCsvService();
    }

    @FXML
    public void selecionarCsv(ActionEvent event) {
        // Obtém a referência do Stage atual
        Stage stage = (Stage) selecionarArquivo.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar arquivo CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos CSV", "*.csv"));
        // Passa a referência do Stage para o método
        File CsvEntrada = fileChooser.showOpenDialog(stage); 
        caminhoArquivo = CsvEntrada.getAbsolutePath();
        if (caminhoArquivo != null) {
            System.out.println("Arquivo selecionado: " + caminhoArquivo);
            System.out.println("Arquivo selecionado: " + caminhoArquivo);
       
        validarCSV();

        if (cidadeEstacaoValida) {
            adquirirInfosCSV();
            atualizarInformacoes();
            salvarCsvButton.setVisible(true);
        }
    }else{
        return;
    }
    }

    private void validarCSV() throws RuntimeException {
        leitor = new CSVResolve(caminhoArquivo);
        try {
            leitor.validarCSV();
            validarCidadeEstacao(leitor.isNomeInvalido());
        } catch (CSVInvalidoException e) {
            // Se a validação do CSV falhar, mostre a caixa de diálogo de erro e retorna para escolher novamente o arquivo
            dialogoCabecalhoCsvInvalido();
            return;
        } catch (RuntimeException e) {
            throw new RuntimeException("Erro ao autenticar a cidade ou a estação.");
        } 
    }

     // Botão que faz o processo de validar CSV, ler e salvar no banco
     public void salvarBanco(ActionEvent actionEvent) {
        service.criarCidade(nomeCidade, siglaCidade);
        service.criarEstacao(numeroEstacao, siglaCidade);
        salvoDuplicado = service.salvarRegistro(listaRegistro);
        salvos = salvoDuplicado[0];
        duplicados = salvoDuplicado[1];
        dialogoRegistroSalvo();
        salvarCsvButton.setVisible(false);
        
    }

    private void adquirirInfosCSV(){ 
        listaRegistro = leitor.criarRegistro();       
        registrosSuspeitos = service.registrosSuspeitos(listaRegistro);
        siglaCidade = siglaCidade.toUpperCase();
        nomeCidade = WordUtils.capitalizeFully(nomeCidade);
    }

    private void atualizarInformacoes() {
        infoLabel.setVisible(true);
        infoLabel.setText("Cidade processada: "  + nomeCidade + " - "+ siglaCidade + ".\n" +
                          "Estação processada: " + numeroEstacao + ".\n" +
                          "Total de registros: " + listaRegistro.size() + ".\n" +
                          "Registros suspeitos encontrados: " + registrosSuspeitos + ".");
    }

    private void validarCidadeEstacao(boolean nomeInvalido) throws RuntimeException{
        // Se o nome da cidade não puder ser identificado, peça ao usuário para inserir o nome da cidade
        if (nomeInvalido) {
            Optional<String[]> result = mostrarDialogoNomeInvalido();
            if (!result.isPresent()) {
                throw new RuntimeException("Operação cancelada pelo usuário.");
            }
            
            result.ifPresent(res -> {
                leitor.setSiglaCidade(siglaCidadeInserida.trim());
                leitor.setCodigoEstacao(numeroEstacaoInserido.trim());
                siglaCidade= leitor.getSiglaCidade().toUpperCase();
                numeroEstacao = leitor.getCodigoEstacao();
                validarCidadeEstacao(siglaCidade, numeroEstacao);
                validacaoNomeCidade(siglaCidade, nomeCidadeInserido.trim());
            });
        }else{
            siglaCidade= leitor.getSiglaCidade();
            numeroEstacao = leitor.getCodigoEstacao();

            validarCidadeEstacao(siglaCidade, numeroEstacao);        
            // Se a cidade não existir, peça ao usuário para inserir o nome da cidade

            if(!validarSigla(siglaCidade)){
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Nome da Cidade");
                dialog.setHeaderText("A sigla da cidade não existe no banco de dados.");
                dialog.setContentText("Insira o nome da cidade para a sigla "+ siglaCidade+ ":" );

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()){
                    validacaoNomeCidade(siglaCidade, WordUtils.capitalizeFully(result.get().trim()));  
                }else {
                    throw new RuntimeException("Operação cancelada pelo usuário.");
                }
            }                  
        }     
    }

    private void validacaoNomeCidade(String siglaCidade, String nomeCidadeInserido) {
        String nomeCidadeBanco = service.validarNomeCidadePelaSigla(siglaCidade);
        if (nomeCidadeBanco == null) { 
            nomeCidade = nomeCidadeInserido;
        }else{
            nomeCidade = nomeCidadeBanco;
        }
        System.out.println("Nome cidade: " + nomeCidade);
    }

    private boolean validarSigla(String siglaCidade) {
        String nomeCidadeBanco = service.validarNomeCidadePelaSigla(siglaCidade);
        if (nomeCidadeBanco == null) { 
            return false;
        }else{
            nomeCidade = nomeCidadeBanco;
            return true;
        }
    }

    private void validarCidadeEstacao(String siglaCidade, String numeroEstacao) throws RuntimeException {
        System.out.println("Validando cidade e estação: " + siglaCidade + " - " + numeroEstacao);
        if(!service.validarCidadeEstacao(siglaCidade, numeroEstacao)) {
            diaologoCidadeEstacaoInvalida();
            cidadeEstacaoValida = false;
            throw new RuntimeException("Estação já pertence a outra cidade.");
        }
        cidadeEstacaoValida =  true;
    }
        
    public void dialogoCabecalhoCsvInvalido(){
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("Cabeçalho do CSV fora do padrão esperado");
            alert.showAndWait();
        });
    }

    public void diaologoCidadeEstacaoInvalida(){
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("Outra cidade está associada a estação: " + numeroEstacao + ".");
            alert.showAndWait();
        });
    }

    public void dialogoRegistroSalvo(){
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Informação de Registro");
            alert.setHeaderText(null);
            alert.setContentText( "Cidade processada: " + nomeCidade + " - "+ siglaCidade + ".\n" +
            "Estação processada: " + numeroEstacao + ".\n" +
            "Total de registros processados: " + listaRegistro.size() + ".\n" +
            "Registros salvos com sucesso: " + salvos + ".\n" +
            "Registros duplicados encontrados: " + duplicados + ".\n" +
            "Registros suspeitos encontrados: " + registrosSuspeitos + ".");
            alert.showAndWait();     
        });
    }

    private Optional<String[]> mostrarDialogoNomeInvalido() {
        // Cria a caixa de diálogo personalizada
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Não foi possível identificar a cidade e a estação do arquivo CSV");
    
        // Cria os campos de entrada
        TextField CampoNomeCidade = new TextField();
        CampoNomeCidade.setPrefWidth(200);
        TextField CampoSiglaCidade = new TextField();
        CampoSiglaCidade.setPrefWidth(200);
        TextField CampoNumeroEstacao = new TextField();
        CampoNumeroEstacao.setPrefWidth(200);
    
        // Campos de entrada à caixa de diálogo
        GridPane grid = new GridPane();
        grid.setPrefWidth(400);
        grid.add(new Label("Nome da cidade:"), 0, 0);
        grid.add(CampoNomeCidade, 1, 0);
        grid.add(new Label("Sigla da cidade:"), 0, 1);
        grid.add(CampoSiglaCidade, 1, 1);
        grid.add(new Label("Número da estação:"), 0, 2);
        grid.add(CampoNumeroEstacao, 1, 2);
        dialog.getDialogPane().setContent(grid);
    
        ButtonType buttonTypeSalvar = new ButtonType("Salvar", ButtonData.OK_DONE);
        ButtonType buttonTypeSair = new ButtonType("Sair", ButtonData.CANCEL_CLOSE);
        // se apertar o buttonTypeSair ele deve fechar o dialogo e impedir o resto do codigo.
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeSalvar, buttonTypeSair);
        
        // Obtenha o botão "Salvar" e inicialmente o desabilite
        Button salvarButton = (Button) dialog.getDialogPane().lookupButton(buttonTypeSalvar);
        salvarButton.setDisable(true);

        // Ouvinte de propriedade que será acionado sempre que o texto em qualquer campo for alterado
        ChangeListener<String> textChangeListener = (observable, oldValue, newValue) -> {
            boolean allFieldsFilled = !CampoSiglaCidade.getText().trim().isEmpty() &&
                                    !CampoNumeroEstacao.getText().trim().isEmpty();
            salvarButton.setDisable(!allFieldsFilled);
        };

        // Ouvinte de propriedade aos campos de texto
        CampoNomeCidade.textProperty().addListener(textChangeListener);
        CampoSiglaCidade.textProperty().addListener(textChangeListener);
        CampoNumeroEstacao.textProperty().addListener(textChangeListener);

        // Defina o resultado da caixa de diálogo para ser um array com os valores dos campos de entrada
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonTypeSalvar) {
                String[] result = new String[3];
                result[0] = CampoNomeCidade.getText();
                result[1] = CampoSiglaCidade.getText();
                result[2] = CampoNumeroEstacao.getText();
                return result;
            }
            return null;
        });

        // Mostre a caixa de diálogo e obtenha o resultado
        Optional<String[]> result = dialog.showAndWait();

        if (!result.isPresent()) {
            throw new RuntimeException("Operação cancelada pelo usuário.");
        }

        result.ifPresent(res -> {
            nomeCidadeInserido = WordUtils.capitalizeFully(res[0]);
            siglaCidadeInserida = res[1].toUpperCase();
            numeroEstacaoInserido = res[2];
        });
        return result;
    }
}

