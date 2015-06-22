package org.meta.plugin.webservice.forms.submit;

import org.meta.plugin.webservice.forms.InterfaceButton;

public class SelfSubmitButton extends InterfaceButton{

    public SelfSubmitButton(String id, String label) {
        super(id, label);
    }

    @Override
    protected String getType() {
        return "selfSubmitButton";
    }

}
