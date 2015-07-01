package org.meta.plugin.tcp;

import java.util.ArrayList;

import org.meta.model.Searchable;

/**
 * Interface that define callbacks called when a contact peer operation is complete
 * 
 * @author faquin
 */
public interface TCPResponseCallbackInteface {

	/**
	 * Called when results are available
	 * if you are running an interface, you should change it here
	 * @param results A list of results found
	 */
    public void callbackSuccess(ArrayList<Searchable> results);

    /**
     * In case of failure retrieving results this method is called
     * @param failureMessage the reason of the failure
     */
    public void callbackFailure(String failureMessage);

}
