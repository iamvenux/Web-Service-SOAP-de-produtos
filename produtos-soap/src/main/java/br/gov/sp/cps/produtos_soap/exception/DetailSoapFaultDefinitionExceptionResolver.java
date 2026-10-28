package br.gov.sp.cps.produtos_soap.exception;

import br.gov.sp.cps.produtos_soap.model.ConsultarProdutoFault;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;

/**
 * Converte exceções em SOAP Fault.
 *
 * - {@link ServiceFaultException} (erros de negócio) viram Fault "Client"
 *   com a mensagem da exceção e um &lt;detail&gt; contendo
 *   &lt;consultarProdutoFault&gt; (código + mensagem), conforme o XSD.
 * - Qualquer outra exceção vira Fault "Server" com mensagem genérica,
 *   para não expor detalhes internos (stack trace, classes) ao cliente.
 */
public class DetailSoapFaultDefinitionExceptionResolver extends SoapFaultMappingExceptionResolver {

    private static final Logger log = LoggerFactory.getLogger(DetailSoapFaultDefinitionExceptionResolver.class);

    private final JAXBContext jaxbContext;

    public DetailSoapFaultDefinitionExceptionResolver() {
        try {
            this.jaxbContext = JAXBContext.newInstance(ConsultarProdutoFault.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("Não foi possível criar o JAXBContext do SOAP Fault", e);
        }
    }

    @Override
    protected void customizeFault(Object endpoint, Exception ex, SoapFault fault) {

        if (ex instanceof ServiceFaultException serviceEx) {
            log.warn("SOAP Fault de cliente [{}]: {}", serviceEx.getCodigoErro(), serviceEx.getMessage());

            ConsultarProdutoFault detalhe = new ConsultarProdutoFault();
            detalhe.setCodigo(serviceEx.getCodigoErro());
            detalhe.setMensagem(serviceEx.getMessage());

            SoapFaultDetail detail = fault.addFaultDetail();
            try {
                Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.marshal(detalhe, detail.getResult());
            } catch (JAXBException e) {
                log.error("Falha ao escrever o detail do SOAP Fault", e);
            }
        } else {
            // Erro inesperado: registra no log do servidor, mas o cliente
            // recebe apenas a mensagem genérica do Fault padrão.
            log.error("Erro inesperado no endpoint {}", endpoint, ex);
        }
    }
}
