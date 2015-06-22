package org.meta.plugin.webservice.forms.fields;

import org.bson.BasicBSONObject;
import org.meta.plugin.webservice.forms.InterfaceField;


/**
 *
 * @author faquin
 *
 */
public class TextOutput extends InterfaceField {

    StringBuilder buffer = null;

    public TextOutput(String id, String label) {
        super(id, label);
        buffer = new StringBuilder();
    }

    public void append(String message){
        buffer.append(message+"\n");
    }

    public void flush(){
        buffer = new StringBuilder();
    }

    public BasicBSONObject toJson() {
        BasicBSONObject o = super.toJson();
        o.append("message", buffer.toString());
        return o;
    }

    @Override
    protected String getType() {
        return "TextOutput";
    }

}
