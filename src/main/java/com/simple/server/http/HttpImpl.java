package com.simple.server.http;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.util.Base64;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.simple.server.config.AppConfig;
import com.simple.server.config.ContentType;
import com.simple.server.domain.contract.RedirectRouting;
import com.simple.server.util.HttpNotFoundException;
import com.simple.server.util.ObjectConverter;

import org.springframework.http.MediaType;



@Service("httpImpl")
@Scope("singleton")
public class HttpImpl {

	
	@Autowired
	private AppConfig appConfig;
	
	private static final Logger logger = LogManager.getLogger(HttpImpl.class);
	
	
	
	private static String convertBody(ContentType contentType, String body) throws Exception {
		String converted = null;
		if (ContentType.XmlPlainText.equals(contentType)) {
			converted = ObjectConverter.objectToXml(body, false);
		} else if (ContentType.ApplicationJson.equals(contentType)) {
			converted = ObjectConverter.objectToJson(body);
		} else if (ContentType.ApplicationXml.equals(contentType)) {
			converted = ObjectConverter.objectToXml(body, false);
		} else {
			converted = ObjectConverter.objectToJson(body);
		}
		return converted;
	}

	private static String getValidContentType(ContentType contentType) {
		if (ContentType.XmlPlainText.equals(contentType)) {
			return "text/plain";
		} else if (ContentType.ApplicationJson.equals(contentType)) {
			return "application/json";
		} else if (ContentType.ApplicationXml.equals(contentType)) {
			return "application/xml";
		} else {
			return "application/json";
		}
	}
	
	private static void checkHttpResonseStatusCode(String url, int statusCode) throws HttpNotFoundException {
		if (statusCode < 200 || statusCode > 300)
			throw new HttpNotFoundException(String.format("HTTP Error, url: < %s >, status code: %s", url, statusCode));
	}

	private static HttpHeaders createHeaders(ContentType contentType) {
		return new HttpHeaders() {
			{
				if (ContentType.XmlPlainText.equals(contentType) || ContentType.ApplicationXml.equals(contentType)) {
					setContentType(new MediaType("application", "xml", Charset.forName("UTF-8")));
				} else if (ContentType.ApplicationJson.equals(contentType)) {
					setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
				}
			}
		};
	}
	
	private static HttpHeaders createHeaders(String contentType) {
		return new HttpHeaders() {
			{
				setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
			}
		};
	}

	public static HttpHeaders createHeaders() {
		return new HttpHeaders() {
			{
				setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
			}
		};
	}

	private static HttpHeaders createHeaders(String username, String password) {
		return new HttpHeaders() {
			{
				String auth = username + ":" + password;
				byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
				String authHeader = "Basic " + new String(encodedAuth);
				set("Authorization", authHeader);
				setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
			}
		};
	}

	private ClientHttpRequestFactory getSimpleClientHttpRequestFactory() {
		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		clientHttpRequestFactory.setConnectionRequestTimeout( appConfig.timeoutPolicies.getFrontSyncConnectionRequestTimeout() );
		clientHttpRequestFactory.setConnectTimeout( appConfig.timeoutPolicies.getFrontSyncConnectionTimeout() );
		clientHttpRequestFactory.setReadTimeout( appConfig.timeoutPolicies.getFrontSyncReadTimeout() );
		return clientHttpRequestFactory;
	}

	private ClientHttpRequestFactory getClientHttpRequestFactory() {
		int timeout = 5000;
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout( appConfig.timeoutPolicies.getFrontSyncConnectionRequestTimeout() )
				.setConnectionRequestTimeout( appConfig.timeoutPolicies.getFrontSyncConnectionRequestTimeout() )
				.build();
		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
		return new HttpComponentsClientHttpRequestFactory(client);
	}

	private static ClientHttpRequestFactory getSimplestClientHttpRequestFactory() {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		return new HttpComponentsClientHttpRequestFactory(httpClient);
	}
	
	
	

	@SuppressWarnings("all")
	public ResponseEntity<String> doPostNTLM(String url, String body, String contentType) throws Exception {

		DefaultHttpClient httpclient = new DefaultHttpClient();
		try {
			httpclient.addRequestInterceptor(new HttpRequestInterceptor() {
				public void process(final HttpRequest request, final HttpContext context)
						throws HttpException, IOException {
					request.addHeader("Accept-Encoding", "gzip, deflate");
					request.addHeader("Accept", "*/*");
					request.addHeader("Accept-Language", " ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
					request.addHeader("Content-Type", contentType);
				}
			});

			HttpContext localContext = new BasicHttpContext();
			HttpPost httpPost = new HttpPost(url);

			httpPost.addHeader("Content-Type", contentType);
			
			final RequestConfig params = RequestConfig.custom()
										.setConnectTimeout( appConfig.timeoutPolicies.getFrontSyncConnectionTimeout() )
										.setConnectionRequestTimeout( appConfig.timeoutPolicies.getFrontSyncConnectionRequestTimeout() ).build();
			httpPost.setConfig(params);

			// StringEntity entity = new StringEntity(body);

			StringEntity entity = new StringEntity(body, "utf-8");

			httpPost.setEntity(entity);
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(AuthScope.ANY,
					new NTCredentials(AppConfig.ACC, AppConfig.PSW, AppConfig.WORKSTATION, AppConfig.DOMEN));

			List<String> authtypes = new ArrayList<String>();
			authtypes.add(AuthPolicy.NTLM);
			httpclient.getParams().setParameter(AuthPNames.TARGET_AUTH_PREF, authtypes);

			localContext.setAttribute(ClientContext.CREDS_PROVIDER, credsProvider);
			HttpResponse response = httpclient.execute(httpPost, localContext);

			checkHttpResonseStatusCode(url, response.getStatusLine().getStatusCode());

			if (response.getEntity() != null) {
				response.getEntity().consumeContent();
			}

			StringBuilder respBody = null;
			String sBody = "";
			if (response.getEntity() != null) {
				respBody = new StringBuilder();
				BufferedReader rd = new BufferedReader(
						new InputStreamReader(response.getEntity().getContent(), "UTF8"));
				String line = "";
				while ((line = rd.readLine()) != null) {
					respBody.append(line);
				}
				response.getEntity().consumeContent();
				sBody = respBody.toString();
			}

			httpclient.getConnectionManager().shutdown();
			final Integer httpCode = response.getStatusLine().getStatusCode();
			return new ResponseEntity<String>(sBody, createHeaders(), HttpStatus.valueOf(httpCode));

		} catch (HttpNotFoundException e) {
			throw new HttpNotFoundException(String.format("HttpImpl NTLM: %s", e.getMessage()));
		} catch (Exception e) {
			throw new Exception(String.format("HttpImpl NTLM: %s", e.getMessage()));
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
	}

	

	public ResponseEntity<String> doPost(RedirectRouting redirect, String body) throws Exception {

		ResponseEntity<String> res = null;
		Boolean useAuth = false;

		if (body == null)
			body = "";
		try {

			if (!(redirect.getUseAuth() == null)) {
				useAuth = redirect.getUseAuth();
			}
			logger.debug(String.format("HttpImpl PRE %s  , %s, thread id: %s", redirect.getUrl(), body,
					Thread.currentThread().getId()));
			String convertedBody = HttpImpl.convertBody(redirect.getResponseContentType(), body);
			if (useAuth) {
				res = doPostNTLM(redirect.getUrl(), convertedBody, getValidContentType(ContentType.fromValue(redirect.getContentType())));
			} else {
				ResponseEntity<String> response = null;
				URI uri = new URI(redirect.getUrl());

				HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
				
				httpRequestFactory.setConnectionRequestTimeout( appConfig.timeoutPolicies.getFrontSyncConnectionRequestTimeout() );
				httpRequestFactory.setConnectTimeout( appConfig.timeoutPolicies.getFrontSyncConnectionTimeout() );
				httpRequestFactory.setReadTimeout(appConfig.timeoutPolicies.getFrontSyncReadTimeout());

				RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
				HttpEntity<String> entity = null;
				entity = new HttpEntity<String>(body, createHeaders(ContentType.fromValue(redirect.getContentType())));
				try {
					response = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
					checkHttpResonseStatusCode(uri.toURL().toString(), response.getStatusCode().value());
									
					return response;
					
				} catch (RestClientException e) {
					logger.debug(
							String.format("[HttpImpl] [ERR] %s %s, thread id: %s , thread name:  %s", uri.toString(),
									e.getMessage(), Thread.currentThread().getId(), Thread.currentThread().getName()));
					throw new HttpNotFoundException(
							String.format("HttpImpl, url: < %s >, %s", e.getMessage(), uri.toString()));
				}
			}

		} catch (HttpStatusCodeException e) {
			logger.debug(String.format("front: HttpImpl ERR %s , thread id: %s , thread name:  %s", redirect.getUrl(),
					Thread.currentThread().getId(), Thread.currentThread().getName()));
			String json = ObjectConverter.xmlToJson(e.getResponseBodyAsString());
			return new ResponseEntity<String>(json, createHeaders(), e.getStatusCode());
		}
		return res;
	}
	
	
	

	public ResponseEntity<String> get(RedirectRouting redirect, String params) throws Exception {

		ResponseEntity<String> res = null;
		Boolean useAuth = false;

		if (params == null)
			params = "";
		try {

			if (!(redirect.getUseAuth() == null)) {
				useAuth = redirect.getUseAuth();
			}
			logger.debug(String.format("HttpImpl PRE %s  , thread id: %s", redirect.getUrl() + params,
					Thread.currentThread().getId()));
			if (useAuth) {
				res = doGetNTLM(redirect.getUrl() + params,
						getValidContentType(ContentType.fromValue(redirect.getContentType())));
			} else {
				URI uri = new URI(redirect.getUrl() + params);
				RestTemplate restTemplate = new RestTemplate(getSimpleClientHttpRequestFactory());

				HttpEntity<String> entity = new HttpEntity<String>("", createHeaders());

				res = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
			}
		} catch (HttpStatusCodeException e) {
			logger.debug(String.format("front: HttpImpl ERR %s , thread id: %s , thread name:  %s",
					redirect.getUrl() + params, Thread.currentThread().getId(), Thread.currentThread().getName()));
			String json = ObjectConverter.xmlToJson(e.getResponseBodyAsString());
			return new ResponseEntity<String>(json, createHeaders(), e.getStatusCode());
		}
		return res;
	}

	
	
	@SuppressWarnings("all")
	public ResponseEntity<String> doGetNTLM(String url, String contentType) throws Exception {

		DefaultHttpClient httpclient = new DefaultHttpClient();

		httpclient.addRequestInterceptor(new HttpRequestInterceptor() {
			public void process(final HttpRequest request, final HttpContext context)
					throws HttpException, IOException {
				request.addHeader("Accept-Encoding", "gzip, deflate");
				request.addHeader("Accept", "*/*");
				request.addHeader("Accept-Language", " ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
				request.addHeader("Content-Type", contentType);
			}
		});

		HttpContext localContext = new BasicHttpContext();
		HttpGet httpGet = new HttpGet(url);

		final RequestConfig params = RequestConfig.custom()
				.setConnectTimeout( appConfig.timeoutPolicies.getFrontSyncConnectionTimeout() )
				.setConnectionRequestTimeout( appConfig.timeoutPolicies.getFrontSyncConnectionRequestTimeout() ).build();
		httpGet.setConfig(params);

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(AuthScope.ANY,
				new NTCredentials(AppConfig.ACC, AppConfig.PSW, AppConfig.WORKSTATION, AppConfig.DOMEN));

		List<String> authtypes = new ArrayList<String>();
		authtypes.add(AuthPolicy.NTLM);
		httpclient.getParams().setParameter(AuthPNames.TARGET_AUTH_PREF, authtypes);

		localContext.setAttribute(ClientContext.CREDS_PROVIDER, credsProvider);
		HttpResponse response = httpclient.execute(httpGet, localContext);

		StringBuilder body = null;
		String sBody = "";
		if (response.getEntity() != null) {
			body = new StringBuilder();
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF8"));
			String line = "";
			while ((line = rd.readLine()) != null) {
				body.append(line);
			}
			response.getEntity().consumeContent();
			sBody = body.toString();
		}

		httpclient.getConnectionManager().shutdown();

		final Integer httpCode = response.getStatusLine().getStatusCode();

		return new ResponseEntity<String>(sBody, createHeaders(), HttpStatus.valueOf(httpCode));
	}

	
}
