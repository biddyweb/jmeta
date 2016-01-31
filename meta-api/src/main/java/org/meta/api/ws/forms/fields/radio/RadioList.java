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

import java.util.ArrayList;
import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;
import org.meta.api.ws.forms.InterfaceField;

/**
 * A radioList is a list of grouped radio buttons.
 *
 * @author faquin
 * @version $Id: $
 */
public class RadioList extends InterfaceField {

    private ArrayList<RadioButton> buttons = null;

    /**
     * <p>Constructor for RadioList.</p>
     *
     * @param id        Unique ID
     * @param label     Label
     */
    public RadioList(String id, String label) {
        super(id, label);
        this.buttons = new ArrayList<RadioButton>();
    }
    
    /**
     * <p>Constructor for RadioList.</p>
     *
     * @param id        Unique ID
     * @param label     Label
     * @param radios    list of {@link RadioButton}
     */
    public RadioList(String id, String label, ArrayList<RadioButton> radios) {
        super(id, label);
        this.buttons = radios;
    }

    /** {@inheritDoc} */
    @Override
    public BasicBSONObject toJson() {
        //get mama's json and add the list of radio button
        BasicBSONObject radioList     = super.toJson();
        BasicBSONList   radios = new BasicBSONList();
        for(int i=0; i<buttons.size(); i++){
            radios.put(i, buttons.get(i).toJson());
        }
        radioList.append("content", radios);
        return radioList;
    }

    /** {@inheritDoc} */
    @Override
    protected String getType() {
        return "radioList";
    }

    /**
     * <p>Setter for the field <code>buttons</code>.</p>
     *
     * @param buttons give a new lists of buttons
     */
    public void setButtons(ArrayList<RadioButton> buttons){
        this.buttons = buttons;
    }
}
