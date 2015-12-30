package org.meta.dht.cache;

import java.io.Serializable;

import org.meta.api.common.MetHash;

public class DHTPushElement implements Comparable<DHTPushElement>, Serializable{

    private static final long serialVersionUID = 3225289095763797950L;

    private MetHash search = null;
    private long    timeStampNextPush   = 0;
    private long    timeStampExpiration = 0;


    public DHTPushElement(MetHash search, long timeStampNextPush,
            long timeStampExpiration) {
        super();
        this.search = search;
        this.timeStampNextPush = timeStampNextPush;
        this.timeStampExpiration = timeStampExpiration;
    }


    @Override
    public int compareTo(DHTPushElement other) {
        return other.timeStampNextPush > this.timeStampNextPush ? -1 : 1;
    }


    /**
     * @return the timeStampNextPush
     */
    public long getTimeStampNextPush() {
        return timeStampNextPush;
    }


    /**
     * @param timeStampNextPush the timeStampNextPush to set
     */
    public void setTimeStampNextPush(long timeStampNextPush) {
        this.timeStampNextPush = timeStampNextPush;
    }


    /**
     * @return the timeStampExpiration
     */
    public long getTimeStampExpiration() {
        return timeStampExpiration;
    }


    /**
     * @param timeStampExpiration the timeStampExpiration to set
     */
    public void setTimeStampExpiration(long timeStampExpiration) {
        this.timeStampExpiration = timeStampExpiration;
    }


    /**
     * @return the search
     */
    public MetHash getHash() {
        return search;
    }


    /**
     * @param search the search to set
     */
    public void setSearch(MetHash search) {
        this.search = search;
    }


}
