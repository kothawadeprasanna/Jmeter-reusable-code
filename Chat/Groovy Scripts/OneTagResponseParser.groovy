import java.util.regex.Matcher
import java.util.regex.Pattern

import groovy.json.JsonSlurper

def chatSrvr = "";
def chatSrvrProtocol = "http";
def chatSrvrCtxRoot = "";

def cbSrvr = "";
def cbSrvrProtocol = "http";
def cbSrvrCtxRoot = "";

def ofrSrvr = "";
def ofrSrvrProtocol = "http";
def ofrSrvrCtxRoot = "";

String response = prev.getResponseDataAsString();
Pattern pattern = ~/.*EGAINCLOUD.init\((.*)\).*/;
Matcher matcher = pattern.matcher(response);
if (matcher.matches())
{
	String jsonPart = matcher.group(1);
	JsonSlurper jsonSlurper = new JsonSlurper();
	def object = jsonSlurper.parseText(jsonPart);
	assert object instanceof Map;
	assert object.apps instanceof List;
	for(Map s: object.apps)
	{
		assert s instanceof Map;
		assert s.name instanceof String;

		if (s.name == "Analytics"){
			/*do nothing*/
		}
		else if (s.name == "Chat / VA (Docked)"){
			assert s.webServer instanceof String;
			chatSrvr = s.webServer;
			assert s.contextRoot instanceof String;
			chatSrvrCtxRoot = s.contextRoot;
			assert s.protocol instanceof String;
			if (s.protocol == "https")
				chatSrvrProtocol = "https";
		}
		else if (s.name == "Cobrowse"){
			assert s.webServer instanceof String;
			cbSrvr = s.webServer;
			assert s.contextRoot instanceof String;
			cbSrvrCtxRoot = s.contextRoot;
			assert s.protocol instanceof String;
			if (s.protocol == "https")
				cbSrvrProtocol = "https";
		}
		else if (s.name == "Offers"){
			assert s.webServer instanceof String;
			ofrSrvr = s.webServer;
			assert s.contextRoot instanceof String;
			ofrSrvrCtxRoot = s.contextRoot;
			assert s.protocol instanceof String;
			if (s.protocol == "https")
				ofrSrvrProtocol = "https";
		}
	}
	
	vars.put("v_chat_srvr",chatSrvr);
	vars.put("v_chat_srvr_protocol",chatSrvrProtocol);
	vars.put("v_chat_srvr_ctx_root",chatSrvrCtxRoot);
	
	vars.put("v_cb_srvr",cbSrvr);
	vars.put("v_cb_srvr_protocol",cbSrvrProtocol);
	vars.put("v_cb_srvr_ctx_root",cbSrvrCtxRoot);
	
	vars.put("v_ofr_srvr",ofrSrvr);
	vars.put("v_ofr_srvr_protocol",ofrSrvrProtocol);
	vars.put("v_ofr_srvr_ctx_root",ofrSrvrCtxRoot);
}