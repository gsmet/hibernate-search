/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.mapper.pojo.bridge;

import org.hibernate.search.mapper.pojo.bridge.FunctionBridge;

public final class IntegerAsStringFunctionBridge implements FunctionBridge<Integer, String> {
	@Override
	public String toIndexedValue(Integer propertyValue) {
		return propertyValue == null ? null : propertyValue.toString();
	}
}