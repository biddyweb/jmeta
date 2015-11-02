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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetamphetUtils;

/**
 * A SearchCriteria is described by a list of criteria.
 *
 * For example: {name:subtitles; value:vostfr}.
 *
 * This class extends Searchable.
 *
 * @author Thomas LAVOCAT
 */
public final class SearchCriteria extends Searchable {

    private static final char CRITERION_SEPARATOR = ';';

    private Set<MetaData> criteria = null;

    /**
     * Creates an empty SearchCriteria.
     *
     * Its hash will be {@link MetHash.ZERO} until a criterion is added.
     */
    public SearchCriteria() {
        this(MetHash.ZERO, null);
    }

    /**
     * Create a criteria from existing meta data.
     *
     * @param crit the initial Set of criteria
     */
    public SearchCriteria(final Set<MetaData> crit) {
        this(MetHash.ZERO, crit);
    }

    /**
     * Create a MetaData -> use in case of creation.
     *
     * @param metHash hash of this MetaData
     * @param crit the initial Set of criteria
     */
    public SearchCriteria(final MetHash metHash, final Set<MetaData> crit) {
        super(metHash);
        this.setCriteria(crit);
    }

    /**
     *
     * @return the Set of {@link MetaData} of this SearchCriteria
     */
    public Set<MetaData> getCriteria() {
        return criteria;
    }

    /**
     * @param props the criteria Set to be used
     */
    public void setCriteria(final Set<MetaData> props) {
        this.criteria = props;
        if (this.criteria == null) {
            this.criteria = new HashSet<>();
        }
        hash();
    }

    /**
     * Adds the given property as a criterion to the criteria list.
     *
     * @param criterion the new criterion
     */
    public void addCriterion(final MetaData criterion) {
        if (this.criteria.add(criterion)) {
            hash();
        }
    }

    /**
     * @param props the criteria Set to be added
     */
    public void addCriteria(final Collection<MetaData> props) {
        if (this.criteria.addAll(props)) {
            hash();
        }
    }

    @Override
    public MetHash hash() {
        //The hash is the hash of the concatenation of every key:value
        //separated by ;
        StringBuilder builder = new StringBuilder();
        for (Iterator<MetaData> i = criteria.iterator(); i.hasNext();) {
            MetaData property = i.next();
            builder.append(property.getKey());
            builder.append(MetaData.SEPARATOR);
            builder.append(property.getValue());
            if (i.hasNext()) {
                builder.append(CRITERION_SEPARATOR);
            }
        }
        hash = MetamphetUtils.makeSHAHash(builder.toString());
        return hash;
    }

}
