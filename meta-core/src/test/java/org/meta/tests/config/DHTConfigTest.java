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
package org.meta.tests.config;

import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.meta.api.common.MetaPeer;
import org.meta.configuration.ConfigurationUtils;
import org.meta.tests.MetaBaseTests;

/**
 * Tests for the DHT configuration.
 */
public class DHTConfigTest extends MetaBaseTests {

    /**
     * Test the parsing of DHT bootstrap peers in the configuration file.
     */
    @Test
    public void testPeersParsing() {
        String testString1 = "127.0.0.1:4000";//Valid
        String testString2 = "127.0.0.1:4000,";//Valid
        String testString3 = "127.0.0.1:4000,127.0.0.1:4001";//Valid
        String testString4 = "127.0.0.1:4000, dsfds:4";//1 peer Valid
        String testString5 = "127.0.0.14000"; //Invalid

        Collection<MetaPeer> tmp;
        tmp = ConfigurationUtils.peersFromString(testString1);
        Assert.assertNotNull("Test string1 should not fail.", tmp);
        Assert.assertEquals("Test string1 should contain 1 peer", 1, tmp.size());

        tmp = ConfigurationUtils.peersFromString(testString2);
        Assert.assertNotNull("Test string2 should not fail.", tmp);
        Assert.assertEquals("Test string2 should contain 1 peer", 1, tmp.size());

        tmp = ConfigurationUtils.peersFromString(testString3);
        Assert.assertNotNull("Test string3 should not fail.", tmp);
        Assert.assertEquals("Test string3 should contain 2 peers", 2, tmp.size());

        tmp = ConfigurationUtils.peersFromString(testString4);
        Assert.assertEquals(1, tmp.size());

        tmp = ConfigurationUtils.peersFromString(testString5);
        Assert.assertNotNull("Test string5 result should not be null.", tmp);
        Assert.assertEquals("Test string5 result should not be null.", 0, tmp.size());
    }
}
