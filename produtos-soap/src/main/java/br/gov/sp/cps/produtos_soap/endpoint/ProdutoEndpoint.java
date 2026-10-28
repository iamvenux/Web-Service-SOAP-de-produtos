package br.gov.sp.cps.produtos_soap.endpoint;

import br.gov.sp.cps.produtos_soap.exception.CodigoInvalidoException;
import br.gov.sp.cps.produtos_soap.exception.ProdutoNaoEncontradoException;
import br.gov.sp.cps.produtos_soap.model.ConsultarProdutoRequest;
import br.gov.sp.cps.produtos_soap.model.ConsultarProdutoResponse;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.Map;

@Endpoint
public class ProdutoEndpoint {

    private static final String NAMESPACE = "http://cps.sp.gov.br/produtos";

    private record Produto(String nome, String descricao, String marca, int estoque) {}

    // "Banco de dados" simulado em memória
    private static final Map<Integer, Produto> PRODUTOS = Map.of(
        1, new Produto("Notebook Pro 15", "Notebook 15 polegadas, 16 GB RAM, SSD 512 GB", "TechBrand", 25),
        2, new Produto("Mouse Sem Fio", "Mouse óptico sem fio 2.4 GHz", "ClickMax", 150),
        3, new Produto("Teclado Mecânico", "Teclado mecânico ABNT2 com iluminação RGB", "KeyPro", 40)
    );

    @PayloadRoot(namespace = NAMESPACE, localPart = "consultarProdutoRequest")
    @ResponsePayload
    public ConsultarProdutoResponse consultarProduto(@RequestPayload ConsultarProdutoRequest request) {

        int codigo = request.getCodigo();

        // Validação de entrada: vira SOAP Fault "Client" (CODIGO_INVALIDO)
        if (codigo <= 0) {
            throw new CodigoInvalidoException(codigo);
        }

        // Produto inexistente: vira SOAP Fault "Client" (PRODUTO_NAO_ENCONTRADO)
        Produto p = PRODUTOS.get(codigo);
        if (p == null) {
            throw new ProdutoNaoEncontradoException(codigo);
        }

        ConsultarProdutoResponse response = new ConsultarProdutoResponse();
        response.setNome(p.nome());
        response.setDescricao(p.descricao());
        response.setMarca(p.marca());
        response.setQuantidadeEstoque(p.estoque());

        return response;
    }
}
