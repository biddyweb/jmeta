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

import java.util.ArrayList;
import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;

/**
 *  Is the super class of all organizer.
 *  Has a list of children
 *
 *  Is able to ask his children to serialize as JSON
 *
 *  @author faquin
 * @version $Id: $
 */
public abstract class InterfaceOrganizer extends InterfaceElement{

    private ArrayList<InterfaceElement> childs = null;

    /**
     * <p>Constructor for InterfaceOrganizer.</p>
     *
     * @param id    Id of the column, better if unique.
     */
    public InterfaceOrganizer(String id) {
        super(id);
        childs = new ArrayList<InterfaceElement>();
    }

    /**
     * Return this as JSON, containing his children
     *
     * @return a {@link org.bson.BasicBSONObject} object.
     */
    public BasicBSONObject toJson() {
        //get parent json, and add it all children's json
        BasicBSONObject organiser     = super.toJson();
        BasicBSONList   oChilds     = new BasicBSONList();
        for(int i=0; i<childs.size(); i++){
            oChilds.put(i, childs.get(i).toJson());
        }
        organiser.append("content", oChilds);
        return organiser;
    }

    /**
     * <p>getType</p>
     *
     * @return Interface type
     */
    protected abstract String getType();

    /**
     * Add a child to the interface
     *
     * @param child a {@link org.meta.api.ws.forms.InterfaceElement} object.
     */
    public void addChild(InterfaceElement child){
        childs.add(child);
    }
    
    /**
     * Does what arrayList.remove does
     *
     * @param child a child element to remove
     * @return true if the element was found in the list
     */
    public boolean removeChild(InterfaceElement child){
        return childs.remove(child);
    }
}
