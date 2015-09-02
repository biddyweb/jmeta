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
package org.meta.tests;

import org.junit.Assert;
import org.meta.api.common.MetamphetUtils;
import org.meta.plugin.tcp.amp.AMPAskBuilder;
import org.meta.plugin.tcp.amp.AMPAskParser;
import org.meta.plugin.tcp.amp.exception.InvalidAMPCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nico
 */
public class AMPAskParserTest {

    private Logger logger = LoggerFactory.getLogger(AMPAskParserTest.class);

    /**
     *
     */
    //@Test
    public void ampPerserTest() {
        AMPAskBuilder factory = new AMPAskBuilder(
                "23",
                "toto",
                "cacahuete",
                MetamphetUtils.makeSHAHash("toto"));
        try {
            AMPAskParser parser = new AMPAskParser(factory.getMessage());
            Assert.assertEquals("23", parser.getAsk());
            Assert.assertEquals("toto", parser.getPlugin());
            Assert.assertEquals("cacahuete", parser.getCommand());
            Assert.assertEquals(MetamphetUtils.makeSHAHash("toto"), parser.getHash());
        } catch (InvalidAMPCommand e) {
            logger.error(e.getMessage(), e);
            Assert.fail(e.getMessage());
        }
    }

}
