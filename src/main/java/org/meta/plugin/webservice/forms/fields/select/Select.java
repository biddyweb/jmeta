package org.meta.plugin.webservice.forms.fields.select;

import org.bson.BasicBSONObject;
import org.meta.plugin.webservice.forms.InterfaceField;

public class Select extends InterfaceField{
    private boolean selected = false; 

    public Select(String id, String label) {
        super(id, label);
    }
    public void setSelected(boolean selected){
        this.selected = selected;
    }

    @Override
    protected String getType() {
        return "select";
    }
    @Override
    public BasicBSONObject toJson() {
        BasicBSONObject o = super.toJson();
        o.append("selected", selected);
        return o;
    }


}
