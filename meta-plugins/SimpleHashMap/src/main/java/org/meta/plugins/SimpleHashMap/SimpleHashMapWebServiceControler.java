package org.meta.plugins.SimpleHashMap;

import java.util.LinkedHashMap;
import org.meta.api.ws.AbstractPluginWebServiceControler;
import org.meta.api.ws.AbstractWebService;

import org.meta.plugins.SimpleHashMap.webservicecommands.Put;
import org.meta.plugins.SimpleHashMap.webservicecommands.Get;

public class SimpleHashMapWebServiceControler extends AbstractPluginWebServiceControler{

    @Override
    protected void registercommands(
            LinkedHashMap<String, Class<? extends AbstractWebService>> commands) {
        commands.put("put",     Put.class);
        commands.put("read",    Get.class);
    }

}
