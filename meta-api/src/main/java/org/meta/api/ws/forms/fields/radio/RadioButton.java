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
