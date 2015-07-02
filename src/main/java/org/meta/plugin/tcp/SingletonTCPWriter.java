package org.meta.plugin.tcp;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.meta.common.MetHash;
import org.meta.configuration.MetaConfiguration;
import org.meta.plugin.tcp.amp.AMPAskBuilder;

/**
 * Singleton used to contact peers
 * @author faquin
 *
 */
public class SingletonTCPWriter {
    private static SingletonTCPWriter     instance     = new SingletonTCPWriter();
    private        int                    lastAsk      = 0;
    private         ExecutorService       executor     = null;


    private SingletonTCPWriter() {
        executor = Executors.newFixedThreadPool(
                MetaConfiguration.getAmpConfiguration().getSenderThPoolSize());
    }

    public static SingletonTCPWriter getInstance() {
        return instance;
    }
    
    /**
     * Ask a question to the given adress, with the given parameters
     * 
     * @param adress    who do we call ?
     * @param port      on wich port do we call ?
     * @param plugin    which plugin is concerned ?
     * @param command   what command to we ask him to execute ?
     * @param hash      on wich hash ?
     * @param listenner after getting result, who is getting the callback ?
     * @return the sender thread for join purposes
     */
    public Future<?> askTo(    InetAddress adress,
                        String plugin,
                        String command,
                        MetHash hash,
                        TCPResponseCallbackInteface listenner,
                        int port){
        //each command is "unique" at least where its send
        lastAsk++;
        //Forge a new AMP ask
        AMPAskBuilder ask = new AMPAskBuilder(lastAsk+"", plugin, command, hash);
        //Let the sender thread do the rest
        AnswerSenderThread sender = new AnswerSenderThread( ask,
                                                            adress,
                                                            port,
                                                            listenner);
        return executor.submit(sender);
    }
}
