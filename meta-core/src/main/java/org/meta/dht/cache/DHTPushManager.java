package org.meta.dht.cache;

import java.util.NavigableSet;

import org.meta.api.common.AsyncOperation;
import org.meta.api.common.MetHash;
import org.meta.api.common.OperationListener;
import org.meta.api.dht.MetaDHT;
import org.meta.api.model.ModelStorage;
import org.meta.executors.MetaScheduledTask;
import org.meta.storage.CollectionStorage;

public class DHTPushManager extends MetaScheduledTask{

    private final String TREE_ID = "TREEID";
    private final int    S_PUSHI = 3600000;
    private final int    F_PUSHI =  500000;
    private MetaDHT                      dht          = null;
    private ModelStorage                 model        = null;
    private NavigableSet<DHTPushElement> pushElements = null;

    public DHTPushManager(CollectionStorage storageManager, MetaDHT dht, ModelStorage model) {
        super(0, 120);
        this.model = model;
        this.dht = dht;
        this.pushElements = storageManager.getPersistentTreeSet(TREE_ID);
    }

    public void pushElement(MetHash hash, long expirationDate){
        DHTPushElement element = new DHTPushElement(hash, System.currentTimeMillis(), expirationDate);
        pushElements.add(element);
    }

    @Override
    public void run() {
        /*
         * TODO this mecanism is not crash proof.
         */
        long now = System.currentTimeMillis();
        synchronized (pushElements) {
            while(!pushElements.isEmpty() && pushElements.first().getTimeStampNextPush() > now ){
                DHTPushElement element = pushElements.pollFirst();
                if(model.get(element.getHash()) != null && element.getTimeStampExpiration() < now){
                    dht.doStore(element.getHash()).addListener(new OperationListener<AsyncOperation>() {
                        @Override
                        public void failed(AsyncOperation operation) {
                            pushElements.add(new DHTPushElement(element.getHash(),
                                    element.getTimeStampNextPush()+F_PUSHI,
                                    element.getTimeStampExpiration()));
                        }
                        @Override
                        public void complete(AsyncOperation operation) {
                            pushElements.add(new DHTPushElement(element.getHash(),
                                    element.getTimeStampNextPush()+S_PUSHI,
                                    element.getTimeStampExpiration()));
                        }
                    });
                }
            }
        }
    }
}