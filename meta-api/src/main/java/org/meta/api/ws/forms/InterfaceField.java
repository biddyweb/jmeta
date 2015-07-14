package org.meta.api.ws.forms;

import org.bson.BasicBSONObject;

/**
 * A field represent an Input or an Ouptut for the interface.
 * By the way it is the super class of all inpu and output elements.
 * 
 * Extends Interface element and add two extra informations :
 * - label (for human purposes)
 * - value (default value, or gotten value thru command execution)
 * - description (for human purposes)
 * 
 * @author faquin
 *
 */
public abstract class InterfaceField extends InterfaceElement {

    protected String label       = "";
    protected String value       = "";
    protected String description = "";

    /**
     * Build this field with values and id
     * @param id            unique ID
     * @param label         label of the element (for human purposes)
     * @param description   description of the element
     */
    public InterfaceField(String id, String label, String description) {
        super(id);
        this.label = label;
        this.description = description;
    }
    
    /**
     * Build this field with values and id
     * @param id    unique ID
     * @param label label of the element (for human purposes)
     */
    public InterfaceField(String id, String label) {
        super(id);
        this.label = label;
    }

    /**
     * Change the value of the element
     * @param value the new value, if null, set to an empty string
     */
    public void setValue(String value){
        this.value = value;
        if(this.value == null)
            this.value = "";
    }

    /**
     * Change the description of the element
     * @param description the new description, if null, set to an empty string
     */
    public void setDescription(String description){
        this.description = description;
        if(this.description == null)
            this.description = "";
    }

    /**
     * Render as simple json.
     */
    public BasicBSONObject toJson() {
        //get parent json and add attributes
        BasicBSONObject o = super.toJson();
        o.append("label", label);
        o.append("value", value);
        o.append("description", description);
        return o;
    }

}
