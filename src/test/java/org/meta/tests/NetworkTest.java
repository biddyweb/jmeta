package org.meta.tests;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
//import org.lavocat.PluginExemple.PluginExempleTcpControler;
import org.meta.model.DataFile;
import org.meta.model.DataString;
import org.meta.model.MetaData;
import org.meta.model.Search;
import org.meta.model.Searchable;
import org.meta.plugin.tcp.SingletonTCPReader;
import org.meta.plugin.tcp.SingletonTCPWriter;
import org.meta.plugin.tcp.TCPResponseCallbackInteface;

public class NetworkTest {

    //@Test
    public void networkTest() {
        try {
            //PluginExempleTcpControler examplePlugin = new PluginExempleTcpControler();

            SingletonTCPReader reader = SingletonTCPReader.getInstance();
            //reader.registerPlugin("test", examplePlugin);
            reader.initializePortAndRun(4001);
            SingletonTCPWriter writer = SingletonTCPWriter.getInstance();
            InetAddress adress;
            try {
                adress = InetAddress.getLocalHost();
                writer.askTo(adress, "toto", "youpi", null, new TCPResponseCallbackInteface() {

                    @Override
                    public void callback(ArrayList<Searchable> results) {
                        for (int i = 0; i < results.size(); i++) {
                            Searchable searchable = results.get(i);

                            if (searchable instanceof Search) {
                                Search search3 = (Search) searchable;
                                System.out.println(search3.getTmpResultsHashes());
                            } else if (searchable instanceof MetaData) {
                                MetaData metaData3 = (MetaData) searchable;
                                System.out.println(metaData3.getTmpLinkedData());
                            } else if (searchable instanceof DataFile) {
                                DataFile dataFile3 = (DataFile) searchable;
                                System.out.println(dataFile3.getFile().getName());
                            } else if (searchable instanceof DataString) {
                                DataString dataString = (DataString) searchable;
                                System.out.println(dataString.getString());
                            }
                        }
                    }
                },4001);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
            reader.kill();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
