package org.meta.plugin.tcp;

import java.util.ArrayList;

import org.meta.model.Searchable;

public interface TCPResponseCallbackInteface {

	/**
	 * Called when results are available
	 * if you are running an interface, you should change it here
	 * @param results
	 */
    public void callback(ArrayList<Searchable> results);

}
