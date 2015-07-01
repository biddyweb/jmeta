package org.meta.plugin.webservice.forms;

import org.bson.BasicBSONObject;

/**
 * A button is a special element that is not a field.
 * used to give to the user where to send the interface execution.
 * 
 * Has a label
 * 
 * @author faquin
 *
 */
public abstract class InterfaceButton extends InterfaceElement {

    protected String label = "";

    public InterfaceButton(String id, String label) {
        super(id);
        this.label = label;
    }
    
    /**
     * Render as simple json.
     */
    public BasicBSONObject toJson() {
        //get parent json and add attributes
        BasicBSONObject o = super.toJson();
        o.append("label", label);
        return o;
    }

}
