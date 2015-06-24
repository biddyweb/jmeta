package org.meta.plugin.webservice.forms.fields.select;

import java.util.ArrayList;

import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;
import org.meta.plugin.webservice.forms.InterfaceField;

public class SelectList extends InterfaceField {

    private ArrayList<Select> selectList = null;

    public SelectList(String id, String label, ArrayList<Select> buttons) {
        super(id, label);
        this.selectList = buttons;
    }

    @Override
    public BasicBSONObject toJson() {
        BasicBSONObject selectList     = super.toJson();
        BasicBSONList   select = new BasicBSONList();
        for(int i=0; i<this.selectList.size(); i++){
            select.put(i, this.selectList.get(i).toJson());
        }
        selectList.append("content", select);
        return selectList;
    }

    @Override
    protected String getType() {
        return "selectList";
    }

}
