package eu.arrowhead.client.skeleton.provider.controller;
import eu.arrowhead.client.skeleton.provider.OPC_UA.OPCUAConnection;
import eu.arrowhead.client.skeleton.provider.OPC_UA.OPCUAInteractions;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.springframework.web.bind.annotation.*;

import static eu.arrowhead.client.skeleton.provider.OPC_UA.OPCUAInteractions.readNode;

@RestController
@RequestMapping("/Sensor")
public class Sensors {

    @GetMapping
    public String hello() {
        return "Hello";
    }
   /* public String readVariableNode(@RequestParam(name = "opcuaServerAddress") final String opcuaServerAddress, @RequestParam(name = "opcuaNamespace") final int namespaceIndex, @RequestParam(name = "opcuaNodeId") final String identifier) {
        System.out.println("Got a read variable request:" + opcuaServerAddress + "/" + namespaceIndex + "/" + identifier);
        NodeId nodeId = new NodeId(namespaceIndex, identifier);

        OPCUAConnection connection = new OPCUAConnection(opcuaServerAddress);
        String body = "";
        try {
            body = OPCUAInteractions.readNode(connection.getConnectedClient(), nodeId);
            connection.dispose();
            return body;
        } catch (Exception ex) {
            connection.dispose();
            return "There was an error reading the OPC-UA node.";
        }*/
}

