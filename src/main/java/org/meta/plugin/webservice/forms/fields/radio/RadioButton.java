package org.meta.plugin.webservice.forms.fields.radio;

import org.bson.BasicBSONObject;
import org.meta.plugin.webservice.forms.InterfaceField;

public class RadioButton extends InterfaceField{


    private boolean selected = false; 

    public RadioButton(String id, String label) {
        super(id, label);
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }


    @Override
    protected String getType() {
        return "radioButton";
    }

    @Override
    public BasicBSONObject toJson() {
        BasicBSONObject o = super.toJson();
        o.append("selected", selected);
        return o;
    }

}
