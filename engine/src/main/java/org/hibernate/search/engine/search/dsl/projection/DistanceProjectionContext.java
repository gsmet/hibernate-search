/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.search.dsl.projection;

import org.hibernate.search.engine.spatial.GeoPoint;
import org.hibernate.search.engine.spatial.ImmutableGeoPoint;

/**
 * The context used when starting to define a distance projection.
 */
public interface DistanceProjectionContext {

	EndProjectionContext<Double> to(GeoPoint reference);

	default EndProjectionContext<Double> to(double latitude, double longitude) {
		return to( new ImmutableGeoPoint( latitude, longitude ) );
	}
}
