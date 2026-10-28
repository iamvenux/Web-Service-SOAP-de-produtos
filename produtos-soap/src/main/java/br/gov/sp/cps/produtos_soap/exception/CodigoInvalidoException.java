package br.gov.sp.cps.produtos_soap.exception;

/**
 * Lançada quando o código do produto é zero ou negativo.
 */
public class CodigoInvalidoException extends ServiceFaultException {

    private static final long serialVersionUID = 1L;

    public static final String CODIGO_ERRO = "CODIGO_INVALIDO";

    public CodigoInvalidoException(int codigoProduto) {
        super(CODIGO_ERRO, "O código do produto deve ser maior que zero. Valor recebido: " + codigoProduto + ".");
    }
}
