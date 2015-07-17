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
public interface WSConfiguration {

    /**
     * @return The network configuration for the Web part.
     */
    NetworkConfiguration getNetworkConfig();

    /**
     * @param networkConfig the network configuration to use.
     */
    void setNetworkConfig(NetworkConfiguration networkConfig);

}
