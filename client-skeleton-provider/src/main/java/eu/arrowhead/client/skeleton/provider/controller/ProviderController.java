package eu.arrowhead.client.skeleton.provider.controller;

import eu.arrowhead.client.skeleton.provider.Entity.Device;
import eu.arrowhead.client.skeleton.provider.Entity.ServiceDBObject;
import eu.arrowhead.client.skeleton.provider.JSONReader;
import eu.arrowhead.client.skeleton.provider.Service.DeviceService;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.jose4j.json.internal.json_simple.JSONArray;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.json.internal.json_simple.parser.JSONParser;
import org.jose4j.json.internal.json_simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import eu.arrowhead.client.skeleton.provider.OPC_UA.*;
import org.springframework.http.HttpHeaders;

import com.jcraft.jsch.JSchException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.CommonConstants;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/factory")
public class ProviderController {
	//=================================================================================================
	// members
	@Value("${opc.ua.connection_address}")
	private String opcuaServerAddress;

	@Value("${opc.ua.root_node_namespace}")
	private int rootNodeNamespaceIndex;

	@Value("${opc.ua.root_node_identifier}")
	private String rootNodeIdentifier;

	public ProviderController() throws IOException, ParseException {
	}

	//DeviceService devices= new DeviceService();

	@RequestMapping(path = "monitor/echo")
	@ResponseBody
	public String echoService() {
		return "Got it!";
	}

	@GetMapping(path = "monitor/services")
	@ResponseBody
	public List<ServiceDBObject> monitorAllServices() throws SQLException {
		return DeviceService.getAllServices();
	}
	@GetMapping(path = "monitor/services/{serviceName}")
	@ResponseBody
	public ServiceDBObject monitorService(@PathVariable(name="serviceName") String servicedef) throws SQLException {
		return DeviceService.getService(servicedef);
	}

	public String end="";
	@GetMapping(path = "/sensors")
	@ResponseBody
	public List<Device> getSensors() throws IOException, ParseException {
		return DeviceService.getAllSensors();
	}

	@GetMapping(path = "/actuators")
	@ResponseBody
	public List<Device> getActuators() throws IOException, ParseException {
		return DeviceService.getAllActuators();
	}

	@GetMapping(path = "/sensors/{sensorId}")
	@ResponseBody
	public Device getsensor(@PathVariable(name = "sensorId") String id) throws IOException, ParseException {
		return DeviceService.getSensor(id);
	}

	@GetMapping(path = "/sensors/I5")
	@ResponseBody
	public Device getsensor() throws IOException, ParseException {
		return DeviceService.getSensor("I5");
	}

	@GetMapping(path = "/actuators/{actuatorId}")
	@ResponseBody
	public Device getActuator(@PathVariable(name = "actuatorId") String id) throws IOException, ParseException {
		return DeviceService.getActuator(id);
	}

	@PutMapping(path = "/actuators/{actuatorId}/{value}")
	@ResponseBody
	public Device UpdateActuator(@PathVariable(name = "actuatorId") String id,@PathVariable(name = "value") String val) throws IOException, ParseException {
		Device updatedDevice=DeviceService.UpdateActuator(id, val);
		return updatedDevice;
	}
	
	//-------------------------------------------------------------------------------------------------
	//TODO: implement here your provider related REST end points
	// FIXME Double-check that the token security prevents tampering with variables in the OPC-UA it is not supposed to access (I.e. only allows access to the variables in the Service Registry)
	//-------------------------------------------------------------------------------------------------
	@RequestMapping(path = "/read/variable")
	@ResponseBody
	public String readVariableNode(@RequestParam(name = "DeviceType") final String DeviceType, @RequestParam(name = "Instance") final String Instance, @RequestParam(name = "Device") final String Device, @RequestParam(name = "Location") final String Location, @RequestParam(name = "nodeIdentifier") final String nodeIdentifier) {
		System.out.println("Got a read variable request:" + Device + "/" + DeviceType + "/" + Location+ "/" + Instance);
		NodeId nodeId = new NodeId(rootNodeNamespaceIndex, nodeIdentifier);
		opcuaServerAddress = opcuaServerAddress.replaceAll("opc.tcp://", "");
		OPCUAConnection connection = new OPCUAConnection(opcuaServerAddress);
		String body = "";
		try {
			body = OPCUAInteractions.readNode(connection.getConnectedClient(), nodeId);
			connection.dispose();
			//String Val = body.replace("Variant{value=","{");
			return body;
		} catch (Exception ex) {
			connection.dispose();
			return "There was an error reading the OPC-UA node.";
		}
	}

	@GetMapping(path ="/read")
	@ResponseBody
	public JSONReader readStatus(@RequestParam(value= "name", defaultValue = "all") final String name) throws IOException, ParseException {

		//String status= JSONReader.getServiceDefinition(name);
		return new JSONReader(name);
	}

	@RequestMapping(path = "read/Sensors")
	@ResponseBody
	public String readVariableNode1() throws IOException, ParseException {
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader("client-skeleton-provider/src/main/resources/SR_Entry.json"));
		JSONObject jsonObject =  (JSONObject) obj;
		JSONArray Services = (JSONArray) jsonObject.get("Services");
		Iterator<JSONObject> iterator = Services.iterator();
		NodeId nodeId = new NodeId(rootNodeNamespaceIndex, rootNodeIdentifier);
		String [] body = new String[29];
		int i=0;
		while (iterator.hasNext()) {
			JSONObject iter = iterator.next();
			String serviceDef = iter.get("ServiceDef").toString();
			JSONObject mdata = (JSONObject) iter.get("MetaData");
			String device = mdata.get("Device").toString();
			if(device.equalsIgnoreCase("Sensor")){
				nodeId = new NodeId(rootNodeNamespaceIndex, rootNodeIdentifier+".\""+serviceDef+"\"");
				opcuaServerAddress = opcuaServerAddress.replaceAll("opc.tcp://", "");
				OPCUAConnection connection = new OPCUAConnection(opcuaServerAddress);
				try {
					body[i] = serviceDef+": "+OPCUAInteractions.readNode(connection.getConnectedClient(), nodeId);
					connection.dispose();
					i++;
				} catch (Exception ex) {
					connection.dispose();
					return "There was an error reading the OPC-UA node.";
			    }
			}
		}
		String returnval="";
		int count=0;
		for(int k=0;k<body.length; k++){
			if(body[k]!= null)
				count++;
		}
		for(int j=0; j<count;j++){
			returnval=String.format("%s\n%s",returnval+", ",body[j]);
		}
		return returnval;
	}

	@RequestMapping(path = "/write/variable")
	@ResponseBody
	public String writeVariableNode(@RequestParam(name = "opcuaNodeId") final String identifier, @RequestParam(name = "value") final String value) {
		System.out.println("Got a write variable request:" + opcuaServerAddress + "/" + rootNodeNamespaceIndex + "/" + identifier + " value: " + value);
		NodeId nodeId = new NodeId(rootNodeNamespaceIndex, identifier);
		boolean bolValue= false;
		if(value.equalsIgnoreCase ("true"))
			bolValue=true;
		else bolValue=false;
		String opcuaServerAddress1 = opcuaServerAddress.replaceAll("opc.tcp://", "");
		OPCUAConnection connection = new OPCUAConnection(opcuaServerAddress1);
		String body = "Wrote value: " + bolValue;

		try {
			String status = OPCUAInteractions.writeNode(connection.getConnectedClient(), nodeId, bolValue);
			//StatusCode status2 = OPCUAInteractions.writeNode2(connection.getConnectedClient(), nodeId, value).get();
			System.out.println("Status Code: " + status);
			connection.dispose();
			return body;
		} catch (Exception ex) {
			connection.dispose();
			return "There was an error writing to the OPC-UA node.";
		}
	}
	@RequestMapping("*")
	@ResponseBody
	public String fallbackMethod(){
		return "fallback method";
	}
}
