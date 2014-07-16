package org.meta.tests;

import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.Assert;
import org.junit.Test;
import org.meta.controler.Controler;

public class ControlerTest {

    @Test
    public void controlerTest() {
        try {
            Controler controler = new Controler();
            controler.stop();
        } catch (IOException | URISyntaxException e) {
            Assert.fail(e.getMessage());
        }
    }
}
