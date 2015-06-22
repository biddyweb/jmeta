package org.meta.plugin.tcp;

import org.meta.plugin.AbstractPluginTCPControler;
import org.meta.plugin.tcp.amp.AMPAnswerFactory;


public abstract class AbstractCommand {

    protected AbstractPluginTCPControler myTCPControler = null;

    public AbstractCommand(){}

    public abstract AMPAnswerFactory execute(String answer, String hash);

    public void setPluginTCPControler(
            AbstractPluginTCPControler abstractPluginTCPControler) {
        this.myTCPControler = abstractPluginTCPControler;
    }
}
