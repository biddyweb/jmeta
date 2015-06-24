package org.meta.plugin.webservice.forms;

import org.bson.BasicBSONObject;


public abstract class InterfaceField extends InterfaceElement {

    protected String label = "";
    protected String value = "";

    public InterfaceField(String id, String label) {
        super(id);
        this.label = label;
    }

    public void setValue(String value){
        this.value = value;
        if(this.value == null)
            this.value = "";
    }

    public BasicBSONObject toJson() {
        BasicBSONObject o = super.toJson();
        o.append("label", label);
        o.append("value", value);
        return o;
    }

}
