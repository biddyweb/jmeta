package org.meta.api.ws.forms.fields;

import org.meta.api.ws.forms.InterfaceField;


/**
 * Simple text input, as extending InterfaceField, can have an optional default 
 * value.
 * @author faquin
 *
 */
public class TextInput extends InterfaceField {

    /**
     * 
     * @param id    Unique ID
     * @param label label
     */
    public TextInput(String id, String label) {
        super(id, label);
    }

    @Override
    protected String getType() {
        return "TextInput";
    }

}
