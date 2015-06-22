package org.meta.plugin.webservice.forms;

import org.bson.BasicBSONObject;


public abstract class InterfaceButton extends InterfaceElement {

    protected String label = "";

    public InterfaceButton(String id, String label) {
        super(id);
        this.label = label;
    }

    public BasicBSONObject toJson() {
        BasicBSONObject o = super.toJson();
        o.append("label", label);
        return o;
    }

}
