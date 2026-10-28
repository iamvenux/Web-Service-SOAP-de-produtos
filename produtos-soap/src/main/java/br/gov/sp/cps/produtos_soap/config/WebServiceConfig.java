package br.gov.sp.cps.produtos_soap.config;

import br.gov.sp.cps.produtos_soap.exception.DetailSoapFaultDefinitionExceptionResolver;
import br.gov.sp.cps.produtos_soap.exception.ServiceFaultException;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import java.util.Properties;

@EnableWs
@Configuration
public class WebServiceConfig {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext context)
    {
        MessageDispatcherServlet servlet =   new MessageDispatcherServlet();

        servlet.setApplicationContext(context);

        servlet.setTransformWsdlLocations(true);

        return new ServletRegistrationBean<>( servlet, "/ws/*" );
    }


    @Bean(name = "produtos")
    public DefaultWsdl11Definition produtosWsdl( XsdSchema produtosSchema) {

        DefaultWsdl11Definition wsdl =  new DefaultWsdl11Definition();

        wsdl.setPortTypeName("ProdutosPort");

        wsdl.setLocationUri("/ws");

        wsdl.setTargetNamespace( "http://cps.sp.gov.br/produtos");

        wsdl.setSchema(produtosSchema);

        return wsdl;
    }

    @Bean
    public XsdSchema produtosSchema() {
        return new SimpleXsdSchema(  new ClassPathResource("produtos.xsd") );
    }

    // ---------------------------------------------------------------
    // SOAP FAULT
    // ---------------------------------------------------------------

    /**
     * Resolver de exceções: transforma exceções lançadas pelo endpoint em SOAP Fault.
     * Ordem 1 para ter prioridade sobre os resolvers padrão do Spring WS.
     */
    @Bean
    public SoapFaultMappingExceptionResolver exceptionResolver() {

        DetailSoapFaultDefinitionExceptionResolver resolver = new DetailSoapFaultDefinitionExceptionResolver();

        // Fault padrão (qualquer exceção não mapeada): erro do servidor, mensagem genérica
        SoapFaultDefinition faultPadrao = new SoapFaultDefinition();
        faultPadrao.setFaultCode(SoapFaultDefinition.SERVER);
        faultPadrao.setFaultStringOrReason("Erro interno ao processar a requisição. Tente novamente mais tarde.");
        resolver.setDefaultFault(faultPadrao);

        // Exceções de negócio: erro do cliente; sem texto fixo => usa a mensagem da exceção
        Properties mapeamentos = new Properties();
        mapeamentos.setProperty(ServiceFaultException.class.getName(), "CLIENT");
        resolver.setExceptionMappings(mapeamentos);

        resolver.setOrder(1);
        return resolver;
    }
}
