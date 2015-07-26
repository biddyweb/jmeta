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
public interface AMPConfiguration {

    /**
     * @return The amp port
     */
    Short getAmpPort();

    /**
     * @return The network configuration.
     */
    NetworkConfiguration getNetworkConfig();

    /**
     * @return the senderThPoolSize
     */
    Integer getSenderThPoolSize();

    /**
     * @return the serverThPoolSize
     */
    Integer getServerThPoolSize();

    /**
     *
     * @param ampPort The new amp port
     */
    void setAmpPort(Short ampPort);

    /**
     *
     * @param networkConfig the underlying network configuration
     */
    void setNetworkConfig(NetworkConfiguration networkConfig);

    /**
     * @param senderThPoolSize the senderThPoolSize to set
     */
    void setSenderThPoolSize(Integer senderThPoolSize);

    /**
     * @param serverThPoolSize the serverThPoolSize to set
     */
    void setServerThPoolSize(Integer serverThPoolSize);

}
