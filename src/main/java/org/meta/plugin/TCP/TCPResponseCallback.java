package org.meta.plugin.TCP;

import java.util.ArrayList;

import org.meta.modele.Searchable;

public interface TCPResponseCallback {
	
	public void callback(ArrayList<Searchable> results);
	
}
