/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meta.api.model;

/**
 *
 * Enum for model factory, lists objects type from model.
 *
 * Only instantiable classes must be listed here!
 */
public enum ModelType {

    /**
     *
     */
    SEARCH(Search.class),
    /**
     *
     */
    DATASTRING(DataString.class),
    /**
     *
     */
    DATAFILE(DataFile.class),
    /**
     *
     */
    METADATA(MetaData.class);

    private final Class<?> clazz;

    private ModelType(final Class<?> claz) {
        this.clazz = claz;
    }

    /**
     *
     * @return the {@link Class} for this type.
     */
    public Class<?> getTypeClass() {
        return this.clazz;
    }

    /**
     *
     * @param claz the {@link Class} to get type for.
     * @return the associated type, or null if unknown.
     */
    public static ModelType fromClass(final Class<?> claz) {
        for (ModelType type : ModelType.values()) {
            if (type.getTypeClass() == claz) {
                return type;
            }
        }
        return null;
    }

}
