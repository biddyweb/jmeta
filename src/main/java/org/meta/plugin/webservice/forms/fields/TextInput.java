package org.meta.plugin.webservice.forms.fields;

import org.meta.plugin.webservice.forms.InterfaceField;


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
