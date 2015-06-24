package org.meta.plugin.webservice.forms.fields.checkbox;

import org.bson.BasicBSONObject;
import org.meta.plugin.webservice.forms.InterfaceField;

public class CheckBox extends InterfaceField{
    
    private boolean checked = false; 
    
    public CheckBox(String id, String label) {
        super(id, label);
    }

    public void setChecked(boolean checked){
        this.checked = checked;
    }

    @Override
    protected String getType() {
        return "checkBox";
    }

    @Override
    public BasicBSONObject toJson() {
        BasicBSONObject o = super.toJson();
        o.append("checked", checked);
        return o;
    }

}
