/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meta.model;

import java.util.LinkedList;
import java.util.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nico
 *
 * @param <T> The type of this instance pool.
 */
public class InstancePool<T extends Searchable> {

    private static final int MIN_INSTANCES = 10;
    private static final int MAX_INSTANCES = 100000;
    private static final Logger logger = LoggerFactory.getLogger(InstancePool.class);

    Class<T> clazz;
    private Queue<T> instances;

    public InstancePool(Class<T> claz) {
        clazz = claz;
        instances = new LinkedList<>();
    }

    private void createInstances() {
        for (int i = 0; i < MAX_INSTANCES - instances.size(); ++i) {
            try {
                instances.add(clazz.newInstance());
            } catch (InstantiationException ex) {
                logger.error(null, ex);
            } catch (IllegalAccessException ex) {
                logger.error(null, ex);
            }
        }
    }

    public T getInstance() {
        synchronized (instances) {
            if (instances.size() < MIN_INSTANCES) {
                createInstances();
            }
            return instances.poll();
        }
    }

}
