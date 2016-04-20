/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.util.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.hibernate.search.analyzer.impl.AnalyzerReference;
import org.hibernate.search.analyzer.impl.LuceneAnalyzerReference;

/**
 * A wrapper containing all the analyzers for a given class.
 *
 * @author Davide D'Alto
 */
public final class ScopedAnalyzerReference implements AnalyzerReference {

	private final Map<String, AnalyzerReference> analyzers;
	private final AnalyzerReference globalAnalyzer;

	private ScopedAnalyzerReference(AnalyzerReference globalAnalyzer, Map<String, AnalyzerReference> analyzers) {
		this.globalAnalyzer = globalAnalyzer;
		this.analyzers = Collections.unmodifiableMap( analyzers );
	}

	private static Analyzer luceneAnalyzer(AnalyzerReference scopedAnalyzer) {
		Analyzer analyzer = scopedAnalyzer.unwrap( LuceneAnalyzerReference.class ).getAnalyzer();
		return analyzer;
	}

	/**
	 * Compares the references of the global analyzer backing this ScopedAnalyzer and each scoped analyzer.
	 *
	 * @param other ScopedAnalyzer to compare to
	 * @return true if and only if the same instance of global analyzer is being used and all scoped analyzers also
	 * match, by reference.
	 */
	public boolean isCompositeOfSameInstances(ScopedAnalyzerReference other) {
		if ( this.globalAnalyzer != other.globalAnalyzer ) {
			return false;
		}
		if ( this.analyzers.size() != other.analyzers.size() ) {
			return false;
		}
		for ( String fieldname : analyzers.keySet() ) {
			if ( this.analyzers.get( fieldname ) != other.analyzers.get( fieldname ) ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public <T extends AnalyzerReference> boolean is(Class<T> analyzerType) {
		return ScopedAnalyzerReference.class.isAssignableFrom( analyzerType );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends AnalyzerReference> T unwrap(Class<T> analyzerType) {
		if ( LuceneAnalyzerReference.class.isAssignableFrom( analyzerType ) ) {
			// There are places where we still have to pass a ScopedAnalyzer
			return (T) new LuceneAnalyzerReference( createScopedAnalyzer( globalAnalyzer, analyzers ) );
		}
		return (T) this;
	}

	private static ScopedAnalyzer createScopedAnalyzer(AnalyzerReference globalAnalyzer, Map<String, AnalyzerReference> analyzers) {
		final ScopedAnalyzer analyzer = createScopedAnalyzer( globalAnalyzer );
		for ( Entry<String, AnalyzerReference> entry : analyzers.entrySet() ) {
			if ( entry.getValue().is( LuceneAnalyzerReference.class ) ) {
				analyzer.addScopedAnalyzer( entry.getKey(), luceneAnalyzer( entry.getValue() ) );
			}
		}
		return analyzer;
	}

	private static ScopedAnalyzer createScopedAnalyzer(AnalyzerReference globalAnalyzer) {
		final Analyzer analyzer = globalAnalyzer.is( LuceneAnalyzerReference.class )
				? luceneAnalyzer( globalAnalyzer )
				: null;
		return new ScopedAnalyzer( analyzer );
	}

	@Override
	public void close() {
		if ( globalAnalyzer != null ) {
			globalAnalyzer.close();
		}
		for ( AnalyzerReference entry : analyzers.values() ) {
			entry.close();
		}
	}

	/**
	 * Builds a new {@link ScopedAnalyzerReference}.
	 *
	 * @author Gunnar Morling
	 */
	public static class Builder {

		private final Map<String, AnalyzerReference> analyzers;
		private AnalyzerReference globalAnalyzer;

		public Builder(ScopedAnalyzerReference original) {
			this.analyzers = new HashMap<>( original.analyzers );
			this.globalAnalyzer = original.globalAnalyzer;
		}

		public Builder(AnalyzerReference globalAnalyzer) {
			analyzers = new HashMap<>();
			this.globalAnalyzer = globalAnalyzer;
		}

		public Builder addAnalyzer(String scope, AnalyzerReference analyzer) {
			analyzers.put( scope, analyzer );
			return this;
		}

		public Builder setGlobalAnalyzer(AnalyzerReference globalAnalyzer) {
			this.globalAnalyzer = globalAnalyzer;
			return this;
		}

		public ScopedAnalyzerReference build() {
			return new ScopedAnalyzerReference( globalAnalyzer, analyzers );
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( getClass().getSimpleName() );
		sb.append( "<" );
		sb.append( "globalAnalyzer: " );
		sb.append( globalAnalyzer );
		sb.append( ">" );
		return sb.toString();
	}
}
