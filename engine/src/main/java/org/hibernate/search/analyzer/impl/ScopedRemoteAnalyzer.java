/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.analyzer.impl;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.search.util.logging.impl.Log;
import org.hibernate.search.util.logging.impl.LoggerFactory;

/**
 * A {@code ScopedRemoteAnalyzer} is a wrapper class containing all remote analyzers for a given class.
 *
 * {@code ScopedRemoteAnalyzer} behaves similar to {@code RemoteAnalyzer} but delegates requests for name
 * to the underlying {@code RemoteAnalyzer} depending on the requested field name.
 *
 * @author Guillaume Smet
 */
public class ScopedRemoteAnalyzer extends RemoteAnalyzer implements ScopedAnalyzer {

	private static final Log log = LoggerFactory.make();

	private RemoteAnalyzer globalAnalyzer;
	private final Map<String, RemoteAnalyzer> analyzers;

	public ScopedRemoteAnalyzer(AnalyzerReference globalAnalyzerReference) {
		this( getRemoteAnalyzer( globalAnalyzerReference ), new HashMap<String, RemoteAnalyzer>() );
	}

	private ScopedRemoteAnalyzer(RemoteAnalyzer globalAnalyzer, Map<String, RemoteAnalyzer> analyzers) {
		super( globalAnalyzer.name );
		this.globalAnalyzer = globalAnalyzer;
		this.analyzers = analyzers;
	}

	@Override
	public void setGlobalAnalyzerReference(AnalyzerReference globalAnalyzerReference) {
		RemoteAnalyzer remoteAnalyzer = getRemoteAnalyzer( globalAnalyzerReference );
		this.name = remoteAnalyzer.name;
		this.globalAnalyzer = remoteAnalyzer;
	}

	@Override
	public void addScopedAnalyzerReference(String scope, AnalyzerReference analyzerReference) {
		this.analyzers.put( scope, getRemoteAnalyzer( analyzerReference ) );
	}

	@Override
	public ScopedAnalyzer clone() {
		return new ScopedRemoteAnalyzer( globalAnalyzer, analyzers );
	}

	private static RemoteAnalyzer getRemoteAnalyzer(AnalyzerReference analyzerReference) {
		if ( !( analyzerReference instanceof RemoteAnalyzerReference ) ) {
			throw log.analyzerReferenceIsNotRemote( analyzerReference );
		}

		return ( (RemoteAnalyzerReference) analyzerReference ).getAnalyzer();
	}

}
