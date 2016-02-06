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
package org.meta.api.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetamphetUtils;

/**
 * A SearchCriteria is described by a list of criterion (MetaData).
 *
 * For example: {name:subtitles; value:vostfr}.
 *
 * This class extends Searchable.
 *
 * @author Thomas LAVOCAT
 * @version $Id: $
 */
public final class SearchCriteria extends Searchable {

    private static final char CRITERION_SEPARATOR = ';';

    private MetaDataMap metaMap;

    private boolean needRehash = false;

    /**
     * Creates an empty SearchCriteria.
     *
     * Its hash will be {@link MetHash#ZERO} until a criterion is added.
     */
    public SearchCriteria() {
        super(MetHash.ZERO);
        this.metaMap = new MetaDataMap();
    }

    /**
     * Create a criteria from existing meta data.
     *
     * @param crit the initial Set of criteria
     */
    public SearchCriteria(final Set<MetaData> crit) {
        this(MetHash.ZERO, crit);
        this.needRehash = true;
    }

    /**
     * Create a MetaData with hash and initial Set of MetaData.
     *
     * @param metHash hash of this MetaData
     * @param crit the initial Set of criteria
     */
    public SearchCriteria(final MetHash metHash, final Set<MetaData> crit) {
        super(metHash);
        this.metaMap = new MetaDataMap(crit);
    }

    /**
     * Create a MetaData with initial MetaDataMap.
     *
     * @param metaDataMap the initial Map of criteria
     */
    public SearchCriteria(final MetaDataMap metaDataMap) {
        this(MetHash.ZERO, metaDataMap);
        this.needRehash = true;
    }

    /**
     * Create a MetaData with initial hash and MetaDataMap.
     *
     * @param metHash hash of this MetaData
     * @param metaDataMap the initial Map of criteria
     */
    public SearchCriteria(final MetHash metHash, final MetaDataMap metaDataMap) {
        super(metHash);
        this.metaMap = metaDataMap;
    }

    /**
     * <p>
     * getCriteria</p>
     *
     * @return the Set of {@link MetaData} of this SearchCriteria
     */
    public Set<MetaData> getCriteria() {
        return metaMap.entrySet();
    }

    /**
     * Adds the given MetaData as a criterion to the criteria list.
     *
     * @param criterion the new criterion
     */
    public void addCriterion(final MetaData criterion) {
        this.metaMap.put(criterion);
        this.needRehash = true;
    }

    /**
     * <p>
     * addCriteria</p>
     *
     * @param props the criteria Set to be added
     */
    public void addCriteria(final Collection<MetaData> props) {
        if (this.metaMap.addAll(props)) {
            this.needRehash = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetHash getHash() {
        if (this.needRehash) {
            return this.hash();
        }
        return this.hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetHash hash() {
        if (!this.needRehash) {
            return this.hash;
        }
        this.needRehash = false;
        //The hash is the hash of the concatenation of every key:value
        //separated by ;
        StringBuilder builder = new StringBuilder();
        for (Iterator<MetaData> i = metaMap.entrySet().iterator(); i.hasNext();) {
            MetaData md = i.next();
            builder.append(md.getKey());
            builder.append(MetaData.SEPARATOR);
            builder.append(md.getValue());
            if (i.hasNext()) {
                builder.append(CRITERION_SEPARATOR);
            }
        }
        hash = MetamphetUtils.makeSHAHash(builder.toString());
        return hash;
    }

}
