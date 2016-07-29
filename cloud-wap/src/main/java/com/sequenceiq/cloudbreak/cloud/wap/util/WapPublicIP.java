package com.sequenceiq.cloudbreak.cloud.wap.util;



import org.springframework.stereotype.Service;

@Service
public class WapPublicIP {
	public String publicIP;
	
	
	public void setPublicIP(String publicIP){
		this.publicIP = publicIP;
	}
	
	public String getPublicIP(){
		return publicIP;
	}
}
