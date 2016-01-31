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
import org.meta.api.common.MetHash;

/**
 * Interface describing a search.
 *
 * A Search object is made to point to a list of results. It can be given as a question or as a response.
 *
 * Basically, a search possessed 3 dependences : - the source object which correspond to the source of the
 * interrogation - the metaData which correspond to the criteria of the question/response - the results who
 * are all you now about the pair source/meteData
 *
 * How to represent a search ? --------------------------------------- | MetaSearch |
 * ---------------------------------------- | Source | SearchCriteria |
 * ----------------------------------------
 *
 * |
 * -----> List OF Results
 *
 * The search Hash is composed by the concatenation of the source hash and the metaData hash. Wich allow
 * anyone who as a source and question about it to built a MetaSearch (who will be in this case a question)
 * and contact anyone who have declared knowing answers.
 *
 * A quick example : To find a subtitle to a movie, you can build a search like this :
 * --------------------------------------- | Unique search | ---------------------------------------- |
 * movie.avi | st:fr | ----------------------------------------
 *
 * You do no now if they are resuts, but you can give a try to look into the DHT. If someone as declared
 * knowing answer to your question, so you will be able to contact him, retrieve the answer and add it in the
 * result list of your search.
 *
 * A search received over network is no longe in a full stable state. It only represent is potential
 * linkedObject with tmp attrbutes.
 *
 * If you really need to update your object, you can use the method UpdateFromNetwork in ModelFactory class
 *
 * @author Thomas LAVOCAT
 * @version $Id: $
 */
public abstract class Search extends Searchable {

    /**
     * <p>Constructor for Search.</p>
     *
     * @param hash the hash
     */
    public Search(final MetHash hash) {
        super(hash);
    }

    /**
     * Add a result to this search.
     *
     * @param data the result to add
     */
    public abstract void addResult(final Data data);

    /**
     * Add results to the search.
     *
     * @param results the results to add to this search
     */
    public abstract void addResults(final Collection<Data> results);

    /**
     * <p>getResults</p>
     *
     * @return a collection of every results for this search
     */
    public abstract Collection<Data> getResults();

    /**
     * <p>getSource</p>
     *
     * @return the source
     */
    public abstract Searchable getSource();

    /**
     * Defines the source of this search.
     *
     * @param src the source of this search
     */
    public abstract void setSource(final Searchable src);

    /**
     * <p>getCriteria</p>
     *
     * @return return the search criteria
     */
    public abstract SearchCriteria getCriteria();

    /**
     * Set the search criteria.
     *
     * @param metData the new meta data for this search.
     */
    public abstract void setCriteria(final SearchCriteria metData);

    /**
     * <p>addCriteria</p>
     *
     * @param criteria the criteria list to add to this search
     */
    public abstract void addCriteria(final Collection<MetaData> criteria);

    /**
     * <p>addCriterion</p>
     *
     * @param criterion the criterion to be added to the search criteria
     */
    public abstract void addCriterion(final MetaData criterion);

}
