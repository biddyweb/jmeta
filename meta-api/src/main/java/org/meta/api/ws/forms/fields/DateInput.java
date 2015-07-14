package org.meta.api.ws.forms.fields;

import org.meta.api.ws.forms.InterfaceField;


/**
 * A simple date input, as extending InterfaceField, can have an optional 
 * default value
 * @author faquin
 *
 */
public class DateInput extends InterfaceField {

    /**
     * 
     * @param id    Unique ID
     * @param label label
     */
    public DateInput(String id, String label) {
        super(id, label);
    }

    @Override
    protected String getType() {
        return "DateInput";
    }

}
