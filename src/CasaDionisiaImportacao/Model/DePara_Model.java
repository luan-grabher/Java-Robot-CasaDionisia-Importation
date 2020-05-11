package CasaDionisiaImportacao.Model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import main.Arquivo;

public class DePara_Model {

    private final File arquivo;
    private String texto;
    private final List<String[]> dePara = new ArrayList<>();

    public DePara_Model(File arquivo) {
        this.arquivo = arquivo;
        setDePara();
    }

    private void setDePara() {
        texto = Arquivo.ler(arquivo.getAbsolutePath()).replaceAll("\r", "");

        String[] linhas = texto.split("\n");
        for (String linha : linhas) {
            // -1 para considerar os vazios
            dePara.add(linha.split(";",-1));
        }
    }

    public Long[] getContas(String filtro) {
        try {
            for (int i = 0; i < dePara.size(); i++) {
                String[] d = dePara.get(i);
                
                if (d.length >= 3){
                    if(d[2].equals(filtro) & !"".equals(d[0])){
                        String part = String.valueOf(!"".equals(d[1])?d[1]:0);
                        return new Long[]{Long.valueOf(d[0]),Long.valueOf(part)};
                    }
                }
            }
            return new Long[]{Long.valueOf(-1),Long.valueOf(0)};
        } catch (Exception e) {
            return new Long[]{Long.valueOf(-1),Long.valueOf(0)};
        }
    }

    public boolean insertFluxo(String fluxo) {
        texto += "\r\n" + ";;" + fluxo;
        return Arquivo.salvar(arquivo.getAbsolutePath(), texto);
    }
}
