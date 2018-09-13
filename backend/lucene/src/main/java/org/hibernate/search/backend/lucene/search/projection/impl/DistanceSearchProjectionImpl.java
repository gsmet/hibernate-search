/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.lucene.search.projection.impl;

import java.util.Optional;

import org.hibernate.search.backend.lucene.document.model.impl.LuceneIndexModel;
import org.hibernate.search.backend.lucene.document.model.impl.LuceneIndexSchemaFieldNode;
import org.hibernate.search.backend.lucene.search.query.impl.HitExtractor;
import org.hibernate.search.engine.search.query.spi.ProjectionHitCollector;
import org.hibernate.search.engine.spatial.GeoPoint;

public class DistanceSearchProjectionImpl<T> implements LuceneSearchProjection<T> {

	private final String absoluteFieldPath;
	
	private final GeoPoint to;

	DistanceSearchProjectionImpl(String absoluteFieldPath, GeoPoint to) {
		this.absoluteFieldPath = absoluteFieldPath;
		this.to = to;
	}

	@Override
	public Optional<HitExtractor<? super ProjectionHitCollector>> getHitExtractor(LuceneIndexModel indexModel) {
		LuceneIndexSchemaFieldNode<?> schemaNode = indexModel.getFieldNode( absoluteFieldPath );

		if ( schemaNode == null ) {
			return Optional.empty();
		}

		return Optional.of( new DistanceProjectionHitExtractor( absoluteFieldPath, to ) );
	}

}
