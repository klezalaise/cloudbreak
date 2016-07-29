package com.sequenceiq.cloudbreak.cloud.wap.client;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import com.sequenceiq.cloudbreak.cloud.wap.util.WapPublicIP;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
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
	 * Create SSLSocketFactory in order to use certificate(pKeyFile)
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
	 * Check credential (endpoint,password, subscription)
	 * Use the parameters to call the api
	 * if not code 200 as response => wrong parameters
	 * called when a new credential is created
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
	 * Create HTTP Connection
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
	 * Create a new Virtual Machine
	 * Call API to create a new virtual Machine
	 * Use a Json file as request body
	 * 
	 * @param endpoint
	 * @param subscription
	 * @param password
	 * @param name
	 * @return VM ID
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
		
		//API Endpoint
		String endpointVirtualMachine = endpoint+subscription+"/services/systemcenter/vmm/VirtualMachines";
	
		//Make connection
		LOGGER.debug(endpoint);
		HttpsURLConnection conn = connectionFactory(endpointVirtualMachine,HTTPMethod.POST,password);

		setRequestBody("Name", "Z"+name, conn);
		
		
		//Request, and verify that it worked (code 201)
		if (conn.getResponseCode() != 201) {
			
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}
		
		//Get the response
		String strResponse = getResponse(conn);
		LOGGER.debug(strResponse);
		
		//Read response to extract VM ID 
		JSONObject resp = (JSONObject) parser.parse(new StringReader(strResponse));
		String id = (String)resp.get("ID");
		LOGGER.debug(id);
		
		return id;
	}


	private String getResponse(HttpsURLConnection conn) throws IOException {
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
		String strResponse = response.toString();
		return strResponse;
	}
	
	/**
	 * Call API to Stop VM 
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
		operationOnVirtualMachine(endpoint, subscription, password, id, "Stop");
	}
	
	
	/**
	 * 
	 * @param endpoint
	 * @param subscription
	 * @param id
	 * @param password
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
	public String checkVM(String endpoint,String subscription, String id,String password) throws UnrecoverableKeyException, KeyManagementException, MalformedURLException, KeyStoreException, NoSuchAlgorithmException, CertificateException, ProtocolException, IOException, ParseException{
		
		//API Endpoint
		String endpointVirtualMachine = endpoint+subscription+"/services/systemcenter/vmm/VirtualMachines(ID=guid'"+id+"',StampId=guid'8ede8b8f-136a-4e03-821f-08510fdb86f8')";
		LOGGER.debug(endpointVirtualMachine);
		
		HttpsURLConnection conn = connectionFactory(endpointVirtualMachine,HTTPMethod.GET,password);
		
		//Call API and check if request was correct(code 200)
		if (conn.getResponseCode() != 200) {
			
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}
		
		//Read Response
		String StrResponse = getResponse(conn);
		
		
		JSONObject json = (JSONObject) parser.parse(new StringReader(StrResponse));
		
		//Get Status parameter
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
		
		//API Endpoint
		String endpointVirtualMachine = endpoint+subscription+"/services/systemcenter/vmm/VirtualMachines(ID=guid'"+instanceName+"',StampId=guid'8ede8b8f-136a-4e03-821f-08510fdb86f8')";
		
		/**
		 * Stop VM
		 * VM need to be stopped to be deleted
		 */
		stopVM(endpoint, subscription, instanceName, password);
		
		HttpsURLConnection conn = connectionFactory(endpointVirtualMachine,HTTPMethod.DELETE,password);
		
		//Call API and check if the request was correct(code 204)
		if (conn.getResponseCode() != 204) {
			
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}

	}
	
	
	/**
	 * Get Private IP for a Given Virtual Machine on a Virtual Network
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
		
		//API Endpoint
		String endpointVirtualMachine = endpoint+subscription+"/services/systemcenter/vmm/VirtualMachines(ID=guid'"+instanceName+"',StampId=guid'8ede8b8f-136a-4e03-821f-08510fdb86f8')/VirtualNetworkAdapters";
		LOGGER.debug(endpointVirtualMachine);
		HttpsURLConnection conn = connectionFactory(endpointVirtualMachine,HTTPMethod.GET,password);
		
		
		//Call API and check if request was correct (code 200)
		if (conn.getResponseCode() != 200) {
			
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}
		
		
		
		
		String strResponse = getResponse(conn);
		
		
		JSONObject  json = (JSONObject) parser.parse(new StringReader(strResponse));
		
		//Get private IP value from response
		String privateIp = getPrivateIPFromResponse(json);
		
		
		return privateIp;
	}

/**
 * 
 * @param json
 * @return
 */
	private String getPrivateIPFromResponse(JSONObject json) {
		JSONArray valueArray = (JSONArray) json.get("value");
		JSONObject value = (JSONObject) valueArray.get(0);
		JSONArray array = (JSONArray) value.get("IPv4Addresses");
		String privateIp = (String)array.get(0);
		return privateIp;
	}
	
	
	/**
	 * Start a given Virtual Machine
	 * @param endpoint
	 * @param subscription
	 * @param password
	 * @param instanceName
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
	public void startVM(String endpoint,String subscription, String password, String instanceName) throws UnrecoverableKeyException, KeyManagementException, MalformedURLException, KeyStoreException, NoSuchAlgorithmException, CertificateException, ProtocolException, IOException, ParseException{
		
		operationOnVirtualMachine(endpoint, subscription, password, instanceName,"Start");
	}


	/**
	 * @param endpoint
	 * @param subscription
	 * @param password
	 * @param instanceName
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws KeyManagementException
	 * @throws ProtocolException
	 * @throws ParseException
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	private void operationOnVirtualMachine(String endpoint, String subscription, String password, String instanceName,String operation)
			throws MalformedURLException, IOException, UnrecoverableKeyException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, KeyManagementException, ProtocolException, ParseException,
			FileNotFoundException, UnsupportedEncodingException {
		
		String endpointVirtualMachine = endpoint+subscription+"/services/systemcenter/vmm/VirtualMachines(ID=guid'"+instanceName+"',StampId=guid'8ede8b8f-136a-4e03-821f-08510fdb86f8')";
		HttpsURLConnection conn = connectionFactory(endpointVirtualMachine,HTTPMethod.PUT,password);
		conn.setDoOutput(true);
		
		setRequestBody("Operation",operation, conn);
		
		if (conn.getResponseCode() != 204) {
			
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}
	}


	/**
	 * @param operation
	 * @param conn
	 * @throws ParseException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private void setRequestBody(String parameter,String value, HttpsURLConnection conn)
			throws ParseException, FileNotFoundException, IOException, UnsupportedEncodingException {
		JSONObject body = (JSONObject) parser.parse(new FileReader("wap-createVirtualMachine-requestBody.json"));
		body.remove(parameter);
		body.put(parameter,value);
		
		OutputStream os = conn.getOutputStream();
		os.write(body.toString().getBytes("UTF-8"));
		os.flush();
	}
	
	/**
	 * Add a new NAT rule : 
	 * 1) Get Gateway ID for a given virtual Network ID
	 * 2) Get Nat Connection ID for a given gateway ID
	 * 3) Get Private IP for a given Virtual Machin
	 * 4) Create a new NAT rule
	 * @param endpoint
	 * @param subscription
	 * @param password
	 * @param instanceName
	 * @param port
	 * @param ruleName
	 * @param vnetworkID
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
	public String addNATRule(String endpoint, String subscription, String password, String instanceName,int port,String ruleName,String vnetworkID) throws UnrecoverableKeyException, KeyManagementException, MalformedURLException, KeyStoreException, NoSuchAlgorithmException, CertificateException, ProtocolException, IOException, ParseException{
		
		String gatewayID = getGatewayID(endpoint, subscription, password, instanceName, vnetworkID);
		
		
		
		String natConnectionID = getNATConnectionID(endpoint, subscription, password, instanceName, gatewayID);
		
		String privateIP = getPrivateIP(endpoint, subscription, password, instanceName);
		
		
		
		String endpointNAT = endpoint+subscription+"/services/systemcenter/vmm/NATRules";
		LOGGER.debug(endpointNAT);
		HttpsURLConnection conn = connectionFactory(endpointNAT, HTTPMethod.POST, password);
		
		setRequestBodyNAT(port, ruleName, natConnectionID, privateIP, conn);
		
		if (conn.getResponseCode() != 201) {
			
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}
		
	
		String strResponse = getResponse(conn);
		
		JSONObject jsonResponNAT = (JSONObject) parser.parse(new StringReader(strResponse));
		String natID = (String)jsonResponNAT.get("ID");
		
		return natID;
	}


	/**
	 * Set body for Adding a new NAT Rule
	 * @param port
	 * @param ruleName
	 * @param natConnectionID
	 * @param privateIP
	 * @param conn
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private void setRequestBodyNAT(int port, String ruleName, String natConnectionID, String privateIP,
			HttpsURLConnection conn) throws IOException, UnsupportedEncodingException {
		conn.setDoOutput(true);
		JSONObject body = new JSONObject();
		body.put("odata.type", "VMM.NATRule");
		body.put("ExternalIPAddress",null);
		body.put("ExternalPort", port);
		body.put("ID", "00000000-0000-0000-0000-000000000000");
		body.put("InternalIPAddress", privateIP);
		body.put("InternalPort", port);
		body.put("Name", ruleName);
		body.put("NATConnectionId", natConnectionID);
		body.put("Protocol", "TCP");
		
		//To Be change
		body.put("StampId", "8ede8b8f-136a-4e03-821f-08510fdb86f8");
		LOGGER.debug(body.toJSONString());
		OutputStream os = conn.getOutputStream();
		os.write(body.toString().getBytes("UTF-8"));
		os.flush();
	}
	
	
	/**
	 * Get Gateway ID
	 * 
	 * @param endpoint
	 * @param subscription
	 * @param password
	 * @param instanceName
	 * @param vnetworkID
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
	private String getGatewayID(String endpoint, String subscription, String password, String instanceName, String vnetworkID) throws UnrecoverableKeyException, KeyManagementException, MalformedURLException, KeyStoreException, NoSuchAlgorithmException, CertificateException, ProtocolException, IOException, ParseException{
		String endpointVMN = endpoint+subscription+"/services/systemcenter/vmm/VMNetworks(ID=guid'"+vnetworkID+"',StampId=guid'8ede8b8f-136a-4e03-821f-08510fdb86f8')/VMNetworkGateways";
		LOGGER.debug(endpointVMN);
		HttpsURLConnection conn = connectionFactory(endpointVMN, HTTPMethod.GET, password);
		
		if (conn.getResponseCode() != 200) {
			
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}
		
		
		String response = getResponse(conn);
		JSONObject jsonResponVMN = (JSONObject) parser.parse(new StringReader(response));
		JSONArray valueArray = (JSONArray) jsonResponVMN.get("value");
		JSONObject value = (JSONObject) valueArray.get(0);
		String gatewayID = (String)value.get("ID");
		return gatewayID;
	}
	
	
	/**
	 * Get Nat ConnectionID
	 * @param endpoint
	 * @param subscription
	 * @param password
	 * @param instanceName
	 * @param gatewayID
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
	private String getNATConnectionID (String endpoint, String subscription, String password, String instanceName, String gatewayID) throws UnrecoverableKeyException, KeyManagementException, MalformedURLException, KeyStoreException, NoSuchAlgorithmException, CertificateException, ProtocolException, IOException, ParseException{ 
		
		String endpointGateway = endpoint+subscription+"/services/systemcenter/vmm/VMNetworkGateways(ID=guid'"+gatewayID+"',StampId=guid'8ede8b8f-136a-4e03-821f-08510fdb86f8')/NATConnections";
		LOGGER.debug(endpointGateway);
		HttpsURLConnection conn = connectionFactory(endpointGateway, HTTPMethod.GET, password);

		if (conn.getResponseCode() != 200) {
			
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}
		
		String response = getResponse(conn);

		JSONObject jsonResponGateway = (JSONObject) parser.parse(new StringReader(response));
		JSONArray valueArray = (JSONArray) jsonResponGateway.get("value");
		JSONObject value = (JSONObject) valueArray.get(0);
		String natConnectionID = (String)value.get("ID");
		return natConnectionID;
	}
	
	
	/**
	 * Delete a Nat Rule for a given NAT Rule ID
	 * @param endpoint
	 * @param subscription
	 * @param password
	 * @param instanceName
	 * @param natRuleID
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws MalformedURLException
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws ProtocolException
	 * @throws IOException
	 */
	public void deleteNATRule(String endpoint, String subscription, String password, String instanceName, String natRuleID) throws UnrecoverableKeyException, KeyManagementException, MalformedURLException, KeyStoreException, NoSuchAlgorithmException, CertificateException, ProtocolException, IOException{
		String endpointNATrule = endpoint+subscription+"/services/systemcenter/vmm/NATRules(ID=guid'"+natRuleID+"',StampId=guid'8ede8b8f-136a-4e03-821f-08510fdb86f8')/NATConnections";
		LOGGER.debug(endpointNATrule);
		HttpsURLConnection conn = connectionFactory(endpointNATrule, HTTPMethod.DELETE, password);
		if (conn.getResponseCode() != 204) {
			
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}
	}
}
	

		









