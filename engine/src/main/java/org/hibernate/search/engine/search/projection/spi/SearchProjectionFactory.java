/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.search.projection.spi;

import java.util.List;
import java.util.function.Function;

import org.hibernate.search.engine.search.SearchProjection;

/**
 * A factory for search projections.
 * <p>
 * This is the main entry point for the engine
 * to ask the backend to build search projections.
 * 
 * @param <P> The type of the projections.
 */
public interface SearchProjectionFactory<P> {

	DocumentReferenceSearchProjectionBuilder documentReference();

	<T> FieldSearchProjectionBuilder<T> field(String absoluteFieldPath, Class<T> clazz);

	DistanceSearchProjectionBuilder distance(String absoluteFieldPath);

	ScoreSearchProjectionBuilder score();
	
	<R> SearchProjection<R> composite(List<P> projections, Function<List<?>, R> resultTransformer);
}
