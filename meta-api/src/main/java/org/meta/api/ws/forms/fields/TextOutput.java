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
package org.meta.api.ws.forms.fields;

import org.bson.BasicBSONObject;
import org.meta.api.ws.forms.InterfaceField;


/**
 * A text outuput is a simple interfaceField who allow you to put raw data in it
 *
 * Please, remember that, even if we give a web interface, we hobe
 * that a curse interface will born one day, so please, do not put any HTML,
 * JS, CSS, or other thing like that in it.
 *
 * @author faquin
 * @version $Id: $
 */
public class TextOutput extends InterfaceField {

    StringBuilder buffer = null;

    /**
     * <p>Constructor for TextOutput.</p>
     *
     * @param id    Unique ID
     * @param label Label
     */
    public TextOutput(String id, String label) {
        super(id, label);
        buffer = new StringBuilder();
    }

    /**
     * Append a message into the output, a line feed will be added after.
     *
     * @param message a simple string message
     */
    public void append(String message){
        buffer.append(message+"\n");
    }

    /**
     * clear the text output
     */
    public void flush(){
        buffer = new StringBuilder();
    }

    /**
     * serialize as JSON
     *
     * @return a {@link org.bson.BasicBSONObject} object.
     */
    public BasicBSONObject toJson() {
        //get mama's json append the string buffer
        BasicBSONObject o = super.toJson();
        o.append("message", buffer.toString());
        return o;
    }

    /** {@inheritDoc} */
    @Override
    protected String getType() {
        return "TextOutput";
    }

}
