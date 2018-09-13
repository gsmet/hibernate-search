/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.lucene.search.projection.impl;

import org.hibernate.search.engine.search.SearchProjection;
import org.hibernate.search.engine.search.projection.spi.DistanceSearchProjectionBuilder;
import org.hibernate.search.engine.spatial.GeoPoint;


public class DistanceSearchProjectionBuilderImpl implements DistanceSearchProjectionBuilder {

	private final String absoluteFieldPath;

	private GeoPoint to;

	public DistanceSearchProjectionBuilderImpl(String absoluteFieldPath) {
		this.absoluteFieldPath = absoluteFieldPath;
	}
	
	@Override
	public void to(GeoPoint geoPoint) {
		this.to = geoPoint;
	}

	@Override
	public SearchProjection<Double> build() {
		return new DistanceSearchProjectionImpl<>( absoluteFieldPath, to );
	}
}
