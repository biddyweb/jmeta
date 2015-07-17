package org.meta.plugin.tcp;

import org.meta.api.amp.AMPResponseCallback;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import org.meta.api.model.Data;
import org.meta.api.model.MetaData;
import org.meta.api.model.ModelFactory;
import org.meta.api.model.Search;
import org.meta.api.model.Searchable;
import org.meta.plugin.tcp.amp.AMPAnswerParser;
import org.meta.plugin.tcp.amp.AMPAskBuilder;
import org.meta.plugin.tcp.amp.exception.InvalidAMPCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nico
 */
public class AnswerSenderThread implements Runnable {

    private InetAddress             adress      = null;
    private AMPAskBuilder           ask         = null;
    private ArrayList<Searchable>   results     = null;
    private int                     port        = 0;
    private ModelFactory            factory     = null;
    private Logger logger      = LoggerFactory.getLogger(AnswerSenderThread.class);
    private AMPResponseCallback listenner = null;
    
    /**
     * 
     * @param ask       Question
     * @param adress    address to contact
     * @param port      contact port
     * @param listenner who to call back
     * @param factory
     */
    public AnswerSenderThread(  AMPAskBuilder ask,
                                InetAddress adress,
                                int port,
                                AMPResponseCallback listenner,
                                ModelFactory factory)
    {
        this.ask       = ask;
        this.adress    = adress;
        this.port      = port;
        this.listenner = listenner;
        this.results   = new ArrayList<Searchable>();
        this.factory   = factory;
    }

    /**
     *
     */
    public void run() {
        try {
            // Open a connection to the pair
            Socket client = new Socket(adress, port);
            // write the message
            OutputStream os = client.getOutputStream();
            os.write(ask.getMessage());
            client.shutdownOutput();

            // wait for an answer
            InputStream is = client.getInputStream();
            //Open the input stream
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int count = 0;
            while ((count = is.read()) != -1) {
                buffer.write(count);
            }
            if(buffer.size() > 0){
                //parse it into an answer
                AMPAnswerParser parser = new AMPAnswerParser(buffer.toByteArray(), factory);
                this.results = parser.getDatas();
                /*
                 * When results are retrieved.
                 * Search objects are incompletes, 
                 * Iterate, find search, and look if an Element inside the rest
                 * of retrieved datas looks like the search's child data
                 */
                for(Searchable searchable : this.results){
                    if(searchable instanceof Search){
                        Search search = (Search) searchable;
                        Searchable source = searchElement(this.results, search.getTmpSourceHash());
                        MetaData   metaData = (MetaData) searchElement(this.results, search.getTmpmetaDataHash());
                        ArrayList<Data> linked = new ArrayList<Data>();
                        for(String link : search.getTmpLinkedData()){
                            Searchable s = searchElement(this.results, link);
                            if(s != null)
                                linked.add((Data)s);
                        }
                        factory.updateFromNewtork(search, source, metaData, linked);
                    }
                }
            }
            //close everything that use memory
            buffer.flush();
            buffer.close();
            client.close();
            is.close();
        } catch (IOException | InvalidAMPCommand e) {
            logger.warn(e.getMessage());
        }

        listenner.callbackSuccess(results);
    }

    /**
     * Look if an element pointed by his hash is present in a searchable list
     * @param results searchable list
     * @param hash    hash we're lookin for
     * @return the element if found, null otherwise
     */
    private Searchable searchElement(ArrayList<Searchable> results, String hash) {
        Searchable element = null;
        for(int i=0; i<results.size() && element == null; i++){
            Searchable e = results.get(i);
            if(e.getHash().toString().equals(hash))
                element = e;
        }
        return element;
    }
}
