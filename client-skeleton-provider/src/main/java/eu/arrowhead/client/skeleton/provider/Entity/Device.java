package eu.arrowhead.client.skeleton.provider.Entity;

import eu.arrowhead.client.skeleton.provider.JSONReader;
import eu.arrowhead.client.skeleton.provider.OPC_UA.OPCUAService;
import org.jose4j.json.internal.json_simple.parser.ParseException;

import java.io.IOException;

public class Device {
    private String id;
    private String Definition;
    private String Value;

    public Device() {
    }

    public Device(String id) throws IOException, ParseException {
        this.id = id;
        JSONReader reader= new JSONReader(id);
        OPCUAService service= new OPCUAService();
        this.Definition = reader.getDefinition();
        this.Value = service.read(id) ;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDefinition() {
        return Definition;
    }

    public void setDefinition(String definition) {
        Definition = definition;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }
}
