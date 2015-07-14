package org.meta.api.ws.forms.submit;

import org.bson.BasicBSONObject;
import org.meta.api.ws.forms.InterfaceButton;

/**
 * A submitTo button is a button who will say to the interface :
 * "send your filled form to an other command of the same plugin"
 * 
 * That way you are able to create many commands that chained together.
 * 
 * Your commands are always independent, and may be executed separately.
 * Chained them is just a big plus for end user experience.
 * 
 * after clicking on submit from this button, the interface will execute the
 * pointed command calling :
 * execute/PluginName/destinationCommand?parameters
 * 
 * @author faquin
 *
 */
public class SubmitToButton extends InterfaceButton{

    private String destination     = null;

    /**
     * 
     * @param id            unique ID
     * @param label         label
     * @param destination   command name to execute
     */
    public SubmitToButton(String id, String label, String destination) {
        super(id, label);
        this.destination = destination;
    }

    @Override
    protected String getType() {
        return "submitToButton";
    }

    /**
     * Serialize as JSON
     */
    public BasicBSONObject toJson() {
        BasicBSONObject o = super.toJson();
        o.append("destination", destination);
        return o;
    }
}
