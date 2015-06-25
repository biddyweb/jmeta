package org.meta.plugin.webservice;

import java.util.Map;

import javax.annotation.PreDestroy;
import org.meta.plugin.AbstractPluginWebServiceControler;
import org.meta.plugin.tcp.TCPResponseCallbackInteface;
import org.meta.plugin.webservice.forms.InterfaceDescriptor;
import org.meta.plugin.webservice.forms.organizers.ColumnOrganizer;

public abstract class AbstractWebService implements TCPResponseCallbackInteface {
    protected ColumnOrganizer     rootColumn     = null;
    protected InterfaceDescriptor descriptor     = null;

    protected AbstractPluginWebServiceControler controler = null;
    
    /**
     * build the user interface
     */
    public AbstractWebService(){
        rootColumn = new ColumnOrganizer("root");
        descriptor = new InterfaceDescriptor(rootColumn);
    }
    
    /**
     * Build user interface
     * @param controler
     */
    public AbstractWebService(AbstractPluginWebServiceControler controler){
        rootColumn = new ColumnOrganizer("root");
        descriptor = new InterfaceDescriptor(rootColumn);
        this.controler = controler;
    }

    /**
     * 
     * @param controler give WebServiceControler
     */
    public void setWebServiceControler(AbstractPluginWebServiceControler controler){
        this.controler = controler;
    }

    /**
     * Return the interface
     * @return
     */
    public InterfaceDescriptor getInterface(){
        return descriptor;
    }


	public InterfaceDescriptor execute(Map<String, String[]> map){
         executeCommand(map);
         return descriptor;
    }
    
    /**
     * Execute the command with the given parameters
     * You are not suppose to make big changes to the interface here.
     * Faster are the operation, better it is for the end user
     * 
     * Data will arrive from DHT in the callback method
     * @param map parameter map from jetty
     * 
     */
    protected abstract void executeCommand(Map<String, String[]> map);
    
    public InterfaceDescriptor retrieveUpdate(){
        applySmallUpdate();
        return descriptor;
    }
    /**
     * if you want to make small changes in the interface, it's possible here
     */
    protected abstract void applySmallUpdate();

    @PreDestroy
    public void kill() {
        controler = null;
    }
    
    /**
     * 
     * @param name name of the parameter
     * @param map map from jetty
     * @return the parameter value or null if not found
     */
    public String[] getParameters(String name, Map<String, String[]> map){
        return map.get(name);
    }
    
    /**
     * 
     * @param name name of the parameter array
     * @param map map from jetty
     * @return String[] containing the values or null if not found
     */
    public String getParameter(String name, Map<String, String[]> map){
        String parameter = null;
        String[] parameters = getParameters(name, map);
        if(parameters != null && parameters.length > 0)
            parameter = parameters[0];
        return parameter;
    }
}
