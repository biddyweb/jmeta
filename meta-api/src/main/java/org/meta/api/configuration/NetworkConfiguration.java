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
     * @return the list of network addresses to bind to
     */
    Collection<InetAddress> getAddresses();

    /**
     *
     * @return the list of network interfaces to bind to
     */
    Collection<String> getInterfaces();

    /**
     *
     * @return the network port to bind to
     */
    Short getPort();

    /**
     *
     * @param addresses the list of network addresses to bind to
     */
    void setAddresses(Collection<InetAddress> addresses);

    /**
     * @param interfaces the list of network interfaces to bind to
     */
    void setInterfaces(Collection<String> interfaces);

    /**
     *
     * @param port the network port to bind to
     */
    void setPort(Short port);

    /**
     * @return true enabling ipV6, false otherwise.
     */
    boolean ipV6();

    /**
     * @param ipV6 the new ipV6 value.
     */
    void setIpV6(boolean ipV6);

    /**
     * @return true enabling ipV6, false otherwise.
     */
    boolean ipV4();

    /**
     * @param ipV4 the new ipV4 value.
     */
    void setIpV4(boolean ipV4);
}
