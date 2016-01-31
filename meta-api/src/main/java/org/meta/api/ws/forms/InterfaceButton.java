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
package org.meta.api.ws.forms;

import org.bson.BasicBSONObject;

/**
 * A button is a special element that is not a field.
 * used to give to the user where to send the interface execution.
 *
 * Has a label
 *
 * @author faquin
 * @version $Id: $
 */
public abstract class InterfaceButton extends InterfaceElement {

    /**
     *
     */
    protected String label = "";

    /**
     * <p>Constructor for InterfaceButton.</p>
     *
     * @param id a {@link java.lang.String} object.
     * @param label a {@link java.lang.String} object.
     */
    public InterfaceButton(String id, String label) {
        super(id);
        this.label = label;
    }
    
    /**
     * Change the label of the button
     *
     * @param label the new label, will be set to an empty string if null
     */
    public void setLabel(String label){
        this.label = label;
        if(this.label == null)
            this.label = "";
    }
    
    /**
     * Render as simple json.
     *
     * @return a {@link org.bson.BasicBSONObject} object.
     */
    public BasicBSONObject toJson() {
        //get parent json and add attributes
        BasicBSONObject o = super.toJson();
        o.append("label", label);
        return o;
    }

}
