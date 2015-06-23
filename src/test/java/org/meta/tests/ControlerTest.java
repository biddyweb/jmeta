package org.meta.tests;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;
import org.meta.controler.Controler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlerTest {

    private final Logger logger = LoggerFactory.getLogger(Controler.class);

    @Test
    public void controlerTest() {
        try {
            Controler controler = new Controler();
            System.out.println("Stoping the controler...");
            controler.stop();
            System.out.println("Controler stopped.");
        } catch (IOException | URISyntaxException e) {
            logger.error(e.getMessage());
            Assert.fail(e.getMessage());
        }
    }
}
