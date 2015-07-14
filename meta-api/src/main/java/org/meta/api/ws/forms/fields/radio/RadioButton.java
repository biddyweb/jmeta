package org.meta.api.ws.forms.fields.radio;

import org.bson.BasicBSONObject;
import org.meta.api.ws.forms.InterfaceField;

/**
 * A Radio button.
 * Can exist outside a {@link RadioList}
 * @author faquin
 *
 */
public class RadioButton extends InterfaceField{


    private boolean selected = false; 

    /**
     * 
     * @param id    unique ID
     * @param label label
     */
    public RadioButton(String id, String label) {
        super(id, label);
    }

    /**
     * 
     * @param selected true if selected
     */
    public void setSelected(boolean selected){
        this.selected = selected;
    }


    @Override
    protected String getType() {
        return "radioButton";
    }

    @Override
    public BasicBSONObject toJson() {
        //get mama's json and add selected value
        BasicBSONObject o = super.toJson();
        o.append("selected", selected);
        return o;
    }

}
