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
 * Describe a simple interface to discuss with the web service
 * Need an Interface organizer to exist
 *
 * @author faquin
 * @version $Id: $
 */
public class InterfaceDescriptor {

    private InterfaceOrganizer root = null;

    /**
     * <p>Constructor for InterfaceDescriptor.</p>
     *
     * @param root the interface root
     */
    public InterfaceDescriptor(InterfaceOrganizer root){
        this.root = root;
    }

    /**
     * <p>toJson</p>
     *
     * @return a {@link org.bson.BasicBSONObject} object.
     */
    public BasicBSONObject toJson() {
        return root.toJson();
    }

    /**
     * Set the root of the interface
     *
     * @param root the InterfaceOrganizer to the root Interface
     */
    public void setRoot(InterfaceOrganizer root) {
        this.root = root;
    }

}
