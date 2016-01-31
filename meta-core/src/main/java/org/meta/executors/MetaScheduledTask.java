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

/**
 * Abstract class representing a task that can be scheduled at fixed rate.
 *
 * @author dyslesiq
 * @version $Id: $
 */
public abstract class MetaScheduledTask implements Runnable {

    private final long executionDelay;
    private final long executionPeriod;

    /**
     * <p>Constructor for MetaScheduledTask.</p>
     *
     * @param delay the number of seconds to wait before executing this task
     * @param period the number of seconds after which this task will execute again
     */
    protected MetaScheduledTask(final long delay, final long period) {
        this.executionDelay = delay;
        this.executionPeriod = period;
    }

    /**
     * <p>Getter for the field <code>executionDelay</code>.</p>
     *
     * @return the initial execution delay of this task
     */
    public long getExecutionDelay() {
        return executionDelay;
    }

    /**
     * <p>Getter for the field <code>executionPeriod</code>.</p>
     *
     * @return the period after which this task will execute again
     */
    public long getExecutionPeriod() {
        return executionPeriod;
    }

}
