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
package org.meta.api.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetamphetUtils;

/**
 *
 * @author Thomas LAVOCAT
 *
 * A MetaData is describe by a list of properties. like {name:subtitles, value:vostfr} and a list of results.
 *
 * This class extends Searchable.
 */
public final class MetaData extends Searchable {

    private TreeSet<MetaProperty> properties = null;

    /**
     * Needed for java Reflection.
     */
    protected MetaData() {
        super();
    }

    /**
     * Create a MetaData -> use in case of creation.
     *
     * @param metHash hash of this MetaData
     * @param props the list of properties
     */
    protected MetaData(final MetHash metHash, final TreeSet<MetaProperty> props) {
        super(metHash);
        this.setProperties(props);
    }

    /**
     * this will only return copies => TODO why ??
     *
     * @return the list of {@link MetaProperty} of this MetaData
     */
    public ArrayList<MetaProperty> getProperties() {
        ArrayList<MetaProperty> property = new ArrayList<>();
        for (Iterator<MetaProperty> i = properties.iterator(); i.hasNext();) {
            MetaProperty next = i.next();
            property.add(new MetaProperty(next));
        }
        return property;
    }

    /**
     * @param props the properties to set Since the MetaProperties are used in the hash calculation This
     * method is only callable in the model package;
     *
     */
    public void setProperties(final TreeSet<MetaProperty> props) {
        this.properties = props;
        this.updateState();
        reHash();
    }

    @Override
    public MetHash reHash() {
        //The hash is the hash of the concatenation of every key:value
        //separate by ;
        //TODO use string builder here
        String concat = "";
        for (Iterator<MetaProperty> i = properties.iterator(); i.hasNext();) {
            MetaProperty property = i.next();
            concat = concat + property.getName() + ":" + property.getValue() + ";";
        }
        hash = MetamphetUtils.makeSHAHash(concat);
        return hash;
    }

    @Override
    public BSONObject getBson() {
        BSONObject bsonObject = super.getBson();
        //foreach proerties, get her value and name and put it in the json
        BasicBSONList bsonProperties = new BasicBSONList();
        int count = 0;
        for (Iterator<MetaProperty> i = properties.iterator(); i.hasNext(); count++) {
            MetaProperty property = i.next();
            BasicBSONObject bsonProperty = new BasicBSONObject();
            bsonProperty.put("name", property.getName());
            bsonProperty.put("value", property.getValue());
            bsonProperties.put(count, bsonProperty);
        }
        bsonObject.put("properties", bsonProperties);
        return bsonObject;
    }

    @Override
    protected void fillFragment(final LinkedHashMap<String, byte[]> fragment) {
        //write every properties
        fragment.put("_nbProperties", (properties.size() + "").getBytes());
        int count = 0;
        for (Iterator<MetaProperty> i = properties.iterator(); i.hasNext(); count++) {
            MetaProperty property = i.next();
            fragment.put("_i" + count + "_property_value", property.getValue().getBytes());
            fragment.put("_i" + count + "_property_name", property.getName().getBytes());
        }
    }

    @Override
    protected void decodefragment(final LinkedHashMap<String, byte[]> fragment) {
        //when this method is called in a metaData, her state is no more a real
        //MetaData but a temporary metaData, it means, it only represent what's
        //over the network, so source = null ans result = null
        //but not properties
        properties = new TreeSet<>();
        //and the Search cannot be write or updated in database

        //extract all linkedDatas and delete it from the fragment too
        int nbProperties = Integer.parseInt(new String(fragment.get("_nbProperties")));
        for (int i = 0; i < nbProperties; i++) {
            String name = new String(fragment.get("_i" + i + "_property_name"));
            String value = new String(fragment.get("_i" + i + "_property_value"));
            MetaProperty property = new MetaProperty(name, value);
            fragment.remove("_i" + i + "_property_name");
            fragment.remove("_i" + i + "_property_value");
            properties.add(property);
        }
    }

    @Override
    public Searchable toOnlyTextData() {
        //Only this
        return this;
    }
}
