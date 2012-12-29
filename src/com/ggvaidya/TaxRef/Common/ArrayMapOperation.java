
/*
 *
 *  MapOperation
 *  Copyright (C) 2012 Gaurav Vaidya
 *
 *  This file is part of TaxonValid.
 *
 *  TaxonValid is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TaxonValid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with TaxonValid.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.ggvaidya.TaxRef.Common;

/**
 * An interface for mapping values from an array of values into a single resulting
 * value. This interface can act as the coderef in a Perl `map' call.
 * 
 * @author vaidyagi
 */
public interface ArrayMapOperation {
	/**
	 * We'd like to map 'values' to 'returned value'.
	 * 
	 * @param values The values to map.
	 * @return The value after mapping.
	 */
	public Object mapTo(Object[] values);
}
