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
