/*
 *
 * JMeta - Meta's java implementation
 *
 * Copyright (C) 2013-2015 Pablo Joubert
 * Copyright (C) 2013-2015 Thomas Lavocat
 * Copyright (C) 2013-2015 Nicolas Michon
 *
 * This file is part of JMeta.
 *
 * JMeta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * JMeta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.meta.executors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Executor task manager to allow recurrent operations to be executed on meta components.
 *
 * @author dyslesiq
 * @version $Id: $
 */
public class MetaTimedExecutor {

    private final List<ScheduledFuture<?>> futures;

    private final ScheduledExecutorService executor;

    /**
     * Default constructor.
     */
    public MetaTimedExecutor() {
        futures = new ArrayList<>();
        executor = Executors.newScheduledThreadPool(1);
    }

    /**
     * <p>addTask</p>
     *
     * @param task the task to schedule
     */
    public void addTask(final MetaScheduledTask task) {
        ScheduledFuture<?> future = executor.scheduleAtFixedRate(task, task.getExecutionDelay(),
                task.getExecutionPeriod(), TimeUnit.SECONDS);
        futures.add(future);
    }

    /**
     * Cancel all currently scheduled tasks and remove them.
     */
    public void cancelTasks() {
        for (ScheduledFuture<?> future : futures) {
            future.cancel(false);
        }
        futures.clear();
    }

}
