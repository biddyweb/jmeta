/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meta.api.configuration;

import java.net.InetAddress;
import java.util.Collection;

/**
 *
 * @author nico
 */
public interface NetworkConfiguration {

    /**
     * @return
     */
    Collection<InetAddress> getAddresses();

    /**
     *
     * @return
     */
    Collection<String> getInterfaces();

    /**
     *
     * @return
     */
    Short getPort();

    /**
     *
     * @param addresses
     */
    void setAddresses(Collection<InetAddress> addresses);

    /**
     * @param interfaces
     */
    void setInterfaces(Collection<String> interfaces);

    /**
     *
     * @param port
     */
    void setPort(Short port);
    
}
