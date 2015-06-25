package org.meta.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;

import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;
import org.meta.common.MetHash;
import org.meta.common.MetamphetUtils;

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
 * This class correspond to a Metadata. A MetaData is describe by a list of
 * properties. like {name:subtitles, value:vostfr} and a list of results.
 *
 * This class extends Searchable.
 */
public class MetaData extends Searchable {

    private List<Data>            linkedData    = null;
    private TreeSet<MetaProperty> properties    = null;
    private List<String>          tmpLinkedData;

    /**
     * needed for java Reflection
     */
    protected MetaData() {
        super();
    }

    /**
     * Create a MetaData -> use in case of creation
     *
     * @param hashCode hash of this MetaData
     * @param linkedData every data linked to this metaData
     */
    protected MetaData(
            MetHash hash,
            List<Data> linkedData,
            TreeSet<MetaProperty> properties
    ) {
        super(hash);
        this.setLinkedData(linkedData);
        this.setProperties(properties);
    }

    /**
     *
     * @return A read-only list of every data linked to this metaData
     */
    public List<Data> getLinkedData() {
        return Collections.unmodifiableList(linkedData);
    }

    /**
     * Set linked data
     *
     * @param linkedData
     */
    protected void setLinkedData(List<Data> linkedData) {
        this.linkedData = linkedData;
        this.updateState();
    }

    /**
     * this will only return copies.
     * @param name
     * @return
     */
    public ArrayList<MetaProperty> getProperties(){
        ArrayList<MetaProperty> property = new ArrayList<MetaProperty>();
        for(Iterator<MetaProperty> i = properties.iterator(); i.hasNext();){
            MetaProperty next = i.next();
            property.add(new MetaProperty(next));
        }
        return property;
    }
    /**
     * @param properties the properties to set
     */
    protected void setProperties(TreeSet<MetaProperty> properties) {
        this.properties = properties;
        this.updateState();
        reHash();
    }
    @Override
    public MetHash reHash() {
        String concat = "";
        for(Iterator<MetaProperty> i = properties.iterator();i.hasNext();){
            MetaProperty property = i.next();
            concat = concat + property.getName()+":"+property.getValue()+";";
        }
        hash = MetamphetUtils.makeSHAHash(concat);
        return null;
    }

    public BSONObject getBson() {
        BSONObject bsonObject = super.getBson();

        BasicBSONList bsonLinkedData = new BasicBSONList();
        for (int i = 0; i < linkedData.size(); ++i) {
            bsonLinkedData.put(i, linkedData.get(i).getHash().toString());
        }
        bsonObject.put("linkedData", bsonLinkedData);
        //foreach proerties, get her value and name and put it in the json
        BasicBSONList bsonProperties = new BasicBSONList();
        int count = 0;
        for (Iterator<MetaProperty> i = properties.iterator(); i.hasNext();count++) {
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
    protected void fillFragment(LinkedHashMap<String, byte[]> fragment) {
        //write every properties
        fragment.put("_nbProperties", (properties.size() + "").getBytes());
        for (Iterator<MetaProperty> i = properties.iterator(); i.hasNext();) {
            MetaProperty property = i.next();
            fragment.put("_i" + i + "_property_name", property.getValue().getBytes());
            fragment.put("_i" + i + "_property_value", property.getName().getBytes());
        }
        //write every data's hash
        fragment.put("_nbLinkedData", (linkedData.size() + "").getBytes());
        for (int i = 0; i < linkedData.size(); i++) {
            Data data = linkedData.get(i);
            fragment.put("_i" + i + "_data", data.getHash().toByteArray());
        }
    }

    @Override
    protected void decodefragment(LinkedHashMap<String, byte[]> fragment) {
        //when this method is called in a metaData, her state is no more a real
        //MetaData but a temporary metaData, it means, it only represent what's
        //over the network, so source = null ans result = null
        linkedData = null;
        //but not properties
        properties = new TreeSet<MetaProperty>();
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

        //extract all linkedDatas and delete it from the fragment too
        int nbLinkedData = Integer.parseInt(new String(fragment.get("_nbLinkedData")));
        tmpLinkedData = new ArrayList<String>();
        for (int i = 0; i < nbLinkedData; i++) {
            String data = new String(fragment.get("_i" + i + "_data"));
            fragment.remove("_i" + i + "_data");
            tmpLinkedData.add(data);
        }
    }

    /**
     *
     * @return a list of the futures results
     */
    public List<String> getTmpLinkedData() {
        return tmpLinkedData;
    }

    @Override
    public Searchable toOnlyTextData() {
        MetaData metaDataClone = new MetaData();
        ArrayList<Data> linkedDataClone = new ArrayList<Data>();
        for (Iterator<Data> i = linkedData.iterator(); i.hasNext();) {
            Data data = i.next();
            linkedDataClone.add((Data) data.toOnlyTextData());
        }
        metaDataClone.setLinkedData(linkedDataClone);
        return metaDataClone;
    }
}
