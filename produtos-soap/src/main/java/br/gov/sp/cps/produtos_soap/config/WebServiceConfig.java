package br.gov.sp.cps.produtos_soap.config;

import br.gov.sp.cps.produtos_soap.exception.DetailSoapFaultDefinitionExceptionResolver;
import br.gov.sp.cps.produtos_soap.exception.ServiceFaultException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurer;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.interceptor.PayloadValidatingInterceptor;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j2.callback.SimplePasswordValidationCallbackHandler;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import java.util.List;
import java.util.Map;
import java.util.Properties;

@EnableWs
@Configuration
public class WebServiceConfig implements WsConfigurer {

    // Credenciais lidas do application.properties (ou das variáveis WS_USUARIO / WS_SENHA)
    @Value("${ws.security.usuario}")
    private String usuario;

    @Value("${ws.security.senha}")
    private String senha;

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
    // WS-SECURITY
    // ---------------------------------------------------------------

    /**
     * Registra os interceptors na ordem em que serão executados:
     * 1) WS-Security: só deixa passar requisições com UsernameToken válido;
     * 2) Validação do payload contra o XSD.
     */
    @Override
    public void addInterceptors(List<EndpointInterceptor> interceptors) {
        interceptors.add(securityInterceptor());
        interceptors.add(validatingInterceptor());
    }

    /**
     * Exige o cabeçalho &lt;wsse:Security&gt; com &lt;wsse:UsernameToken&gt;.
     * Sem o cabeçalho ou com usuário/senha inválidos, o próprio interceptor
     * devolve um SOAP Fault e o endpoint nem é chamado.
     */
    @Bean
    public Wss4jSecurityInterceptor securityInterceptor() {
        Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();
        interceptor.setValidationActions("UsernameToken");
        interceptor.setValidationCallbackHandler(securityCallbackHandler());
        return interceptor;
    }

    /**
     * Confere usuário e senha do UsernameToken
     * (aceita PasswordText e PasswordDigest).
     */
    @Bean
    public SimplePasswordValidationCallbackHandler securityCallbackHandler() {
        SimplePasswordValidationCallbackHandler handler = new SimplePasswordValidationCallbackHandler();
        handler.setUsersMap(Map.of(usuario, senha));
        return handler;
    }

    /**
     * Valida o corpo da requisição contra o produtos.xsd.
     * Ex.: &lt;codigo&gt;abc&lt;/codigo&gt; gera SOAP Fault "Client" com o erro de validação.
     */
    @Bean
    public PayloadValidatingInterceptor validatingInterceptor() {
        PayloadValidatingInterceptor interceptor = new PayloadValidatingInterceptor();
        interceptor.setXsdSchema(produtosSchema());
        interceptor.setValidateRequest(true);
        interceptor.setValidateResponse(true);
        return interceptor;
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
