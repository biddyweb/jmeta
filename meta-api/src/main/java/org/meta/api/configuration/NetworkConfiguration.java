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

    /**
     * @return true enabling ipV6, false otherwise.
     */
    public boolean ipV6();

    /**
     * @param ipV6 the new ipV6 value.
     */
    public void setIpV6(boolean ipV6);

    /**
     * @return true enabling ipV6, false otherwise.
     */
    public boolean ipV4();

    /**
     * @param ipV4 the new ipV4 value.
     */
    public void setIpV4(boolean ipV4);
}
