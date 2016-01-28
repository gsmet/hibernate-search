package org.hibernate.search.backend.elasticsearch.impl;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.index.query.QueryBuilder;

public class QueryBuilders2 {

	public static PlainQueryQueryBuilder plainQuery(String query) {
		return new PlainQueryQueryBuilder( query );
	}

	public static class PlainQueryQueryBuilder extends QueryBuilder {

		final Map<String, Object> source;

		protected PlainQueryQueryBuilder(String query) {
			source = XContentHelper.convertToMap( new BytesArray( query ), true ).v2();
		}

		@Override
		protected void doXContent(XContentBuilder builder, Params params) throws IOException {
			for ( Map.Entry<String, Object> entry : source.entrySet() ) {
				builder.field( entry.getKey() );
				Object value = entry.getValue();
				if ( value == null ) {
					builder.nullValue();
				}
				else {
					builder.value( value );
				}
			}
		}

	}

}
