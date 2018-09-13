/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.search.dsl.projection.impl;

import org.hibernate.search.engine.search.SearchProjection;
import org.hibernate.search.engine.search.dsl.projection.DistanceProjectionContext;
import org.hibernate.search.engine.search.dsl.projection.EndProjectionContext;
import org.hibernate.search.engine.search.projection.spi.DistanceSearchProjectionBuilder;
import org.hibernate.search.engine.search.projection.spi.SearchProjectionFactory;
import org.hibernate.search.engine.spatial.GeoPoint;


public class DistanceProjectionContextImpl implements DistanceProjectionContext,
		EndProjectionContext<Double> {

	private DistanceSearchProjectionBuilder distanceProjectionBuilder;

	DistanceProjectionContextImpl(SearchProjectionFactory factory, String absoluteFieldPath) {
		this.distanceProjectionBuilder = factory.distance( absoluteFieldPath );
	}

	@Override
	public EndProjectionContext<Double> to(GeoPoint reference) {
		this.distanceProjectionBuilder.to( reference );
		return this;
	}

	@Override
	public SearchProjection<Double> toProjection() {
		return distanceProjectionBuilder.build();
	}

}
