/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.util.common.function;

@FunctionalInterface
public interface TriFunction<T, U, V, R> {

	/**
	 * Applies this function to the given arguments.
	 *
	 * @param t The first function argument.
	 * @param u The second function argument.
	 * @param v The third function argument.
	 * @return The function result.
	 */
	R apply(T t, U u, V v);

}