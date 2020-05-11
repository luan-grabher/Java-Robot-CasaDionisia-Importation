package CasaDionisiaImportacao.Control;

import CasaDionisiaImportacao.Model.Queops_Model;
import LctoTemplate.CfgBancoTemplate;
import Selector.Entity.FiltroString;
import Selector.Pasta;
import Selector.View.SelectorOsView;
import static j2html.TagCreator.a;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Control {

    private static Integer empresa = 662;

    public static String response = "";

    private static Integer mes;
    private static Integer ano;
    private static File arquivoMensal = null;
    private static File arquivoDePara = null;

    public static String executar(int mes, int ano) {
        try {
            Control.mes = mes;
            Control.ano = ano;

            List<CfgBancoTemplate> bancos = new ArrayList<>();

            //cfg queops
            CfgBancoTemplate cfgQueops = new CfgBancoTemplate();
            cfgQueops.setContaBanco(625);
            cfgQueops.setFiltroNomeArquivoOriginal("Extrato;Queops;.xlsx#Santander");
            cfgQueops.setNomeBanco("Queops");

            CfgBancoTemplate cfgSantander = new CfgBancoTemplate();
            cfgSantander.setContaBanco(10);
            cfgSantander.setFiltroNomeArquivoOriginal("Extrato;Queops;Santander;.xlsx");
            cfgSantander.setNomeBanco("Queops Santander");

            bancos.add(cfgQueops);
            bancos.add(cfgSantander);

            StringBuilder retorno = new StringBuilder();
            for (CfgBancoTemplate banco : bancos) {
                retorno.append(inserirBanco(banco)).append("<br>");
            }

            return retorno.toString();
        } catch (Exception e) {
            return "Ocorreu o erro no Java: " + e + "<br>" + e.getStackTrace().toString();
        }

    }

    private static String inserirBanco(CfgBancoTemplate banco) {
        String r = "";
        //Pega arquivo Queops do mês
        r = definirArquivos(banco);

        //Control -> Valida Arquivo Queops
        if (r.equals("")) {
            //Model Conta -> Busca Nº Conta ctb                
            //Model Arquivo -> Recebe Arquivo

            Queops_Model model = new Queops_Model(arquivoMensal, arquivoDePara, banco.getContaBanco().toString());

            //Model Arquivo -> Se todos Lctos tiverem contas
            if (model.getFluxosDesconhecidos().isEmpty()) {
                //TRUE
                //Model Arquivo -> Importar Lctos
                r = model.inserirLctosNoBanco();
                if(r == ""){
                    r = "Importei " + model.getFluxosDesconhecidos().size() + " lançamentos para o banco " + banco.getNomeBanco();
                }
            } else {
                //FALSE
                //Model Arquivo -> Insere no Arquivo Fluxos de Conta desconhecidos
                if (model.salvarFluxosDesconhecidos()) {
                    //Modelo Conta -> Insere fluxo
                    //View -> Mostra mensagem para completar arquivo de Fluxos
                    r = "Existem fluxos de contas desconhecidos!\n"
                            + "Por favor verifique os fluxos adicionados no arquivo "
                            + a("DePara").withHref(arquivoDePara.getAbsolutePath()).render();
                } else {
                    r = "Houve erros ao salvar os fluxos desconhecidos! "
                            + "Por favor rode o programa novamente ou contate um programador!";
                }
            }
        }

        return r;
    }

    private static String definirArquivos(CfgBancoTemplate banco) {

        String mesMM = (mes < 10 ? "0" : "") + mes;

        String pastaEmpresa = "Comércio e Importação de Alimentos e Bebidas Casa Dionísia Eireli";
        String filtroArquivoDePara = "DePara;Contas;.csv";
        String escrituracaoMensal = "\\\\HEIMERDINGER\\docs\\Contábil\\Clientes\\" + pastaEmpresa
                + "\\Escrituração mensal\\";
        String pastaNaRede = escrituracaoMensal + ano + "\\Movimentos\\" + mesMM + "." + ano;

        File pastaMovimento = new File(pastaNaRede);
        File pastaEscrituracaoMensal = new File(escrituracaoMensal);

        arquivoMensal = Pasta.procura_arquivo(pastaMovimento, banco.getFiltroNomeArquivoOriginal());
        arquivoDePara = Pasta.procura_arquivo(pastaEscrituracaoMensal, filtroArquivoDePara);

        if (arquivoDePara == null || !arquivoDePara.exists()) {
            return SelectorOsView.msgArquivoPastaNaoEncontrado(pastaEscrituracaoMensal, new FiltroString(filtroArquivoDePara));
        } else {
            if (arquivoMensal == null || !arquivoMensal.exists()) {
                return SelectorOsView.msgArquivoPastaNaoEncontrado(pastaMovimento, new FiltroString(banco.getFiltroNomeArquivoOriginal()));
            } else {
                return "";
            }
        }
    }
}
