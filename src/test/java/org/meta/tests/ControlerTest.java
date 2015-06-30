package org.meta.tests;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.meta.configuration.MetaConfiguration;
import org.meta.controler.Controler;
import org.meta.model.DataString;
import org.meta.model.Model;
import org.meta.model.ModelFactory;
import org.meta.model.Searchable;
import org.meta.model.exceptions.ModelException;
import org.meta.plugin.tcp.SingletonTCPWriter;
import org.meta.plugin.tcp.TCPResponseCallbackInteface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlerTest extends MetaBaseTests {

    private final Logger logger = LoggerFactory.getLogger(Controler.class);
    private DataString data;
    private Controler controler;
    private Model model;

    public ControlerTest(){
        try {
            MetaConfiguration.initConfiguration();
        } catch (IOException ex) {
            logger.error("Failed to initialize configuration, exiting.", ex);
            System.exit(1);
        }
        try {
            controler = new Controler();
        } catch (ModelException | IOException | URISyntaxException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            model = Model.getInstance();
            ModelFactory Factory = model.getFactory();
            data = Factory.createDataString("tutu");
            Model.getInstance().set(data);
        } catch (ModelException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void networkTest(){
        try {
            InetAddress adress;
            try {
                adress = InetAddress.getLocalHost();
                Thread question = SingletonTCPWriter.getInstance().askTo(adress, "PluginExemple", "example", data.getHash(), new TCPResponseCallbackInteface() {

                    @Override
                    public void callback(ArrayList<Searchable> results) {
                        Assert.assertEquals(1, results.size());
                        
                        if(results.size() > 0){
                            DataString dataString = (DataString) results.get(0);
                            Assert.assertEquals(data.getHash(), dataString.getHash());
                            Assert.assertEquals(data.getString(), dataString.getString());
                        }
                        close();
                    }
                    
                },MetaConfiguration.getAmpConfiguration().getAmpPort());
                question.join();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
    
    public void close(){
        model.remove(data.getHash());
        controler.stop();
        
    }
}
