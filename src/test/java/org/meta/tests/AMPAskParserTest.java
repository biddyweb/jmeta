package org.meta.tests;

import org.junit.Assert;
import org.junit.Test;
import org.meta.common.MetHash;
import org.meta.common.MetamphetUtils;
import org.meta.plugin.tcp.amp.AMPAskBuilder;
import org.meta.plugin.tcp.amp.AMPAskParser;
import org.meta.plugin.tcp.amp.exception.NotAValidAMPCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AMPAskParserTest {
    private Logger logger = LoggerFactory.getLogger(AMPAskParserTest.class);

    @Test
    public void ampPerserTest() {
        byte[] message = {0x00, 0x04, 0x5F, 0x61, 0x73, 0x6B, 0x00, 0x02, 0x32, 0x33,
            0x00, 0x08, 0x5F, 0x63, 0x6F, 0x6D, 0x6D, 0x61, 0x6E, 0x64,
            0x00, 0x03, 0x53, 0x75, 0x6D, 0x00, 0x01, 0x61, 0x00, 0x02,
            0x31, 0x33, 0x00, 0x01, 0x62, 0x00, 0x02, 0x38, 0x31, 0x00,
            0x00};
        AMPAskParser parser;
        try {
            parser = new AMPAskParser(message);
            System.out.println(parser.getAsk());
            System.out.println(parser.getCommand());
            System.out.println(parser.getHash());
        } catch (NotAValidAMPCommand e) {
            logger.error(e.getMessage(), e);
        }

        AMPAskBuilder factory = new AMPAskBuilder(
                                            "23", 
                                            "toto", 
                                            "cacahuete", 
                                            MetamphetUtils.makeSHAHash("toto"));
        try {
            parser = new AMPAskParser(factory.getMessage());
            Assert.assertEquals("23", parser.getAsk());
            Assert.assertEquals("toto", parser.getPlugin());
            Assert.assertEquals("cacahuete", parser.getCommand());
            Assert.assertEquals(MetamphetUtils.makeSHAHash("toto"), parser.getHash());
        } catch (NotAValidAMPCommand e) {
            logger.error(e.getMessage(), e);
            Assert.fail(e.getMessage());
        }
    }

}
