package org.meta.api.ws.forms;

import org.bson.BasicBSONObject;

/**
 *  Is the super class of all Interface elements
 *  @author faquin
 *
 */
public abstract class InterfaceElement {

    private String     id = null;

    /**
     * 
     * @param id ID of the element, shall be unique in your interface
     */
    public InterfaceElement(String id){
        this.id = id;
    }

    /**
     * 
     * @return This as JSON
     */
    public BasicBSONObject toJson() {
       //Create a new Json objet and set all we know (id)
        BasicBSONObject bson = new BasicBSONObject("type", getType());
        bson.append("id", id);
        return bson;

    }
    
    /**
     * 
     * @return the type of th element
     */
    protected abstract String getType();

    /**
     * 
     * @return the (unique ? ;-) ) ID
     */
    public String getId() {
        return id;
    }

    /**
     * May be used to change an element ID
     * @param id another unique ID
     */
    public void setId(String id) {
        this.id = id;
    }

}
