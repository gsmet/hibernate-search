/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.lucene.search.sort.impl;

import java.lang.invoke.MethodHandles;

import org.hibernate.search.backend.lucene.logging.impl.Log;
import org.hibernate.search.backend.lucene.types.converter.impl.LuceneFieldConverter;
import org.hibernate.search.backend.lucene.types.sort.impl.LuceneFieldSortContributor;
import org.hibernate.search.backend.lucene.types.sort.impl.SortMissingValue;
import org.hibernate.search.engine.logging.spi.EventContexts;
import org.hibernate.search.engine.search.sort.spi.FieldSortBuilder;
import org.hibernate.search.util.impl.common.LoggerFactory;

class FieldSortBuilderImpl extends AbstractSearchSortBuilder
		implements FieldSortBuilder<LuceneSearchSortBuilder> {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private final String absoluteFieldPath;

	private final LuceneFieldConverter<?, ?> fieldConverter;

	private final LuceneFieldSortContributor fieldSortContributor;

	private Object missingValue;

	FieldSortBuilderImpl(String absoluteFieldPath, LuceneFieldConverter<?, ?> fieldConverter,
			LuceneFieldSortContributor fieldSortContributor) {
		this.absoluteFieldPath = absoluteFieldPath;
		this.fieldConverter = fieldConverter;
		this.fieldSortContributor = fieldSortContributor;
	}

	@Override
	public void missingFirst() {
		missingValue = SortMissingValue.MISSING_FIRST;
	}

	@Override
	public void missingLast() {
		missingValue = SortMissingValue.MISSING_LAST;
	}

	@Override
	public void missingAs(Object value) {
		try {
			missingValue = fieldConverter.convertFromDsl( value );
		}
		catch (RuntimeException e) {
			throw log.cannotConvertDslParameter(
					e.getMessage(), e, EventContexts.fromIndexFieldAbsolutePath( absoluteFieldPath )
			);
		}
	}

	@Override
	public void buildAndAddTo(LuceneSearchSortCollector collector) {
		fieldSortContributor.contribute( collector, absoluteFieldPath, order, missingValue );
	}
}
