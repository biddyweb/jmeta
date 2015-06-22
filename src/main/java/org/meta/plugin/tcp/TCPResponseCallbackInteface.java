package org.meta.plugin.tcp;

import java.util.ArrayList;

import org.meta.model.Searchable;

public interface TCPResponseCallbackInteface {

    public void callback(ArrayList<Searchable> results);

}
