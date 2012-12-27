
/*
 *
 *  ColumnMatch
 *  Copyright (C) 2012 Gaurav Vaidya
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

package com.ggvaidya.TaxRef.Model;

import java.util.List;

/**
 * A ColumnMatch matches one column against a RowIndex.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class ColumnMatch {
	private String columnName;
	private List<Object> values;
	private RowIndex against;
	
	public ColumnMatch(String colName, List<Object> v, RowIndex againstRI) {
		this.columnName = colName;
		this.values = v;
		this.against = againstRI;
		
		///////////////////// Continue here.
	}
}
