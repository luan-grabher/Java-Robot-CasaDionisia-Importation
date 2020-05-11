package CasaDionisiaImportacao.Model.Entity;

import java.util.Date;
import java.util.Objects;

public class Lcto {
    private final Date data;
    private final String historico;
    private final String docto;
    private final Long historicoPadrao;
    private final Long contaDeb;
    private final Long contaCred;
    private final Long codTerceiro;
    private final Double valor;

    public Lcto(Date data, String historico, String docto, Long historicoPadrao, Long contaDeb, Long contaCred, Long codTerceiro, Double valor) {
        this.data = data;
        this.historico = historico.trim().replaceAll("'", "");
        this.docto = docto.trim().replaceAll("'", "");
        this.historicoPadrao = historicoPadrao;
        this.contaDeb = contaDeb;
        this.contaCred = contaCred;
        this.codTerceiro = codTerceiro;
        this.valor = Math.abs(valor);
    }

    public String getData() {
        return (data.getYear() + 1900) + "-" + (data.getMonth() + 1) + "-" + data.getDate();
    }

    public String getHistorico() {
        String returnedHistorico = historico;
        returnedHistorico = returnedHistorico.length() > 199?returnedHistorico.substring(0, 198):returnedHistorico;
        return returnedHistorico;
    }

    public String getDocto() {
        return docto.substring(0, docto.length()>15?15:docto.length());
    }

    public Long getContaDeb() {
        return contaDeb;
    }

    public Long getContaCred() {
        return contaCred;
    }

    public Double getValor() {
        return valor;
    }

    public Long getHistoricoPadrao() {
        return historicoPadrao;
    }

    public String getCodTerceiro() {
        return !Objects.equals(codTerceiro, Long.valueOf(0))?codTerceiro.toString():"null";
    }
    
}
