package org.meta.api.ws.forms.fields.checkbox;

import java.util.ArrayList;
import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;
import org.meta.api.ws.forms.InterfaceField;


/**
 * A CheckBox list is a list of grouped {@link CheckBox}.
 * 
 * @author faquin
 *
 */
public class CheckBoxLists extends InterfaceField {

    private ArrayList<CheckBox> checkBoxes = null;

    /**
     * 
     * @param id            Unique ID
     * @param label         label
     * @param checkBoxes    list of {@link CheckBox}s
     */
    public CheckBoxLists(String id, String label, ArrayList<CheckBox> checkBoxes) {
        super(id, label);
        this.checkBoxes = checkBoxes;
    }

    @Override
    public BasicBSONObject toJson() {
        //get mama's json and add the list of checboxes
        BasicBSONObject checkBoxLists     = super.toJson();
        BasicBSONList   checkBox = new BasicBSONList();
        for(int i=0; i<checkBoxes.size(); i++){
            checkBox.put(i, checkBoxes.get(i).toJson());
        }
        checkBoxLists.append("content", checkBox);
        return checkBoxLists;
    }

    @Override
    protected String getType() {
        return "checkBoxList";
    }

}
