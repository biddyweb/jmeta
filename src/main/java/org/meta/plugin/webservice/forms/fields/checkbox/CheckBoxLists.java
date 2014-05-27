package org.meta.plugin.webservice.forms.fields.checkbox;

import java.util.ArrayList;

import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;
import org.meta.plugin.webservice.forms.InterfaceElement;

public class CheckBoxLists extends InterfaceElement {

	private ArrayList<CheckBox> checkBoxes = null;
	
	public CheckBoxLists(String id, ArrayList<CheckBox> buttons) {
		super(id);
		this.checkBoxes = buttons;
	}

	@Override
	public BasicBSONObject toJson() {
		BasicBSONObject checkBoxLists 	= super.toJson();
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
