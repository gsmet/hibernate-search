/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.util.impl.integrationtest.common.stub.mapper;

import java.util.Map;

import org.hibernate.search.engine.backend.index.spi.IndexManager;
import org.hibernate.search.engine.mapper.mapping.spi.MappingImplementor;

public class StubMapping implements MappingImplementor<StubMapping> {

	private final Map<String, IndexManager<?>> indexManagersByTypeIdentifier;

	StubMapping(Map<String, IndexManager<?>> indexManagersByTypeIdentifier) {
		this.indexManagersByTypeIdentifier = indexManagersByTypeIdentifier;
	}

	@Override
	public StubMapping toAPI() {
		return this;
	}

	public IndexManager<?> getIndexManagerByTypeIdentifier(String typeId) {
		return indexManagersByTypeIdentifier.get( typeId );
	}

	@Override
	public void close() {
		// Nothing to do
	}
}
