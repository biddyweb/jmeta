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
 *  Is the super class of all Interface elements
 *
 *  @author faquin
 * @version $Id: $
 */
public abstract class InterfaceElement {

    private String     id = null;

    /**
     * <p>Constructor for InterfaceElement.</p>
     *
     * @param id ID of the element, shall be unique in your interface
     */
    public InterfaceElement(String id){
        this.id = id;
    }

    /**
     * <p>toJson</p>
     *
     * @return This as JSON
     */
    public BasicBSONObject toJson() {
       //Create a new Json objet and set all we know (id)
        BasicBSONObject bson = new BasicBSONObject("type", getType());
        bson.append("id", id);
        return bson;

    }
    
    /**
     * <p>getType</p>
     *
     * @return the type of th element
     */
    protected abstract String getType();

    /**
     * <p>Getter for the field <code>id</code>.</p>
     *
     * @return the (unique ? ;-) ) ID
     */
    public String getId() {
        return id;
    }

    /**
     * May be used to change an element ID
     *
     * @param id another unique ID
     */
    public void setId(String id) {
        this.id = id;
    }

}
