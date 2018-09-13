/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.search.dsl.projection;

import java.util.List;
import java.util.function.BiFunction;

import org.hibernate.search.engine.search.DocumentReference;
import org.hibernate.search.engine.search.SearchProjection;
import org.hibernate.search.engine.search.dsl.sort.FieldSortContext;
import org.hibernate.search.engine.search.dsl.sort.ScoreSortContext;
import org.hibernate.search.util.common.function.QuadriFunction;
import org.hibernate.search.util.common.function.TriFunction;

/**
 * A context allowing to create a projection.
 *
 * @param <N> The type of the next context (returned by terminal calls such as {@link FieldSortContext#end()}
 * or {@link ScoreSortContext#end()}).
 */
public interface SearchProjectionContainerContext {

	/**
	 * Project the match to a {@link DocumentReference}.
	 */
	DocumentReferenceProjectionContext documentReference();

	/**
	 * Project to a field of the indexed document.
	 *
	 * @param absoluteFieldPath The absolute path of the field.
	 */
	<T> FieldProjectionContext<T> field(String absoluteFieldPath, Class<T> type);

	/**
	 * Project to the distance from the geo point represented by the field to the provided geo point.
	 */
	DistanceProjectionContext distance(String absoluteFieldPath);

	/**
	 * Project to the score of the indexed document.
	 */
	ScoreProjectionContext score();
	
	/**
	 * Creates a type-safe composite projection.
	 * 
	 * @param projection1 The first projection.
	 * @param projection2 The second projection.
	 * @param instantiator The function instantiating the type-safe projected object from the projected values.
	 * @return The composite projection.
	 */
	<C, T1, T2> SearchProjection<C> composite(SearchProjection<T1> projection1, SearchProjection<T2> projection2,
			BiFunction<T1, T2, C> instantiator);

	/**
	 * Creates a type-safe composite projection.
	 * 
	 * @param projection1 The first projection.
	 * @param projection2 The second projection.
	 * @param projection3 The third projection.
	 * @param instantiator The function instantiating the type-safe projected object from the projected values.
	 * @return The composite projection.
	 */
	<C, T1, T2, T3> SearchProjection<C> composite(SearchProjection<T1> projection1, SearchProjection<T2> projection2,
			SearchProjection<T3> projection3, TriFunction<T1, T2, T3, C> instantiator);
	
	/**
	 * Creates a type-safe composite projection.
	 * 
	 * @param projection1 The first projection.
	 * @param projection2 The second projection.
	 * @param projection3 The third projection.
	 * @param projection4 The fourth projection.
	 * @param instantiator The function instantiating the type-safe projected object from the projected values.
	 * @return The composite projection.
	 */
	<C, T1, T2, T3, T4> SearchProjection<C> composite(SearchProjection<T1> projection1, SearchProjection<T2> projection2,
			SearchProjection<T3> projection3, SearchProjection<T4> projection4, QuadriFunction<T1, T2, T3, T4, C> instantiator);
	
	/**
	 * Creates an untyped composite projection.
	 * <p>
	 * The projection will return a list of objects.
	 * 
	 * @param projection1 The first projection.
	 * @param projection2 The second projection.
	 * @param additionalProjections The additional projections.
	 * @return The composite projection.
	 */
	SearchProjection<List<?>> composite(SearchProjection<?> projection1, SearchProjection<?> projection2, SearchProjection<?>... additionalProjections);
}
