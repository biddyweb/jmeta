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

import java.util.ArrayList;
import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;
import org.meta.api.ws.forms.InterfaceField;

/**
 * Correspond to a Select liste between optional elements
 * Contains a list of {@link Select}
 *
 * @author faquin
 * @version $Id: $
 */
public class SelectList extends InterfaceField {

    private ArrayList<Select> selectList = null;

    /**
     * <p>Constructor for SelectList.</p>
     *
     * @param id        Unique ID
     * @param label     Label
     * @param selects   a list of {@link Select}
     */
    public SelectList(String id, String label, ArrayList<Select> selects) {
        super(id, label);
        this.selectList = selects;
    }

    /** {@inheritDoc} */
    @Override
    public BasicBSONObject toJson() {
        //get mamas JSON and add the children
        BasicBSONObject selectList     = super.toJson();
        BasicBSONList   select = new BasicBSONList();
        for(int i=0; i<this.selectList.size(); i++){
            select.put(i, this.selectList.get(i).toJson());
        }
        selectList.append("content", select);
        return selectList;
    }

    /** {@inheritDoc} */
    @Override
    protected String getType() {
        return "selectList";
    }

}
