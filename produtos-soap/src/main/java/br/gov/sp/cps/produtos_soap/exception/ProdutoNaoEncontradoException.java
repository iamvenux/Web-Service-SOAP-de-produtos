package br.gov.sp.cps.produtos_soap.exception;

/**
 * Lançada quando o código informado não corresponde a nenhum produto cadastrado.
 */
public class ProdutoNaoEncontradoException extends ServiceFaultException {

    private static final long serialVersionUID = 1L;

    public static final String CODIGO_ERRO = "PRODUTO_NAO_ENCONTRADO";

    public ProdutoNaoEncontradoException(int codigoProduto) {
        super(CODIGO_ERRO, "Produto com código " + codigoProduto + " não encontrado.");
    }
}
