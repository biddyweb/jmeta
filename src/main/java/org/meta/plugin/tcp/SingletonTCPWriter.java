package org.meta.plugin.tcp;

import java.net.InetAddress;

import org.meta.common.MetHash;
import org.meta.plugin.tcp.amp.AMPAskFactory;

/**
 * Singleton used to contact peers
 * @author faquin
 *
 */
public class SingletonTCPWriter {
    private static SingletonTCPWriter     instance     = new SingletonTCPWriter();
    private        int                    lastAsk      = 0;

    private SingletonTCPWriter() {}

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
    public Thread askTo(    InetAddress adress,
                        String plugin,
                        String command,
                        MetHash hash,
                        TCPResponseCallbackInteface listenner,
                        int port){
        //each command is "unique" at least where its send
        lastAsk++;
        //Forge a new AMP ask
        AMPAskFactory ask = new AMPAskFactory(lastAsk+"", plugin, command, hash);
        //Let the sender thread do the rest
        AnswerSenderThread sender = new AnswerSenderThread( ask,
                                                            adress,
                                                            port,
                                                            listenner);
        sender.start();
        //return the sender for join() purposes
        return sender;
    }
}
