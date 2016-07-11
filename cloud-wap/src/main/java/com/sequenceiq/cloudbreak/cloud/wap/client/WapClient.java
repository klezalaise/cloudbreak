package com.sequenceiq.cloudbreak.cloud.wap.client;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonParser;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
/**
 * 
 * @author AGREVIN
 *
 */
@Service
public class WapClient {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WapClient.class);
	
	private final JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
	/**
	 * 
	 * @param pKeyFile
	 * @param pKeyPassword
	 * @return
	 * @throws UnrecoverableKeyException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws KeyManagementException
	 */
	private SSLSocketFactory getFactory( File pKeyFile, String pKeyPassword ) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, KeyManagementException{
		  KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
		  KeyStore keyStore = KeyStore.getInstance("PKCS12");
		  InputStream keyInput = new FileInputStream(pKeyFile);
		  keyInput = new FileInputStream(pKeyFile);
		  keyStore.load(keyInput, pKeyPassword.toCharArray());
		  keyInput.close();

		  keyManagerFactory.init(keyStore, pKeyPassword.toCharArray());

		  SSLContext context = SSLContext.getInstance("TLS");
		  context.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());

		  return context.getSocketFactory();
		
	}
	
	
	/**
	 * 
	 * @param endpoint
	 * @param subscriptionId
	 * @param password
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 */
	public void checkConnect(String endpoint,String subscriptionId,String password) throws  UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException{
			
			String fullEndpoint = endpoint +"subscriptions/"+subscriptionId;
			
			LOGGER.debug(fullEndpoint);
			HttpsURLConnection conn = connectionFactory(fullEndpoint,HTTPMethod.GET,password);

			if (conn.getResponseCode() != 200) {
				InputStream is = conn.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String line ;
				StringBuilder response = new StringBuilder();
				
				while((line = br.readLine())!=null){
					response.append(line);
					response.append("\n");
				}
				br.close();
				LOGGER.debug(response.toString());
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}
			conn.disconnect();	
	}


	/**
	 * 
	 * @param endpoint
	 * @param method
	 * @param password
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws KeyManagementException
	 * @throws ProtocolException
	 */
	private HttpsURLConnection connectionFactory(String endpoint,HTTPMethod method,String password)
			throws MalformedURLException, IOException, UnrecoverableKeyException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, KeyManagementException, ProtocolException {
		
		
		URL url = new URL(endpoint);
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		
		//Certificate badly handle
		conn.setSSLSocketFactory(getFactory(new File("/root/cloudbreak/mypkcs12.pfx"), password));
		conn.setRequestMethod(method.toString());
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("Content-Type", "application/json");
		return conn;
	}
	/**
	 * 
	 * @param endpoint
	 * @param subscription
	 * @param password
	 * @param name
	 * @return
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws ParseException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public String createVM(String endpoint,String subscription,String password,String name) throws IOException, UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, ParseException, ParserConfigurationException, SAXException{
		
		String endpointVirtualMachine = endpoint+subscription+"/services/systemcenter/vmm/VirtualMachines";
	
		LOGGER.debug(endpoint);
		HttpsURLConnection conn = connectionFactory(endpointVirtualMachine,HTTPMethod.POST,password);
		conn.setDoOutput(true);
		
		
		JSONObject body = (JSONObject) parser.parse(new FileReader("wap-createVirtualMachine-requestBody.json"));
		body.remove("Name");
		body.put("Name", "Z"+name);
		OutputStream os = conn.getOutputStream();
		
		
		os.write(body.toString().getBytes("UTF-8"));
		os.flush();
		if (conn.getResponseCode() != 201) {
			
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}
		InputStream is = conn.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line ;
		StringBuilder response = new StringBuilder();
		
		while((line = br.readLine())!=null){
			response.append(line);
			response.append("\n");
		}
		
		br.close();
		conn.disconnect();	
		LOGGER.debug(response.toString());
		JSONObject resp = (JSONObject) parser.parse(new StringReader(response.toString()));
		String id = (String)resp.get("ID");
		String stampID = (String)resp.get("StampId");
		LOGGER.debug(id);
		LOGGER.debug(stampID);
		return id;
	}
	
	/**
	 * 
	 * @param endpoint
	 * @param subscription
	 * @param id
	 * @param password
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws MalformedURLException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws ProtocolException
	 * @throws IOException
	 * @throws ParseException
	 */
	public void stopVM(String endpoint,String subscription,String id, String password) throws UnrecoverableKeyException, KeyManagementException, MalformedURLException, KeyStoreException, NoSuchAlgorithmException, CertificateException, ProtocolException, IOException, ParseException{

		LOGGER.debug(endpoint);
		
		HttpsURLConnection conn = connectionFactory(endpoint,HTTPMethod.PUT,password);
		conn.setDoOutput(true);
		
		JSONObject body = (JSONObject) parser.parse(new FileReader("wap-createVirtualMachine-requestBody.json"));
		body.remove("Operation");
		body.put("Operation","Stop");
		OutputStream os = conn.getOutputStream();
		os.write(body.toString().getBytes("UTF-8"));
		os.flush();
		if (conn.getResponseCode() != 204) {
			
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}
	}
	
	public String checkVM(String endpoint,String subscription, String id,String password) throws UnrecoverableKeyException, KeyManagementException, MalformedURLException, KeyStoreException, NoSuchAlgorithmException, CertificateException, ProtocolException, IOException, ParseException{

		String endpointVirtualMachine = endpoint+subscription+"/services/systemcenter/vmm/VirtualMachines(ID=guid'"+id+"',StampId=guid'8ede8b8f-136a-4e03-821f-08510fdb86f8')";
		LOGGER.debug(endpointVirtualMachine);
		
		HttpsURLConnection conn = connectionFactory(endpointVirtualMachine,HTTPMethod.GET,password);

		if (conn.getResponseCode() != 200) {
			
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}
		InputStream is = conn.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line ;
		StringBuilder response = new StringBuilder();
		
		while((line = br.readLine())!=null){
			response.append(line);
			response.append("\n");
		}
		
		br.close();
		conn.disconnect();	
		LOGGER.debug(response.toString());
		JSONObject json = (JSONObject) parser.parse(new StringReader(response.toString()));
		String status = (String) json.get("Status");
		return status;
	}
	/**
	 * 
	 * @param endpoint
	 * @param subscription
	 * @param password
	 * @param instanceName
	 * @return
	 * @throws ParseException
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws MalformedURLException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws ProtocolException
	 * @throws IOException
	 */
	public void deleteVM(String endpoint,String subscription, String password, String instanceName) throws ParseException, UnrecoverableKeyException, KeyManagementException, MalformedURLException, KeyStoreException, NoSuchAlgorithmException, CertificateException, ProtocolException, IOException{
		String endpointVirtualMachine = endpoint+subscription+"/services/systemcenter/vmm/VirtualMachines(ID=guid'"+instanceName+"',StampId=guid'8ede8b8f-136a-4e03-821f-08510fdb86f8')";
		
		stopVM(endpointVirtualMachine, subscription, instanceName, password);
		
		HttpsURLConnection conn = connectionFactory(endpointVirtualMachine,HTTPMethod.DELETE,password);
		if (conn.getResponseCode() != 204) {
			
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}

	}
	
	
	/**
	 * 
	 * @param endpoint
	 * @param subscription
	 * @param password
	 * @param instanceName
	 * @return
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws MalformedURLException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws ProtocolException
	 * @throws IOException
	 * @throws ParseException
	 */
	public String getPrivateIP(String endpoint,String subscription, String password, String instanceName) throws UnrecoverableKeyException, KeyManagementException, MalformedURLException, KeyStoreException, NoSuchAlgorithmException, CertificateException, ProtocolException, IOException, ParseException{

		String endpointVirtualMachine = endpoint+subscription+"/services/systemcenter/vmm/VirtualNetworkAdapters(ID=guid'"+instanceName+"',StampId=guid'8ede8b8f-136a-4e03-821f-08510fdb86f8')";
		LOGGER.debug(endpointVirtualMachine);
		HttpsURLConnection conn = connectionFactory(endpointVirtualMachine,HTTPMethod.GET,password);
		
		if (conn.getResponseCode() != 200) {
			
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}
		InputStream is = conn.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line ;
		StringBuilder response = new StringBuilder();
		
		while((line = br.readLine())!=null){
			response.append(line);
			response.append("\n");
		}
		
		br.close();
		conn.disconnect();	
		LOGGER.debug(response.toString());
		JSONObject json = (JSONObject) parser.parse(new StringReader(response.toString()));
		JSONObject status = (JSONObject) json.get("IPv4Addresses");
		String privateIp = (String) json.get("element");
		return privateIp;
	}
	
}

	

		









