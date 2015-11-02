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
package org.meta.plugin.tcp.amp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.meta.api.model.Searchable;
import org.meta.model.MetaObjectModelFactory;
import org.meta.plugin.tcp.amp.exception.InvalidAMPCommand;
import org.meta.plugin.tcp.amp.exception.InvalidAmpAnswerCommand;

/**
 * Parse an AMP answer.
 *
 * @author faquin
 *
 */
public class AMPAnswerParser extends AMPParser {

    //Do not initialize those variables, because it's made by the mumy
    //in her constructor ;) via the implement method "useContent"
    private String answer;
    private ArrayList<Searchable> datas;

    /**
     *
     * @param bs the data
     * @param factory the model factory
     * @throws InvalidAMPCommand if an invalid value is encountered in the data
     */
    public AMPAnswerParser(final byte[] bs, final MetaObjectModelFactory factory) throws InvalidAMPCommand {
        super(bs, factory);
    }

    @Override
    protected void useContent(final LinkedHashMap<String, byte[]> content) throws InvalidAmpAnswerCommand {
        answer = new String(content.get("_answer"));
        content.remove("_answer");
        datas = new ArrayList<Searchable>();
        extractDatas(content);
    }

    /**
     * Extract {@link Searchable} objects from protocol data.
     *
     * @param content the parsed data
     * @throws InvalidAmpAnswerCommand
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private void extractDatas(final LinkedHashMap<String, byte[]> content) throws InvalidAmpAnswerCommand {
        //get the count of datas
        int nbDatas = Integer.parseInt(new String(content.get("_nbDatas")));
        //and remove it
        content.remove("_nbDatas");

        //Now, for each datas
        for (int i = 0; i < nbDatas; i++) {
            //re-create the fragment
            LinkedHashMap<String, byte[]> fragment = new LinkedHashMap<>();
            //extract the next type
            String type = new String(content.get("_" + i + "_type"));
            //add it in the fragment
            fragment.put("_type", content.get("_" + i + "_type"));
            //remove it from the content.
            content.remove("_" + i + "_type");

            boolean nextTypeFound = false;
            Iterator<String> j = content.keySet().iterator();

            //Until we find the next type, it means we are in the same object
            //So for each element in content
            while (j.hasNext() && !nextTypeFound) {
                //get key and value
                String key = j.next();
                byte[] value = content.get(key);
                //if it's a type stop the loop
                if (key.contains("_type")) {
                    nextTypeFound = true;
                } else {
                    //otherwise, remove the key from content
                    content.put(key, null);
                    //and add the fragment
                    fragment.put(key.replaceFirst("_" + i, ""), value);
                }
            }

            //Now that we have a rebuild fragment
            //We can invoke the object by his type
            //and feed him with this fragment.
//            Class<?> clazz = null;
//            try {
//                clazz = Class.forName(new String(fragment.get("_type")));
//                Searchable searchable = factory.newInstance(clazz);
//
//                //security check. In case of someone wo'll try to execute somthing
//                //on an other object than on of the model.
//                if (searchable instanceof MetaSearch
//                        || searchable instanceof SearchCriteria
//                        || searchable instanceof DataFile
//                        || searchable instanceof Data) {
//                    searchable.unParseFromAmpFragment(fragment);
//                    datas.add(searchable);
//                }
//            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
//                throw new InvalidAmpAnswerCommand(type);
//            }
        }
    }

    /**
     *
     * @return The answer code
     */
    public String getAnswer() {
        return answer;
    }

    /**
     *
     * @return return the list of extracted datas
     */
    public ArrayList<Searchable> getDatas() {
        return datas;
    }
}
