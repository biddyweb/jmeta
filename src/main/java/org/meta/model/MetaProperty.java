package org.meta.model;

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
 * A MetaProperty is a key value object used in two cases :
 * - add semantic to an objet (MetaData)
 * - add information to an object (Data)
 * 
 * Since MetaProperty is not storable as is in the DB, it does not contain a hash
 * value.
 * 
 * it implements Comparable to be ordered by key:value in a TreeSet or other 
 * sorted collections.
 */
public class MetaProperty implements Comparable<MetaProperty>{

    private String name  = "";
    private String value = "";

    /**
     * @param name
     * @param value
     */
    public MetaProperty(String name, String value) {
        super();
        this.name = name;
        this.value = value;
    }
    
    /**
     * copy constructor
     * @param other
     */
    public MetaProperty(MetaProperty other){
        super();
        this.name = other.name;
        this.value = other.value;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

     @Override
     public int compareTo(MetaProperty o) {
         //compare to another property
         // on key:value concatenation
         return (name+":"+value).compareTo((o.name+":"+o.value));
     }

}
