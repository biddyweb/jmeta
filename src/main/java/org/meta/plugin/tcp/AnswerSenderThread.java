package org.meta.plugin.tcp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import org.meta.model.Data;
import org.meta.model.DataFile;
import org.meta.model.DataString;
import org.meta.model.MetaData;
import org.meta.model.Model;
import org.meta.model.ModelFactory;
import org.meta.model.Search;
import org.meta.model.Searchable;
import org.meta.model.exceptions.ModelException;
import org.meta.plugin.tcp.amp.AMPAnswerParser;
import org.meta.plugin.tcp.amp.AMPAskFactory;
import org.meta.plugin.tcp.amp.exception.NotAValidAMPCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AnswerSenderThread extends Thread {

    private InetAddress             adress      = null;
    private AMPAskFactory           ask         = null;
    private ArrayList<Searchable>   results     = null;
    private int                     port        = 0;
    private TCPResponseCallbackInteface     listenner     = null;
    private ModelFactory           factory  = null;
    private Logger                 log      = LoggerFactory.getLogger(AnswerSenderThread.class);
    /**
     *
     * @param listenner
     * @param answer
     */
    public AnswerSenderThread(    AMPAskFactory ask,
                                InetAddress adress,
                                int port,
                                TCPResponseCallbackInteface listenner)
    {
        this.ask         = ask;
        this.adress     = adress;
        this.port         = port;
        this.listenner     = listenner;
        this.results     = new ArrayList<Searchable>();
        try {
			factory = Model.getInstance().getFactory();
		} catch (ModelException e) {
			e.printStackTrace();
		}
    }

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
                AMPAnswerParser parser = new AMPAnswerParser(buffer.toByteArray());
                this.results = parser.getDatas();
                for(Searchable searchable : this.results){
                    if(searchable instanceof Search){
                        Search search = (Search) searchable;
                        Searchable source = searchElement(this.results, search.getTmpSourceHashes());
                        Searchable result = searchElement(this.results, search.getTmpResultsHashes());
                        ArrayList<Data> linked = new ArrayList<Data>();
                        for(String link : search.getTmpLinkedData()){
                            Searchable s = searchElement(this.results, link);
                            if(s != null)
                                linked.add((Data)s);
                        }
                        factory.updateFromNewtork(search, source, result, linked);
                    }
                }
            }
            //close everything that use memory
            buffer.flush();
            buffer.close();
            client.close();
            is.close();
        } catch (IOException | NotAValidAMPCommand e) {
            log.warn(e.getMessage());
        }

        listenner.callback(results);
    }

    private Searchable searchElement(ArrayList<Searchable> results, String hash) {
    	Searchable element = null;
    	for(int i=0; i<results.size() && element == null; i++){
    		Searchable e = results.get(i);
    		if(e.getHash().toString().equals(hash))
    			element = e;
    	}
		return element;
	}

	public ArrayList<Searchable> getResults() {
        return results;
    }
}
