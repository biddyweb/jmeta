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
package org.meta.api.amp;

import java.util.ArrayList;
import org.meta.api.model.Searchable;

/**
 * Create a new AmpAnswerFactory to create an answer.
 *
 * @author faquin
 *
 */
public class AMPAnswerBuilder extends AMPBuilder {

    /**
     *
     * @param answer the same number as the ask
     * @param datas the Searchable objects to send back
     */
    public AMPAnswerBuilder(final String answer, final ArrayList<Searchable> datas) {
        //write the answer number and the count of datas send
//        LinkedHashMap<String, byte[]> map = new LinkedHashMap<>();
//        map.put("_answer", answer.getBytes());
//        map.put("_nbDatas", Integer.toString(datas.size()).getBytes());
//
//        int nb = 0;
//        //Foreach Searchable data, get the AMPMessagePart
//        for (Iterator<Searchable> i = datas.iterator(); i.hasNext(); nb++) {
//            Searchable searchable = (Searchable) i.next();
//            LinkedHashMap<String, byte[]> fragment = searchable.getAmpAnswerPart();
//            for (Iterator<String> j = fragment.keySet().iterator(); j.hasNext();) {
//                String key = (String) j.next();
//                map.put("_" + nb + key, fragment.get(key));
//            }
//        }
//        //finally build the map
//        super.build(map);
    }
}
