package org.meta.api.ws.forms.submit;

import org.meta.api.ws.forms.InterfaceButton;

/**
 * A self submit button is a button who will say to interface 
 * "execute the command where I came from with you filled form."
 * 
 * It has an Id (has ALL elements, unique by the way) and a label.
 * 
 * @author faquin
 *
 */
public class SelfSubmitButton extends InterfaceButton{

    /**
     * 
     * @param id    unique ID
     * @param label label
     */
    public SelfSubmitButton(String id, String label) {
        super(id, label);
    }

    @Override
    protected String getType() {
        return "selfSubmitButton";
    }

}
