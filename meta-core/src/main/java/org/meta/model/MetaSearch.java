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
package org.meta.model;

import java.util.Collection;
import java.util.HashMap;
import org.meta.api.common.MetHash;
import org.meta.api.common.MetamphetUtils;
import org.meta.api.model.Data;
import org.meta.api.model.MetaData;
import org.meta.api.model.Search;
import org.meta.api.model.SearchCriteria;
import org.meta.api.model.Searchable;

/**
 * <p>MetaSearch class.</p>
 *
 * @author dyslesiq
 * @version $Id: $
 */
public final class MetaSearch extends Search {

    /**
     * The unique type id of the MetaSearch type.
     */
    public static final short SEARCH_TYPE = 0;

    private Searchable source = null;
    private SearchCriteria criteria = null;
    private HashMap<MetHash, Data> results = null;

    private boolean needRehash = false;

    /**
     * Creates an empty Search.
     *
     * Its hash will be set to {@link MetHash.ZERO}.
     */
    public MetaSearch() {
        super(MetHash.ZERO);
    }

    /**
     * Creates a search from its hash.
     *
     * @param hash the hash
     */
    public MetaSearch(final MetHash hash) {
        super(hash);
    }

    /**
     * Full initialization of a search.
     *
     * Generates the hash from specified entries.
     *
     * @param crit the criteria
     * @param src the source
     * @param res the result list
     */
    public MetaSearch(final SearchCriteria crit, final Searchable src, final Collection<Data> res) {
        super(MetHash.ZERO);
        this.criteria = crit;
        this.source = src;
        this.needRehash = true;
        this.addResults(res);
    }

    /**
     * Full initialization of a search.
     *
     * Generates the hash from specified entries.
     *
     * @param searchHash the hash
     * @param crit the criteria
     * @param src the source
     * @param res the result list
     */
    public MetaSearch(final MetHash searchHash, final SearchCriteria crit, final Searchable src,
            final Collection<Data> res) {
        super(searchHash);
        this.criteria = crit;
        this.source = src;
        this.addResults(res);
    }

    /** {@inheritDoc} */
    @Override
    public MetHash getHash() {
        if (this.needRehash) {
            return hash();
        }
        return this.hash;
    }

    /** {@inheritDoc} */
    @Override
    public MetHash hash() {
        if (!needRehash) {
            return this.hash;
        }
        this.needRehash = false;
        //Hash is composed of concatenation of source hash and criteria hash
        if (this.criteria == null || this.source == null) {
            this.hash = MetHash.ZERO;
            return this.hash;
        }
        hash = MetamphetUtils.makeSHAHash(source.getHash().toString() + criteria.getHash().toString());
        return hash;
    }

    /** {@inheritDoc} */
    @Override
    public Searchable getSource() {
        return source;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Data> getResults() {
        return results.values();
    }

    /** {@inheritDoc} */
    @Override
    public void addResults(final Collection<Data> res) {
        if (this.results == null) {
            this.results = new HashMap<>();
        }
        if (res != null) {
            for (Data d : res) {
                if (d != null) {
                    this.results.put(d.getHash(), d);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addResult(final Data data) {
        if (results == null) {
            results = new HashMap<>();
        }
        this.results.put(data.getHash(), data);
    }

    /** {@inheritDoc} */
    @Override
    public void setSource(final Searchable src) {
        this.source = src;
        this.needRehash = true;
    }

    /** {@inheritDoc} */
    @Override
    public SearchCriteria getCriteria() {
        return criteria;
    }

    /** {@inheritDoc} */
    @Override
    public void setCriteria(final SearchCriteria metData) {
        this.criteria = metData;
        this.needRehash = true;
    }

    /** {@inheritDoc} */
    @Override
    public void addCriteria(final Collection<MetaData> criterionList) {
        this.criteria.addCriteria(criterionList);
        this.needRehash = true;
    }

    /** {@inheritDoc} */
    @Override
    public void addCriterion(final MetaData criterion) {
        this.criteria.addCriterion(criterion);
        this.needRehash = true;
    }

}
