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
package org.meta.api.ws.forms.submit;

import org.bson.BasicBSONObject;
import org.meta.api.ws.forms.InterfaceButton;

/**
 * A submitTo button is a button who will say to the interface :
 * "send your filled form to an other command of the same plugin"
 * 
 * That way you are able to create many commands that chained together.
 * 
 * Your commands are always independent, and may be executed separately.
 * Chained them is just a big plus for end user experience.
 * 
 * after clicking on submit from this button, the interface will execute the
 * pointed command calling :
 * execute/PluginName/destinationCommand?parameters
 * 
 * @author faquin
 *
 */
public class SubmitToButton extends InterfaceButton{

    private String destination     = null;

    /**
     * 
     * @param id            unique ID
     * @param label         label
     * @param destination   command name to execute
     */
    public SubmitToButton(String id, String label, String destination) {
        super(id, label);
        this.destination = destination;
    }

    @Override
    protected String getType() {
        return "submitToButton";
    }

    /**
     * Serialize as JSON
     * @return 
     */
    public BasicBSONObject toJson() {
        BasicBSONObject o = super.toJson();
        o.append("destination", destination);
        return o;
    }
}
