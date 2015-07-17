package org.meta.api.ws.forms.fields.select;

import java.util.ArrayList;
import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;
import org.meta.api.ws.forms.InterfaceField;

/**
 * Correspond to a Select liste between optional elements
 * Contains a list of {@link Select}
 * @author faquin
 *
 */
public class SelectList extends InterfaceField {

    private ArrayList<Select> selectList = null;

    /**
     * 
     * @param id        Unique ID
     * @param label     Label
     * @param selects   a list of {@link Select}
     */
    public SelectList(String id, String label, ArrayList<Select> selects) {
        super(id, label);
        this.selectList = selects;
    }

    @Override
    public BasicBSONObject toJson() {
        //get mamas JSON and add the children
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
