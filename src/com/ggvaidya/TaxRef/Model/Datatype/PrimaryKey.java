
/*
 *
 *  PrimaryKey
 *  Copyright (C) 2013 Gaurav Vaidya
 *
 *  This file is part of TaxRef.
 *
 *  TaxRef is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TaxRef is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with TaxRef.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ggvaidya.TaxRef.Model.Datatype;

/**
 * A data type which marks a column as being a primary key.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public final class PrimaryKey {
	String textValue;
	
	public PrimaryKey(String str) {
		textValue = str;
	}
	
	public String getValue() {
		return textValue;
	}
	
	public String toString() {
		return textValue;
	}
	
	/**
	 * Modifies the equals(...) method so that it does a case-sensitive
	 * comparison of primary keys. 
	 * 
	 * @param o The object to compare it with
	 * @return Gets a string representation of o, then compares it case-sensitively
	 * against this primary key.
	 */
	@Override
	public boolean equals(Object o) {
		if(o == null)
			return false;
		
		return textValue.equals(o.toString());
	}
}
