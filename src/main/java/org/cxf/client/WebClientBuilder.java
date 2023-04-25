package org.cxf.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.jaxrs.JAXRSBindingFactory;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.jaxrs.client.ResponseExceptionMapper;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.cxf.client.providers.ApiClientErrorHandler;
import org.cxf.client.utils.JsonUtils;
import org.drop.utils.ProxyConfig;
import org.drop.utils.SSLConfig;
import org.drop.utils.SSLUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

public class WebClientBuilder {

	public static final int IDLE_CONN_TIMEOUT_MILLIS = 5;
	public static final int IDLE_REC_TIMEOUT_MILLIS = 5;
	public static final String[] DEFAULT_MEDIA_TYPES_ALLOWED = new String[] {"application/json", "text/plain"};
	
	private String basePath = null;
	private int recieveTimeoutSec = IDLE_REC_TIMEOUT_MILLIS;
	private int connectionTimeoutSec = IDLE_CONN_TIMEOUT_MILLIS;
	private SSLConfig sslConfig = null;
	private ProxyConfig proxyConfig = null;
	private String[] mediaTypesAllow = null;
	private String clientName = "cxfwebclient";
	
	private ArrayList<Object> additionalProviders = null;
	private ObjectMapper jsonMapper = null;
	private JacksonJaxbJsonProvider jacksonMarshaller = null;
	private ResponseExceptionMapper<Throwable> apiClientErrorHandler = new ApiClientErrorHandler();
	
	public WebClientBuilder() {}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public int getRecieveTimeoutSec() {
		return recieveTimeoutSec;
	}

	public void setRecieveTimeoutSec(int recieveTimeoutSec) {
		this.recieveTimeoutSec = recieveTimeoutSec;
	}

	public int getConnectionTimeoutSec() {
		return connectionTimeoutSec;
	}

	public void setConnectionTimeoutSec(int connectionTimeoutSec) {
		this.connectionTimeoutSec = connectionTimeoutSec;
	}

	public SSLConfig getSslConfig() {
		return sslConfig;
	}

	public void setSslConfig(SSLConfig sslConfig) {
		this.sslConfig = sslConfig;
	}

	public ProxyConfig getProxyConfig() {
		return proxyConfig;
	}

	public void setProxyConfig(ProxyConfig proxyConfig) {
		this.proxyConfig = proxyConfig;
	}

	public String[] getMediaTypesAllow() {
		return mediaTypesAllow;
	}

	public void setMediaTypesAllow(String[] mediaTypesAllow) {
		this.mediaTypesAllow = mediaTypesAllow;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public ArrayList<Object> getAdditionalProviders() {
		return additionalProviders;
	}

	public void setAdditionalProviders(ArrayList<Object> additionalProviders) {
		this.additionalProviders = additionalProviders;
	}

	public ObjectMapper getJsonMapper() {
		return jsonMapper;
	}

	public void setJsonMapper(ObjectMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	public JacksonJaxbJsonProvider getJacksonMarshaller() {
		return jacksonMarshaller;
	}

	public void setJacksonMarshaller(JacksonJaxbJsonProvider jacksonMarshaller) {
		this.jacksonMarshaller = jacksonMarshaller;
	}

	public ResponseExceptionMapper<Throwable> getApiClientErrorHandler() {
		return apiClientErrorHandler;
	}

	public void setApiClientErrorHandler(ResponseExceptionMapper<Throwable> apiClientErrorHandler) {
		this.apiClientErrorHandler = apiClientErrorHandler;
	}
	
	public WebClientProxyBuilder build() throws FileNotFoundException, NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException, UnrecoverableKeyException, KeyManagementException {
		if(jacksonMarshaller == null) {
			jacksonMarshaller = new JacksonJaxbJsonProvider();
			ObjectMapper objectMapper = jsonMapper;
			if(objectMapper == null) {
				objectMapper = JsonUtils.getJsonMapper();
			}
			jacksonMarshaller.setMapper(objectMapper);
		}
		
		List<Object> providers = new ArrayList<>();
		providers.add(jacksonMarshaller);
		if(additionalProviders != null) {
			providers.addAll(additionalProviders);
		}
		if(apiClientErrorHandler != null) {
			providers.add(apiClientErrorHandler);
		}
		
		JAXRSClientFactoryBean cfBean = new JAXRSClientFactoryBean();
		cfBean.setAddress(basePath);
		cfBean.setProviders(providers);
		
		if(mediaTypesAllow == null) {
			mediaTypesAllow = DEFAULT_MEDIA_TYPES_ALLOWED;
		}
	
		Bus bus = cfBean.getBus();
		
		JAXRSBindingFactory factory = new JAXRSBindingFactory();
		factory.setBus(bus);
		
		BindingFactoryManager manager = bus.getExtension(BindingFactoryManager.class);
		manager.registerBindingFactory(JAXRSBindingFactory.JAXRS_BINDING_ID, factory);
		
		WebClient client = cfBean.createWebClient().authorization(manager).accept(mediaTypesAllow);
		HTTPConduit conduit = WebClient.getConfig(client).getHttpConduit();
		conduit.getClient().setConnectionTimeout(connectionTimeoutSec * 1000);
		conduit.getClient().setReceiveTimeout(recieveTimeoutSec * 1000);
		
		if(proxyConfig != null) {
			conduit.getClient().setProxyServer(proxyConfig.getHost());
			conduit.getClient().setProxyServerPort(proxyConfig.getPort());
			if(proxyConfig.hasUser()) {
				conduit.getProxyAuthorization().setUserName(proxyConfig.getUser());
			}
			if(proxyConfig.hasPass()) {
				conduit.getProxyAuthorization().setPassword(proxyConfig.getPass());
			}
		}

		TLSClientParameters tlsClientParameters = null;
		if(sslConfig != null && sslConfig.isEnabled()) {
			tlsClientParameters = new TLSClientParameters();
			tlsClientParameters.setSslContext(SSLUtils.getSSLContext(sslConfig));
			conduit.setTlsClientParameters(tlsClientParameters);
		}
		
		return new WebClientProxyBuilder(client);
	}
	
	
}
