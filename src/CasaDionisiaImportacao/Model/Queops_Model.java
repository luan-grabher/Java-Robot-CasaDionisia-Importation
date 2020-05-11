package CasaDionisiaImportacao.Model;

import CasaDionisiaImportacao.Model.Entity.Lcto;
import Executor.Execution;
import Executor.View.Carregamento;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JFrame;
import main.Arquivo;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import sql.Banco;

public final class Queops_Model {

    private final File arquivo;
    private final DePara_Model dePara;
    private final List<Lcto> lctos = new ArrayList<>();
    private List<String> fluxosDesconhecidos = new ArrayList<>();
    private Banco db = null;
    private String contaBanco; //Itau

    public Queops_Model(File arquivo, File arquivoDePara, String contaBanco) {
        this.arquivo = arquivo;
        this.dePara = new DePara_Model(arquivoDePara);
        this.contaBanco = contaBanco;

        createListToImport();

    }

    private void createListToImport() {
        try {
            XSSFWorkbook wk = new XSSFWorkbook(arquivo);
            XSSFSheet sh = wk.getSheetAt(wk.getActiveSheetIndex());

            for (int i = sh.getFirstRowNum(); i <= sh.getLastRowNum(); i++) {
                Row lin = sh.getRow(i);
                try {
                    Date data = lin.getCell(1).getDateCellValue();
                    Double valor = Double.valueOf(lin.getCell(7).toString().replaceAll(",", "."));
                    String fluxo = lin.getCell(9).toString() + " - " + lin.getCell(4).toString();

                    Long cred;
                    Long deb;
                    Long[] contas = dePara.getContas(fluxo);

                    if (valor < 0) {
                        deb = contas[0];
                        cred = Long.valueOf(contaBanco);
                    } else {
                        deb = Long.valueOf(contaBanco);
                        cred = contas[0];
                    }

                    Long participante = contas[1];

                    if (cred == -1 | deb == -1) {
                        addFluxoDesconhecido(fluxo);
                    }

                    String nf = regex(lin.getCell(8).toString(), "[A-Z\\s\\d]");

                    lctos.add(
                            new Lcto(
                                    data, //Data
                                    nf + " " + lin.getCell(4) + " " + lin.getCell(5) + " "
                                    + fluxo + " " + lin.getCell(10) + " " + lin.getCell(11) + " "
                                    + lin.getCell(13), //Historico
                                    nf, //DOCTO
                                    Long.valueOf(3050), //Historico padrao -- não está sendo usado
                                    deb, cred,
                                    participante,
                                    valor
                            )
                    );
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }

            wk.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Lcto> getLctos() {
        return lctos;
    }

    public boolean salvarFluxosDesconhecidos() {
        for (String fluxo : fluxosDesconhecidos) {
            if (!dePara.insertFluxo(fluxo)) {
                return false;
            }
        }

        return true;
    }

    public void addFluxoDesconhecido(String fluxo) {
        if (!fluxosDesconhecidos.stream().anyMatch(f -> f.equals(fluxo))) {
            fluxosDesconhecidos.add(fluxo);
        }
    }

    public String inserirLctosNoBanco() {

        /*Comeca carregamento*/
        int max = lctos.size();
        Carregamento barra = new Carregamento("Inserindo lançamentos no banco", 0, max);

        conectaBanco();
        try {
            String empresa = "662";

            Object[] empDBInfo = db.select("select e.BDCODPLAPADRAO, e.BDCODHIST from SP_GR_SCI_EMPRESAS"
                    + " e where e.BDCODEMP = '" + empresa + "';").get(0);
            String empCodPlanoPadrao = empDBInfo[0].toString();
            String empCodHist = empDBInfo[1].toString();

            conectaBanco();
            Long chave = Long.valueOf(db.select("select coalesce((max(bdchave)), 1) from vsuc_empresas_tlan "
                    + "where bdcodemp = '" + empresa + "'").get(0)[0].toString());

            String inserts = "";
            for (int i = 0; i < max; i++) {
                chave++;

                barra.atualizar(i);

                Lcto lcto = lctos.get(i);

                String insertLcto = ""
                        + "INSERT INTO VSUC_EMPRESAS_TLAN (BDCODEMP,BDCHAVE,BDCODPLAPADRAO,BDDEBITO,"
                        + "BDCREDITO,BDCODTERCEIROD,BDCODHIST,BDDATA,BDVALOR,BDCOMPL,BDDCTO,BDCODUSU,"
                        + "BDTIPOLAN"
                        + ") "
                        + "VALUES ( "
                        + "'" + empresa + "',"
                        + "'" + chave + "',"
                        + "'" + empCodPlanoPadrao + "',"
                        + "'" + lcto.getContaDeb() + "',"
                        + "'" + lcto.getContaCred() + "',"
                        + "" + lcto.getCodTerceiro() + ","
                        + "'" + empCodHist + "',"
                        + "'" + lcto.getData() + "',"
                        + "'" + lcto.getValor() + "',"
                        + "'" + lcto.getHistorico() + "',"
                        + "'" + lcto.getDocto() + "',"
                        + "'40',"
                        + "2"
                        + ");\n";
                inserts += insertLcto;

                /*limitação de 20 linhas por execução para que o execute block funcione*/
                if (i != 0 & i % 40 == 0) {
                    executaBlocoSql(inserts);
                    inserts = "";
                }
            }

            if (!"".equals(inserts)) {
                executaBlocoSql(inserts);
                inserts = "";
            }

            barra.dispose();
            return "";
        } catch (Exception e) {
            barra.dispose();
            return "Erro ao inserir lançamentos: " + e;
        }
    }

    private void executaBlocoSql(String bloco) {
        conectaBanco();
        db.executeBatchs(bloco.split("\n"));
    }

    private void conectaBanco() {
        db = new Banco("\\\\ZAC\\robos\\Tarefas\\Arquivos\\sci.cfg");
    }

    public List<String> getFluxosDesconhecidos() {
        return fluxosDesconhecidos;
    }

    private String regex(String str, String pattern) {
        String strFim = "";
        String[] sp = str.split("");
        for (String s : sp) {
            if (s.matches(pattern)) {
                strFim += s;
            }
        }
        return strFim;
    }
}
