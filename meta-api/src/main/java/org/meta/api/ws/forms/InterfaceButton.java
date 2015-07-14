package org.meta.api.ws.forms;

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
     * Change the label of the button
     * @param label the new label, will be set to an empty string if null
     */
    public void setLabel(String label){
        this.label = label;
        if(this.label == null)
            this.label = "";
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
