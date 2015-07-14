package org.meta.api.ws.forms.fields;

import org.bson.BasicBSONObject;
import org.meta.api.ws.forms.InterfaceField;


/**
 * A text outuput is a simple interfaceField who allow you to put raw data in it
 * 
 * Please, remember that, even if we give a web interface, we hobe 
 * that a curse interface will born one day, so please, do not put any HTML, 
 * JS, CSS, or other thing like that in it.
 * 
 * @author faquin
 *
 */
public class TextOutput extends InterfaceField {

    StringBuilder buffer = null;

    /**
     * 
     * @param id    Unique ID
     * @param label Label
     */
    public TextOutput(String id, String label) {
        super(id, label);
        buffer = new StringBuilder();
    }

    /**
     * Append a message into the output, a line feed will be added after.
     * @param message a simple string message
     */
    public void append(String message){
        buffer.append(message+"\n");
    }

    /**
     * clear the text output
     */
    public void flush(){
        buffer = new StringBuilder();
    }

    /**
     * serialize as JSON
     */
    public BasicBSONObject toJson() {
        //get mama's json append the string buffer
        BasicBSONObject o = super.toJson();
        o.append("message", buffer.toString());
        return o;
    }

    @Override
    protected String getType() {
        return "TextOutput";
    }

}
