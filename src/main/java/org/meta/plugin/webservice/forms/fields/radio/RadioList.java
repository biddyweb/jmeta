package org.meta.plugin.webservice.forms.fields.radio;

import java.util.ArrayList;

import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;
import org.meta.plugin.webservice.forms.InterfaceField;

/**
 * A radioList is a list of grouped radio buttons.
 * 
 * @author faquin
 *
 */
public class RadioList extends InterfaceField {

    private ArrayList<RadioButton> buttons = null;

    /**
     * 
     * @param id        Unique ID
     * @param label     Label
     * @param radios    list of {@link RadioButton}
     */
    public RadioList(String id, String label) {
        super(id, label);
        this.buttons = new ArrayList<RadioButton>();
    }
    
    /**
     * 
     * @param id        Unique ID
     * @param label     Label
     * @param radios    list of {@link RadioButton}
     */
    public RadioList(String id, String label, ArrayList<RadioButton> radios) {
        super(id, label);
        this.buttons = radios;
    }

    @Override
    public BasicBSONObject toJson() {
        //get mama's json and add the list of radio button
        BasicBSONObject radioList     = super.toJson();
        BasicBSONList   radios = new BasicBSONList();
        for(int i=0; i<buttons.size(); i++){
            radios.put(i, buttons.get(i).toJson());
        }
        radioList.append("content", radios);
        return radioList;
    }

    @Override
    protected String getType() {
        return "radioList";
    }

    /**
     * 
     * @param buttons give a new lists of buttons
     */
    public void setButtons(ArrayList<RadioButton> buttons){
        this.buttons = buttons;
    }
}
