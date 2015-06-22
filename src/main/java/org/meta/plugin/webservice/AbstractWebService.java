package org.meta.plugin.webservice;

import java.util.Map;

import javax.annotation.PreDestroy;

import org.bson.BasicBSONObject;
import org.meta.plugin.AbstractPluginWebServiceControler;
import org.meta.plugin.tcp.TCPResponseCallbackInteface;
import org.meta.plugin.webservice.forms.InterfaceDescriptor;

//TODO transform as an interface
public abstract class AbstractWebService implements TCPResponseCallbackInteface {

    protected AbstractPluginWebServiceControler controler = null;

    public AbstractWebService(){

    }

    public AbstractWebService(AbstractPluginWebServiceControler controler){
        this.controler = controler;
    }

    public void setWebServiceControler(AbstractPluginWebServiceControler controler){
        this.controler = controler;
    }

    public abstract InterfaceDescriptor getInterface(Map<String, String[]> map);

    public abstract InterfaceDescriptor execute(Map<String, String[]> map);

    public abstract InterfaceDescriptor retrieveUpdate();

    public abstract BasicBSONObject getNextStep();

    @PreDestroy
    public void kill() {
        controler = null;
    }


}
