package org.meta;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.meta.common.MetaProperties;
 
import org.meta.controler.Controler;
import org.meta.dht.BootstrapOperation;
import org.meta.dht.DHTConfiguration;
import org.meta.dht.MetaDHT;
import org.meta.dht.MetaPeer;
import org.meta.dht.OperationListener;

/**
 * Just the main class.
 */
public class JMeta {

    public static void main(String[] args) {
        MetaDHT dht = MetaDHT.getInstance();
        DHTConfiguration configuration = new DHTConfiguration(MetaProperties.getDefaultProperties());

        try {
            dht.start(configuration);
        } catch (IOException ex) {
            Logger.getLogger(JMeta.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }

        BootstrapOperation bootstrapOperation = dht.bootstrap();

        bootstrapOperation.addListener(new OperationListener<BootstrapOperation>() {

            @Override
            public void failed(BootstrapOperation operation) {
                Logger.getLogger(JMeta.class.getName()).log(Level.SEVERE, "Bootstrap oeration failed, exiting");
                System.exit(1);
            }

            @Override
            public void complete(BootstrapOperation operation) {
                System.out.println("Bootstrap complete!");
                for (MetaPeer peer : operation.getBootstrapTo()) {
                    System.out.println("Bootstraped to : " + peer);
                }
            }
        });
        //Wait for boostraping to finish.
        bootstrapOperation.awaitUninterruptibly();

        try {
            System.out.println("JMETA: starting controler.");
            Controler controler = new Controler();
        } catch (IOException | URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
