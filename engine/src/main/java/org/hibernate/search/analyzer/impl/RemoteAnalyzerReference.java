/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.analyzer.impl;

import org.hibernate.search.util.logging.impl.Log;
import org.hibernate.search.util.logging.impl.LoggerFactory;

/**
 * A reference to a {@code RemoteAnalyzer}.
 *
 * @author Davide D'Alto
 * @author Guillaume Smet
 */
public final class RemoteAnalyzerReference implements AnalyzerReference {

	private static final Log log = LoggerFactory.make();

	private String name;
	private RemoteAnalyzer analyzer;

	public RemoteAnalyzerReference(String name) {
		this.name = name;
	}

	public RemoteAnalyzerReference(RemoteAnalyzer analyzer) {
		this.analyzer = analyzer;
	}

	public String getName() {
		return name;
	}

	public RemoteAnalyzer getAnalyzer() {
		validate();
		return analyzer;
	}

	public void setAnalyzer(RemoteAnalyzer analyzer) {
		this.analyzer = analyzer;
	}

	@Override
	public void close() {
		if ( analyzer != null ) {
			analyzer.close();
		}
	}

	@Override
	public <T extends AnalyzerReference> boolean is(Class<T> analyzerType) {
		return RemoteAnalyzerReference.class.isAssignableFrom( analyzerType );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends AnalyzerReference> T unwrap(Class<T> analyzerType) {
		return (T) this;
	}

	private void validate() {
		if ( analyzer == null ) {
			throw log.remoteAnalyzerNotInitialized( this );
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( getClass().getSimpleName() );
		sb.append( "<" );
		sb.append( name );
		if ( analyzer != null ) {
			sb.append( ", analyzer: " );
			sb.append( analyzer );
		}
		sb.append( ">" );
		return sb.toString();
	}
}
