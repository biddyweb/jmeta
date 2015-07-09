package org.meta.tests;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meta.common.exceptions.MetaException;
import org.meta.configuration.MetaConfiguration;
import org.meta.controler.MetaController;
import org.meta.model.DataString;
import org.meta.model.Model;
import org.meta.model.ModelFactory;
import org.meta.model.Searchable;
import org.meta.model.exceptions.ModelException;
import org.meta.plugin.tcp.TCPResponseCallbackInteface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlerTest extends MetaBaseTests {

    private final Logger logger = LoggerFactory.getLogger(MetaController.class);

    private static DataString data;

    private static Model model;
    private static MetaController controller;

    public ControlerTest() throws ModelException {
    }

    @BeforeClass
    public static void initControllerTest() {
        try {
            controller = new MetaController();
            controller.initAndStartAll();
            model = controller.getModel();
            ModelFactory Factory = model.getFactory();
            data = Factory.createDataString("tutu");
            model.set(data);
        } catch (MetaException ex) {
            Assert.fail();
        }
    }

    @Test
    public void networkTest() {
        try {
            InetAddress adress;
            try {
                adress = InetAddress.getLocalHost();
                Future<?> question = controller.getAmpWriter().askTo(adress, "PluginExemple", "example", data.getHash(), new TCPResponseCallbackInteface() {

                    @Override
                    public void callbackSuccess(ArrayList<Searchable> results) {
                        Assert.assertEquals(1, results.size());

                        if (results.size() > 0) {
                            DataString dataString = (DataString) results.get(0);
                            Assert.assertEquals(data.getHash(), dataString.getHash());
                            Assert.assertEquals(data.getString(), dataString.getString());
                        }
                        close();
                    }

                    @Override
                    public void callbackFailure(String failureMessage) {
                        // TODO Auto-generated method stub

                    }

                }, MetaConfiguration.getAmpConfiguration().getAmpPort());
                while (!question.isDone()) {
                }
            } catch (UnknownHostException e) {
                logger.error(e.getMessage(), e);
                Assert.fail(e.getMessage());
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail(e.getMessage());
        }
    }

    public void close() {
        this.controller.getModel().remove(data.getHash());
        controller.stop();
    }
}
