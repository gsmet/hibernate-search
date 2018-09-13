/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.lucene.search.query.impl;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.apache.lucene.document.Document;
import org.hibernate.search.engine.search.query.spi.ProjectionHitCollector;

public class CompositeProjectionHitExtractor implements HitExtractor<ProjectionHitCollector> {

	private final CompositeHitExtractor<ProjectionHitCollector> delegate;

	private final Function<List<?>, ?> resultTransformer;

	public CompositeProjectionHitExtractor(CompositeHitExtractor<ProjectionHitCollector> delegate, Function<List<?>, ?> resultTransformer) {
		this.delegate = delegate;
		this.resultTransformer = resultTransformer;
	}

	@Override
	public void extract(ProjectionHitCollector collector, Document document) {
		// FIXME: implement score retrieval from the hit
		collector.collectProjection( 0 );
	}

	@Override
	public void contributeCollectors(LuceneCollectorsBuilder luceneCollectorBuilder) {
		delegate.contributeCollectors( luceneCollectorBuilder );
	}

	@Override
	public void contributeFields(Set<String> absoluteFieldPaths) {
		delegate.contributeFields( absoluteFieldPaths );
	}
}
