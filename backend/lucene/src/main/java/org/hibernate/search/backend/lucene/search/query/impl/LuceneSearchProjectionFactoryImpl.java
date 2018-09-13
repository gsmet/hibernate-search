/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.lucene.search.query.impl;

import java.util.List;
import java.util.function.Function;

import org.hibernate.search.backend.lucene.search.projection.impl.DistanceSearchProjectionBuilderImpl;
import org.hibernate.search.backend.lucene.search.projection.impl.DocumentReferenceSearchProjectionBuilderImpl;
import org.hibernate.search.backend.lucene.search.projection.impl.FieldSearchProjectionBuilderImpl;
import org.hibernate.search.backend.lucene.search.projection.impl.LuceneSearchProjection;
import org.hibernate.search.backend.lucene.search.projection.impl.ScoreSearchProjectionBuilderImpl;
import org.hibernate.search.engine.search.projection.spi.DistanceSearchProjectionBuilder;
import org.hibernate.search.engine.search.projection.spi.DocumentReferenceSearchProjectionBuilder;
import org.hibernate.search.engine.search.projection.spi.FieldSearchProjectionBuilder;
import org.hibernate.search.engine.search.projection.spi.ScoreSearchProjectionBuilder;
import org.hibernate.search.engine.search.projection.spi.SearchProjectionFactory;

class LuceneSearchProjectionFactoryImpl implements SearchProjectionFactory<LuceneSearchProjection<?>> {

	LuceneSearchProjectionFactoryImpl() {
	}

	@Override
	public DocumentReferenceSearchProjectionBuilder documentReference() {
		return DocumentReferenceSearchProjectionBuilderImpl.get();
	}

	@Override
	public <T> FieldSearchProjectionBuilder<T> field(String absoluteFieldPath, Class<T> clazz) {
		return new FieldSearchProjectionBuilderImpl<T>( absoluteFieldPath, clazz );
	}

	@Override
	public DistanceSearchProjectionBuilder distance(String absoluteFieldPath) {
		return new DistanceSearchProjectionBuilderImpl( absoluteFieldPath );
	}

	@Override
	public ScoreSearchProjectionBuilder score() {
		return ScoreSearchProjectionBuilderImpl.get();
	}

	@Override
	public <R> LuceneSearchProjection<R> composite(List<LuceneSearchProjection<?>> projections, Function<List<?>, R> resultTransformer) {
		return null;
	}
}
