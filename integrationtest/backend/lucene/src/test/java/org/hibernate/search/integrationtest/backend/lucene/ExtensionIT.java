/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.backend.lucene;

import static org.hibernate.search.util.impl.integrationtest.common.assertion.DocumentReferencesSearchResultAssert.assertThat;
import static org.hibernate.search.util.impl.integrationtest.common.stub.mapper.StubMapperUtils.referenceProvider;

import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LatLonPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.TermQuery;
import org.hibernate.search.engine.backend.document.DocumentElement;
import org.hibernate.search.engine.backend.document.IndexFieldAccessor;
import org.hibernate.search.engine.backend.document.model.IndexSchemaElement;
import org.hibernate.search.engine.backend.document.model.Sortable;
import org.hibernate.search.engine.backend.index.spi.ChangesetIndexWorker;
import org.hibernate.search.engine.backend.index.spi.IndexManager;
import org.hibernate.search.engine.backend.index.spi.IndexSearchTarget;
import org.hibernate.search.backend.lucene.LuceneExtension;
import org.hibernate.search.engine.backend.spatial.GeoPoint;
import org.hibernate.search.engine.backend.spatial.ImmutableGeoPoint;
import org.hibernate.search.engine.common.spi.SessionContext;
import org.hibernate.search.integrationtest.backend.tck.util.rule.SearchSetupHelper;
import org.hibernate.search.engine.search.DocumentReference;
import org.hibernate.search.engine.search.SearchPredicate;
import org.hibernate.search.engine.search.SearchQuery;
import org.hibernate.search.engine.search.SearchSort;
import org.hibernate.search.util.impl.integrationtest.common.stub.StubSessionContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ExtensionIT {

	private static final String FIRST_ID = "1";
	private static final String SECOND_ID = "2";
	private static final String THIRD_ID = "3";
	private static final String FOURTH_ID = "4";
	private static final String FIFTH_ID = "5";

	@Rule
	public SearchSetupHelper setupHelper = new SearchSetupHelper();

	private IndexAccessors indexAccessors;
	private IndexManager<?> indexManager;
	private String indexName;
	private SessionContext sessionContext = new StubSessionContext();

	@Before
	public void setup() {
		setupHelper.withDefaultConfiguration()
				.withIndex(
						"MappedType", "IndexName",
						ctx -> this.indexAccessors = new IndexAccessors( ctx.getSchemaElement() ),
						(indexManager, indexName) -> {
							this.indexManager = indexManager;
							this.indexName = indexName;
						}
				)
				.setup();

		initData();
	}

	@Test
	public void predicate_fromJsonString() {
		IndexSearchTarget searchTarget = indexManager.createSearchTarget().build();

		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().bool( b -> {
					b.should().withExtensionOptional(
							LuceneExtension.get(),
							// FIXME find some way to forbid using the context twice... ?
							c -> c.fromLuceneQuery( new TermQuery( new Term( "string", "text 1" ) ) )
					);
					b.should().withExtension( LuceneExtension.get() )
							.fromLuceneQuery( IntPoint.newExactQuery( "integer", 2 ) );
					b.should().withExtensionOptional(
							LuceneExtension.get(),
							// FIXME find some way to forbid using the context twice... ?
							c -> c.fromLuceneQuery( LatLonPoint.newDistanceQuery( "geoPoint", 40, -70, 200_000 ) ),
							c -> Assert.fail( "Expected the extension to be present" )
					);
				} )
				.build();
		assertThat( query )
				.hasReferencesHitsAnyOrder( indexName, FIRST_ID, SECOND_ID, THIRD_ID )
				.hasHitCount( 3 );
	}

	@Test
	public void predicate_fromJsonString_separatePredicate() {
		IndexSearchTarget searchTarget = indexManager.createSearchTarget().build();

		SearchPredicate predicate1 = searchTarget.predicate().withExtensionOptional(
				LuceneExtension.get(),
				// FIXME find some way to forbid using the context twice... ?
				c -> c.fromLuceneQuery( new TermQuery( new Term( "string", "text 1" ) ) )
		);
		SearchPredicate predicate2 = searchTarget.predicate().withExtension( LuceneExtension.get() )
				.fromLuceneQuery( IntPoint.newExactQuery( "integer", 2 ) );
		SearchPredicate predicate3 = searchTarget.predicate().withExtensionOptional(
				LuceneExtension.get(),
				// FIXME find some way to forbid using the context twice... ?
				c -> c.fromLuceneQuery( LatLonPoint.newDistanceQuery( "geoPoint", 40, -70, 200_000 ) ),
				c -> Assert.fail( "Expected the extension to be present" )
		);
		SearchPredicate booleanPredicate = searchTarget.predicate().bool( b -> {
			b.should( predicate1 );
			b.should( predicate2 );
			b.should( predicate3 );
		} );

		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate( booleanPredicate )
				.build();
		assertThat( query )
				.hasReferencesHitsAnyOrder( indexName, FIRST_ID, SECOND_ID, THIRD_ID )
				.hasHitCount( 3 );
	}

	@Test
	public void sort_fromJsonString() {
		IndexSearchTarget searchTarget = indexManager.createSearchTarget().build();

		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().all().end()
				.sort( c -> c
						.withExtensionOptional(
								LuceneExtension.get(),
								c2 -> c2.fromLuceneSortField( new SortField( "sort1", Type.STRING ) )
						)
						.then().withExtension( LuceneExtension.get() )
								.fromLuceneSortField( new SortField( "sort2", Type.STRING ) )
						.then().withExtensionOptional(
								LuceneExtension.get(),
								c2 -> c2.fromLuceneSortField( new SortField( "sort3", Type.STRING ) ),
								c2 -> Assert.fail( "Expected the extension to be present" )
						)
				)
				.build();
		assertThat( query ).hasReferencesHitsExactOrder(
				indexName,
				FIRST_ID, SECOND_ID, THIRD_ID, FOURTH_ID, FIFTH_ID
		);

		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().all().end()
				.sort( c -> c
						.withExtensionOptional(
								LuceneExtension.get(),
								c2 -> c2.fromLuceneSort( new Sort(
										new SortField( "sort3", Type.STRING ),
										new SortField( "sort2", Type.STRING ),
										new SortField( "sort1", Type.STRING )
									)
								)
						)
				)
				.build();
		assertThat( query ).hasReferencesHitsExactOrder(
				indexName,
				THIRD_ID, SECOND_ID, FIRST_ID, FOURTH_ID, FIFTH_ID
		);
	}

	@Test
	public void sort_fromJsonString_separateSort() {
		IndexSearchTarget searchTarget = indexManager.createSearchTarget().build();

		SearchSort sort1 = searchTarget.sort()
				.withExtensionOptional(
						LuceneExtension.get(),
						c2 -> c2.fromLuceneSortField( new SortField( "sort1", Type.STRING ) )
				)
				.end();
		SearchSort sort2 = searchTarget.sort().withExtension( LuceneExtension.get() )
				.fromLuceneSortField( new SortField( "sort2", Type.STRING ) )
				.end();
		SearchSort sort3 = searchTarget.sort()
				.withExtensionOptional(
						LuceneExtension.get(),
						c2 -> c2.fromLuceneSortField( new SortField( "sort3", Type.STRING ) ),
						c2 -> Assert.fail( "Expected the extension to be present" )
				)
				.end();

		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().all().end()
				.sort().by( sort1 ).then().by( sort2 ).then().by( sort3 ).end()
				.build();
		assertThat( query )
				.hasReferencesHitsExactOrder( indexName, FIRST_ID, SECOND_ID, THIRD_ID, FOURTH_ID, FIFTH_ID );

		SearchSort sort = searchTarget.sort()
				.withExtensionOptional(
						LuceneExtension.get(),
						c2 -> c2.fromLuceneSort( new Sort(
								new SortField( "sort3", Type.STRING ),
								new SortField( "sort2", Type.STRING ),
								new SortField( "sort1", Type.STRING )
							)
						)
				)
				.end();

		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().all().end()
				.sort().by( sort ).end()
				.build();
		assertThat( query )
				.hasReferencesHitsExactOrder( indexName, THIRD_ID, SECOND_ID, FIRST_ID, FOURTH_ID, FIFTH_ID );
	}

	private void initData() {
		ChangesetIndexWorker<? extends DocumentElement> worker = indexManager.createWorker( sessionContext );
		worker.add( referenceProvider( FIRST_ID ), document -> {
			indexAccessors.string.write( document, "text 1" );

			indexAccessors.sort1.write( document, "a" );
			indexAccessors.sort2.write( document, "z" );
			indexAccessors.sort3.write( document, "z" );
		} );
		worker.add( referenceProvider( SECOND_ID ), document -> {
			indexAccessors.integer.write( document, 2 );

			indexAccessors.sort1.write( document, "z" );
			indexAccessors.sort2.write( document, "a" );
			indexAccessors.sort3.write( document, "z" );
		} );
		worker.add( referenceProvider( THIRD_ID ), document -> {
			indexAccessors.geoPoint.write( document, new ImmutableGeoPoint( 40.12, -71.34 ) );

			indexAccessors.sort1.write( document, "z" );
			indexAccessors.sort2.write( document, "z" );
			indexAccessors.sort3.write( document, "a" );
		} );
		worker.add( referenceProvider( FOURTH_ID ), document -> {
			indexAccessors.sort1.write( document, "z" );
			indexAccessors.sort2.write( document, "z" );
			indexAccessors.sort3.write( document, "z" );
		} );
		worker.add( referenceProvider( FIFTH_ID ), document -> {
			// This document should not match any query
			indexAccessors.string.write( document, "text 2" );
			indexAccessors.integer.write( document, 1 );
			indexAccessors.geoPoint.write( document, new ImmutableGeoPoint( 45.12, -75.34 ) );

			indexAccessors.sort1.write( document, "zz" );
			indexAccessors.sort2.write( document, "zz" );
			indexAccessors.sort3.write( document, "zz" );
		} );

		worker.execute().join();

		// Check that all documents are searchable
		IndexSearchTarget searchTarget = indexManager.createSearchTarget().build();
		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().all().end()
				.build();
		assertThat( query ).hasReferencesHitsAnyOrder(
				indexName,
				FIRST_ID, SECOND_ID, THIRD_ID, FOURTH_ID, FIFTH_ID
		);
	}

	private static class IndexAccessors {
		final IndexFieldAccessor<Integer> integer;
		final IndexFieldAccessor<String> string;
		final IndexFieldAccessor<GeoPoint> geoPoint;

		final IndexFieldAccessor<String> sort1;
		final IndexFieldAccessor<String> sort2;
		final IndexFieldAccessor<String> sort3;

		IndexAccessors(IndexSchemaElement root) {
			integer = root.field( "integer" )
					.asInteger()
					.createAccessor();
			string = root.field( "string" )
					.asString()
					.createAccessor();
			geoPoint = root.field( "geoPoint" )
					.asGeoPoint()
					.createAccessor();

			sort1 = root.field( "sort1" )
					.asString()
					.sortable( Sortable.YES )
					.createAccessor();
			sort2 = root.field( "sort2" )
					.asString()
					.sortable( Sortable.YES )
					.createAccessor();
			sort3 = root.field( "sort3" )
					.asString()
					.sortable( Sortable.YES )
					.createAccessor();
		}
	}

}