package org.meta.tests;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;

import org.junit.Assert;
import org.junit.Test;
import org.meta.controler.Controler;
import org.meta.model.exceptions.ModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlerTest extends MetaBaseTests {

    private final Logger logger = LoggerFactory.getLogger(Controler.class);

    @Test
    public void testControler() {
        try {
            Controler controler = new Controler();
            controler.stop();
        } catch (IOException | URISyntaxException e) {
            logger.error(e.getMessage());
            Assert.fail(e.getMessage());
        } catch (ModelException ex) {
            Assert.fail(ex.getMessage());
            logger.error("Failed to create model", ex);
        }
    }
}
