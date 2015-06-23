package org.meta.tests;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;
import org.meta.controler.Controler;

public class ControlerTest {

    private Logger log = Logger.getLogger(Controler.class+"");
    @Test
    public void controlerTest() {
        try {
            Controler controler = new Controler();
            controler.stop();
        } catch (IOException | URISyntaxException e) {
            log.log(Level.SEVERE, e.getMessage());
            Assert.fail(e.getMessage());
        }
    }
}
