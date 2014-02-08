package com.meta.controler.P2P;

import java.io.IOException;
import java.util.Random;

import net.tomp2p.futures.BaseFuture;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;



/*
 *	JMeta - Meta's java implementation
 *	Copyright (C) 2013 Thomas LAVOCAT
 *	
 *	This program is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Affero General Public License as
 *	published by the Free Software Foundation, either version 3 of the
 *	License, or (at your option) any later version.
 *	
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Affero General Public License for more details.
 *	
 *	You should have received a copy of the GNU Affero General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 * @author Thomas LAVOCAT
 *
 */
public class P2PControler implements BaseFutureListener<BaseFuture>{
	
	private Peer 	peer 	= 	null;
	private int 	port	=	0;
	
	/**
	 * Instantiate the P2P controler
	 * @throws IOException 
	 */
	public P2PControler(int port) throws IOException {
		peer= new PeerMaker(new Number160(new Random())).setPorts(port).makeAndListen();
		Peer another = new PeerMaker(new Number160(new Random())).setMasterPeer(peer).makeAndListen();
		FutureBootstrap future = another.bootstrap().setPeerAddress(peer.getPeerAddress()).start();
		future.addListener(new BaseFutureListener<BaseFuture>() {

			@Override
			public void exceptionCaught(Throwable t) throws Exception {
				t.printStackTrace();
				
			}

			@Override
			public void operationComplete(BaseFuture future) throws Exception {
				if(future.isSuccess()) { // this flag indicates if the future was successful
					System.out.println("success");
				} else {
					System.out.println("failure");
				}
			}
		});
	}

	/**
	 * register the hash on the DHT
	 * @param hash
	 * @throws IOException 
	 */
	public void register(Number160 hash) throws IOException{
		Data data = new Data(peer.getPeerAddress()+":"+port);
		FutureDHT futureDHT = peer.put(hash).setData(data).start();
	}
	
	/**
	 * Start a search in a new Thread
	 * @param hash
	 * @param listener  
	 */
	public synchronized void lookForPeer(Number160 hash, P2PListener listener){
		FutureDHT futureDHT = peer.get(hash).start();
		futureDHT.addListener(this);
	}

	@Override
	public void exceptionCaught(Throwable t) throws Exception {
		System.out.println(t.getLocalizedMessage());
	}

	@Override
	public void operationComplete(BaseFuture future) throws Exception {
		
	}
	
}
