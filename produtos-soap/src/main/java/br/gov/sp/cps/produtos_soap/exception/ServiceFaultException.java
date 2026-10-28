package br.gov.sp.cps.produtos_soap.exception;

/**
 * Exceção base do Web Service.
 *
 * Toda exceção de negócio estende esta classe e carrega um código de erro
 * legível por máquina (ex.: PRODUTO_NAO_ENCONTRADO). O resolver de SOAP Fault
 * converte essas exceções em um Fault do tipo Client com o código e a
 * mensagem no elemento &lt;detail&gt;.
 */
public abstract class ServiceFaultException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String codigoErro;

    protected ServiceFaultException(String codigoErro, String mensagem) {
        super(mensagem);
        this.codigoErro = codigoErro;
    }

    public String getCodigoErro() {
        return codigoErro;
    }
}
