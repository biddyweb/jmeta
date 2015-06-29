package org.meta.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;
import org.meta.common.MetHash;

/*
 *    JMeta - Meta's java implementation
 *    Copyright (C) 2013 Thomas LAVOCAT
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Affero General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 *
 * @author Thomas LAVOCAT
 *
 * This class correspond to a data on the hard drive. Pointed by a file.
 */
public abstract class Data extends Searchable {
    //TODO add an optional description into the files -> not need to be added
    //in the hash operation
    protected ArrayList<MetaProperty> description = null;

    /**
     * needed for java Reflexion
     */
    public Data() {
        super();
        description = new ArrayList<MetaProperty>();
    }

    /**
     * Instantiate a new Data -> use in case of creation
     *
     * @param hashCode
     * @param file
     */
    public Data(MetHash hash) {
        super(hash);
    }

    public BSONObject getBson() {
        BSONObject bsonObject = super.getBson();
       //foreach proerties, get her value and name and put it in the json
        BasicBSONList bsonProperties = new BasicBSONList();
        int count = 0;
        for (Iterator<MetaProperty> i = description.iterator(); i.hasNext();count++) {
            MetaProperty property = i.next();
            BasicBSONObject bsonProperty = new BasicBSONObject();
            bsonProperty.put("name", property.getName());
            bsonProperty.put("value", property.getValue());
            bsonProperties.put(count, bsonProperty);
        }
        bsonObject.put("description", bsonProperties);
        return bsonObject;
    }

    @Override
    protected void fillFragment(LinkedHashMap<String, byte[]> fragment) {
        //write every description
        fragment.put("_nbProperties", (description.size() + "").getBytes());
        int count = 0;
        for (Iterator<MetaProperty> i = description.iterator(); i.hasNext();count++) {
            MetaProperty property = i.next();
            fragment.put("_i" + count + "_property_value", property.getValue().getBytes());
            fragment.put("_i" + count + "_property_name", property.getName().getBytes());
        }
    }

    @Override
    protected void decodefragment(LinkedHashMap<String, byte[]> fragment) {
        description = new ArrayList<MetaProperty>();
        //and the Search cannot be write or updated in database
        //extract all linkedDatas and delete it from the fragment too
        int nbProperties = Integer.parseInt(new String(fragment.get("_nbProperties")));
        for (int i = 0; i < nbProperties; i++) {
            String name = new String(fragment.get("_i" + i + "_property_name"));
            String value = new String(fragment.get("_i" + i + "_property_value"));
            MetaProperty property = new MetaProperty(name, value);
            fragment.remove("_i" + i + "_property_name");
            fragment.remove("_i" + i + "_property_value");
            description.add(property);
        }
    }

    public void setDescription(ArrayList<MetaProperty> description) {
        this.description = description;
        updateState();
    }

    public ArrayList<MetaProperty> getDescription() {
        return description;
    }
}
