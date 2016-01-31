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
package org.meta.api.ws.forms.fields.select;

import org.bson.BasicBSONObject;
import org.meta.api.ws.forms.InterfaceField;

/**
 * A select is an option in a {@link SelectList} has no sense living outside a
 * {@link SelectList}
 *
 * @author faquin
 * @version $Id: $
 */
public class Select extends InterfaceField {

    private boolean selected = false;

    /**
     * <p>Constructor for Select.</p>
     *
     * @param id Unique ID
     * @param label Label
     */
    public Select(String id, String label) {
        super(id, label);
    }

    /**
     * <p>Setter for the field <code>selected</code>.</p>
     *
     * @param selected true if you want this to be selected by default
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /** {@inheritDoc} */
    @Override
    protected String getType() {
        return "select";
    }

    /** {@inheritDoc} */
    @Override
    public BasicBSONObject toJson() {
        //get mama's json and add the option selected or not
        BasicBSONObject o = super.toJson();
        o.append("selected", selected);
        return o;
    }

}
