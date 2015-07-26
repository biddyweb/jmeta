/*
 *
 * JMeta - Meta's java implementation
 *
 * Copyright (C) 2013-2015 Pablo Joubert
 * Copyright (C) 2013-2015 Thomas Lavocat
 * Copyright (C) 2013-2015 Nicolas Michon
 *
 * This file is part of JMeta.
 *
 * JMeta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * JMeta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
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
