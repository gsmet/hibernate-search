/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.impl;

import java.lang.invoke.MethodHandles;

import org.hibernate.search.backend.elasticsearch.document.model.impl.esnative.RootTypeMapping;
import org.hibernate.search.backend.elasticsearch.gson.impl.JsonAccessor;
import org.hibernate.search.backend.elasticsearch.logging.impl.Log;
import org.hibernate.search.engine.backend.spi.Backend;
import org.hibernate.search.util.impl.common.LoggerFactory;

import com.google.gson.JsonObject;

public class NoMultiTenancyStrategyImpl implements MultiTenancyStrategy {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private static final JsonAccessor<String> HIT_ID_ACCESSOR = JsonAccessor.root().property( "_id" ).asString();

	@Override
	public boolean isMultiTenancySupported() {
		return false;
	}

	@Override
	public void contributeToMapping(RootTypeMapping rootTypeMapping) {
	}

	@Override
	public String toElasticsearchId(String tenantId, String id) {
		return id;
	}

	@Override
	public void contributeToIndexedDocument(JsonObject document, String tenantId, String id) {
	}

	@Override
	public JsonObject decorateJsonQuery(JsonObject originalJsonQuery, String tenantId) {
		return originalJsonQuery;
	}

	@Override
	public void contributeToSearchRequest(JsonObject requestBody) {
	}

	@Override
	public String extractTenantScopedDocumentId(JsonObject hit) {
		return HIT_ID_ACCESSOR.get( hit ).get();
	}

	@Override
	public void checkTenantId(Backend<?> backend, String tenantId) {
		if ( tenantId != null ) {
			throw log.tenantIdProvidedButMultiTenancyDisabled( backend, tenantId );
		}
	}
}