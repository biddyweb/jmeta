package org.meta.plugin.webservice.forms.fields.radio;

import java.util.ArrayList;

import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;
import org.meta.plugin.webservice.forms.InterfaceField;

public class RadioList extends InterfaceField {

    private ArrayList<RadioButton> buttons = null;

    public RadioList(String id, String label, ArrayList<RadioButton> buttons) {
        super(id, label);
        this.buttons = buttons;
    }

    @Override
    public BasicBSONObject toJson() {
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

}
