/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.search.dsl.projection.impl;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.hibernate.search.engine.search.SearchProjection;
import org.hibernate.search.engine.search.dsl.projection.DistanceProjectionContext;
import org.hibernate.search.engine.search.dsl.projection.DocumentReferenceProjectionContext;
import org.hibernate.search.engine.search.dsl.projection.FieldProjectionContext;
import org.hibernate.search.engine.search.dsl.projection.ScoreProjectionContext;
import org.hibernate.search.engine.search.dsl.projection.SearchProjectionContainerContext;
import org.hibernate.search.engine.search.projection.spi.SearchProjectionFactory;
import org.hibernate.search.util.common.function.QuadriFunction;
import org.hibernate.search.util.common.function.TriFunction;
import org.hibernate.search.util.impl.common.CollectionHelper;


public class SearchProjectionContainerContextImpl implements SearchProjectionContainerContext {

	private final SearchProjectionFactory factory;

	public SearchProjectionContainerContextImpl(SearchProjectionFactory factory) {
		this.factory = factory;
	}

	@Override
	public DocumentReferenceProjectionContext documentReference() {
		return new DocumentReferenceProjectionContextImpl( factory );
	}

	@Override
	public <T> FieldProjectionContext<T> field(String absoluteFieldPath, Class<T> clazz) {
		return new FieldProjectionContextImpl<T>( factory, absoluteFieldPath, clazz );
	}

	@Override
	public DistanceProjectionContext distance(String absoluteFieldPath) {
		return new DistanceProjectionContextImpl( factory, absoluteFieldPath );
	}

	@Override
	public ScoreProjectionContext score() {
		return new ScoreProjectionContextImpl( factory );
	}

	@Override
	public <C, T1, T2> SearchProjection<C> composite(SearchProjection<T1> projection1, SearchProjection<T2> projection2,
			BiFunction<T1, T2, C> instantiator) {
		List<SearchProjection<?>> projections = Arrays.asList(projection1, projection2);
		Function<List<?>, C> resultTransformer = new Function<List<?>, C>() {

			@SuppressWarnings("unchecked")
			@Override
			public C apply(List<?> results) {
				return instantiator.apply((T1) results.get(0), (T2) results.get(1));
			}
		};
		
		return factory.composite(projections, resultTransformer);
	}

	@Override
	public <C, T1, T2, T3> SearchProjection<C> composite(SearchProjection<T1> projection1,
			SearchProjection<T2> projection2, SearchProjection<T3> projection3,
			TriFunction<T1, T2, T3, C> instantiator) {
		List<SearchProjection<?>> projections = Arrays.asList(projection1, projection2, projection3);
		Function<List<?>, C> resultTransformer = new Function<List<?>, C>() {

			@SuppressWarnings("unchecked")
			@Override
			public C apply(List<?> results) {
				return instantiator.apply((T1) results.get(0), (T2) results.get(1), (T3) results.get(2));
			}
		};
		
		return factory.composite(projections, resultTransformer);
	}

	@Override
	public <C, T1, T2, T3, T4> SearchProjection<C> composite(SearchProjection<T1> projection1,
			SearchProjection<T2> projection2, SearchProjection<T3> projection3, SearchProjection<T4> projection4,
			QuadriFunction<T1, T2, T3, T4, C> instantiator) {
		List<SearchProjection<?>> projections = Arrays.asList(projection1, projection2, projection3, projection4);
		Function<List<?>, C> resultTransformer = new Function<List<?>, C>() {

			@SuppressWarnings("unchecked")
			@Override
			public C apply(List<?> results) {
				return instantiator.apply((T1) results.get(0), (T2) results.get(1), (T3) results.get(2), (T4) results.get(3));
			}
		};
		
		return factory.composite(projections, resultTransformer);
	}

	@Override
	public SearchProjection<List<?>> composite(SearchProjection<?> projection1, SearchProjection<?> projection2,
			SearchProjection<?>... additionalProjections) {
		List<SearchProjection<?>> projections = CollectionHelper.asList(projection1, projection2, additionalProjections);
		
		return factory.composite(projections, Function.identity());
	}

}
