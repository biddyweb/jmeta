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

import org.bson.BasicBSONObject;
import org.meta.api.ws.forms.InterfaceField;

/**
 * A Check box .
 * Can exist outside a {@link CheckBoxLists}
 *
 * @author faquin
 * @version $Id: $
 */
public class CheckBox extends InterfaceField{
    
    private boolean checked = false; 

    /**
     * <p>Constructor for CheckBox.</p>
     *
     * @param id    unique ID
     * @param label label
     */
    public CheckBox(String id, String label) {
        super(id, label);
    }

    /**
     * <p>Setter for the field <code>checked</code>.</p>
     *
     * @param checked true if checked
     */
    public void setChecked(boolean checked){
        this.checked = checked;
    }

    /** {@inheritDoc} */
    @Override
    protected String getType() {
        return "checkBox";
    }

    /** {@inheritDoc} */
    @Override
    public BasicBSONObject toJson() {
        //get mama's json an add checked value
        BasicBSONObject o = super.toJson();
        o.append("checked", checked);
        return o;
    }

}
