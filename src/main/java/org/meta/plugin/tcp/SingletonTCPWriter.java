package org.meta.plugin.tcp;

import java.net.InetAddress;

import org.meta.common.MetHash;
import org.meta.plugin.tcp.amp.AMPAskFactory;


public class SingletonTCPWriter {
    private static SingletonTCPWriter     instance     = new SingletonTCPWriter();
    private        int                    lastAsk      = 0;

    private SingletonTCPWriter() {}

    public static SingletonTCPWriter getInstance() {
        return instance;
    }

    public Thread askTo(    InetAddress adress,
                        String plugin,
                        String command,
                        MetHash hash,
                        TCPResponseCallbackInteface listenner,
                        int port){
        lastAsk++;
        AMPAskFactory ask = new AMPAskFactory(lastAsk+"", plugin, command, hash);
        AnswerSenderThread sender = new AnswerSenderThread(    ask,
                                                            adress,
                                                            port,
                                                            listenner);
        sender.start();
        return sender;
    }
}
