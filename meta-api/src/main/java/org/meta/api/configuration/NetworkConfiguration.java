/*
 *
 * JMeta - Meta's java implementation
 *
 * Copyright (C) 2013-2015 Pablo Joubert
 * Copyright (C) 2013-2015 Thomas Lavocat
 * Copyright (C) 2013-2015 Nicolas Michon
 *
 * This file is part of JMeta.
 *
 * JMeta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * JMeta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.meta.api.configuration;

import java.net.InetAddress;
import java.util.Collection;

/**
 * <p>NetworkConfiguration interface.</p>
 *
 * @author nico
 * @version $Id: $
 */
public interface NetworkConfiguration {

    /**
     * <p>getAddresses</p>
     *
     * @return the list of network addresses to bind to
     */
    Collection<InetAddress> getAddresses();

    /**
     * <p>getInterfaces</p>
     *
     * @return the list of network interfaces to bind to
     */
    Collection<String> getInterfaces();

    /**
     * <p>getPort</p>
     *
     * @return the network port to bind to
     */
    Short getPort();

    /**
     * <p>setAddresses</p>
     *
     * @param addresses the list of network addresses to bind to
     */
    void setAddresses(Collection<InetAddress> addresses);

    /**
     * <p>setInterfaces</p>
     *
     * @param interfaces the list of network interfaces to bind to
     */
    void setInterfaces(Collection<String> interfaces);

    /**
     * <p>setPort</p>
     *
     * @param port the network port to bind to
     */
    void setPort(Short port);

    /**
     * <p>ipV6</p>
     *
     * @return true enabling ipV6, false otherwise.
     */
    boolean ipV6();

    /**
     * <p>setIpV6</p>
     *
     * @param ipV6 the new ipV6 value.
     */
    void setIpV6(boolean ipV6);

    /**
     * <p>ipV4</p>
     *
     * @return true enabling ipV6, false otherwise.
     */
    boolean ipV4();

    /**
     * <p>setIpV4</p>
     *
     * @param ipV4 the new ipV4 value.
     */
    void setIpV4(boolean ipV4);
}
