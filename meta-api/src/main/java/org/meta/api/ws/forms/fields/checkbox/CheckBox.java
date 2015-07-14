package org.meta.api.ws.forms.fields.checkbox;

import org.bson.BasicBSONObject;
import org.meta.api.ws.forms.InterfaceField;

/**
 * A Check box .
 * Can exist outside a {@link CheckBoxLists}
 * @author faquin
 *
 */
public class CheckBox extends InterfaceField{
    
    private boolean checked = false; 

    /**
     * 
     * @param id    unique ID
     * @param label label
     */
    public CheckBox(String id, String label) {
        super(id, label);
    }

    /**
     * 
     * @param checked true if checked
     */
    public void setChecked(boolean checked){
        this.checked = checked;
    }

    @Override
    protected String getType() {
        return "checkBox";
    }

    @Override
    public BasicBSONObject toJson() {
        //get mama's json an add checked value
        BasicBSONObject o = super.toJson();
        o.append("checked", checked);
        return o;
    }

}
