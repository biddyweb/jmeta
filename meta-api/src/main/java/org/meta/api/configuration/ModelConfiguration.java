/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meta.api.configuration;

/**
 *
 * @author nico
 */
public interface ModelConfiguration {

    /**
     *
     * @return the database path.
     */
    String getDatabasePath();

    /**
     *
     * @return The database file max size.
     */
    Integer getMaxSize();

    /**
     *
     * @param databasePath The new database path.
     */
    void setDatabasePath(String databasePath);

    /**
     *
     * @param maxSize The new database file max size.
     */
    void setMaxSize(Integer maxSize);
    
}
