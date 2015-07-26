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
 * A field represent an Input or an Ouptut for the interface.
 * By the way it is the super class of all inpu and output elements.
 * 
 * Extends Interface element and add two extra informations :
 * - label (for human purposes)
 * - value (default value, or gotten value thru command execution)
 * - description (for human purposes)
 * 
 * @author faquin
 *
 */
public abstract class InterfaceField extends InterfaceElement {

    /**
     *
     */
    protected String label       = "";

    /**
     *
     */
    protected String value       = "";

    /**
     *
     */
    protected String description = "";

    /**
     * Build this field with values and id
     * @param id            unique ID
     * @param label         label of the element (for human purposes)
     * @param description   description of the element
     */
    public InterfaceField(String id, String label, String description) {
        super(id);
        this.label = label;
        this.description = description;
    }
    
    /**
     * Build this field with values and id
     * @param id    unique ID
     * @param label label of the element (for human purposes)
     */
    public InterfaceField(String id, String label) {
        super(id);
        this.label = label;
    }

    /**
     * Change the value of the element
     * @param value the new value, if null, set to an empty string
     */
    public void setValue(String value){
        this.value = value;
        if(this.value == null)
            this.value = "";
    }

    /**
     * Change the description of the element
     * @param description the new description, if null, set to an empty string
     */
    public void setDescription(String description){
        this.description = description;
        if(this.description == null)
            this.description = "";
    }

    /**
     * Render as simple json.
     * @return 
     */
    public BasicBSONObject toJson() {
        //get parent json and add attributes
        BasicBSONObject o = super.toJson();
        o.append("label", label);
        o.append("value", value);
        o.append("description", description);
        return o;
    }

}
