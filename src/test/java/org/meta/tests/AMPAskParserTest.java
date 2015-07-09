package org.meta.tests;

import org.junit.Assert;
import org.junit.Test;
import org.meta.common.MetHash;
import org.meta.common.MetamphetUtils;
import org.meta.plugin.tcp.amp.AMPAskBuilder;
import org.meta.plugin.tcp.amp.AMPAskParser;
import org.meta.plugin.tcp.amp.exception.InvalidAMPCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AMPAskParserTest {
    private Logger logger = LoggerFactory.getLogger(AMPAskParserTest.class);

    @Test
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
