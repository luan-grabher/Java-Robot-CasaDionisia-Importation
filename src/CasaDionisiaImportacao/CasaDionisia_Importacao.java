package CasaDionisiaImportacao;

import CasaDionisiaImportacao.Control.Control;
import Robo.AppRobo;

public class CasaDionisia_Importacao {

    /**
     *
     */
    public static void main(String[] args) {

        String nome = "Casa Dionisia Importação #mes#/#ano#";
        AppRobo robo = new AppRobo(nome);

        robo.definirParametros();

        int mes = robo.getParametro("mes").getMes();
        int ano = robo.getParametro("ano").getInteger();

        nome = nome.replaceAll("#mes#", "" + mes);
        nome = nome.replaceAll("#ano#", "" + ano);
        robo.setNome(nome);

        robo.executar(
                Control.executar(mes, ano)
        );
    }

}
