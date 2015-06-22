/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.meta.model;

/**
 *
 * Enum for model factory, lists objects type from model.
 *
 * Only instanciable classes must be listed here!
 */
public enum ModelType {

    SEARCH(Search.class),
    DATASTRING(DataString.class),
    DATAFILE(DataFile.class),
    METADATA(MetaData.class);

    private Class<?> clazz;

    private ModelType(Class<?> claz) {
        this.clazz = claz;
    }

    public Class<?> getTypeClass() {
        return this.clazz;
    }

    public static ModelType fromClass(Class<?> claz) {
        for (ModelType type : ModelType.values()) {
            if (type.getTypeClass() == claz) {
               return type;
            }
        }
        return null;
    }

}
