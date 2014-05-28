package org.meta.plugin.webservice.forms.fields.select;

import java.util.ArrayList;

import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;
import org.meta.plugin.webservice.forms.InterfaceElement;

public class SelectList extends InterfaceElement {

	private ArrayList<Select> selectList = null;
	
	public SelectList(String id, ArrayList<Select> buttons) {
		super(id);
		this.selectList = buttons;
	}

	@Override
	public BasicBSONObject toJson() {
		BasicBSONObject selectList 	= super.toJson();
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
