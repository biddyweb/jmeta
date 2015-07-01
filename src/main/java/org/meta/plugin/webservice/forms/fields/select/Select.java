package org.meta.plugin.webservice.forms.fields.select;

import org.bson.BasicBSONObject;
import org.meta.plugin.webservice.forms.InterfaceField;

/**
 * A select is an option in a {@link SelectList}
 * has no sense living outside a {@link SelectList}
 * @author faquin
 *
 */
public class Select extends InterfaceField{
    private boolean selected = false; 

    /**
     * 
     * @param id    Unique ID
     * @param label Label
     */
    public Select(String id, String label) {
        super(id, label);
    }
    /**
     * 
     * @param selected true if you want this to be selected by default
     */
    public void setSelected(boolean selected){
        this.selected = selected;
    }

    @Override
    protected String getType() {
        return "select";
    }
    @Override
    public BasicBSONObject toJson() {
        //get mama's json and add the option selected or not
        BasicBSONObject o = super.toJson();
        o.append("selected", selected);
        return o;
    }


}
