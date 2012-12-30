
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

import java.util.*;
import javax.swing.event.*;

/**
 * A ColumnMatch matches one column against a RowIndex. This is TaxRef's most
 * common function, so it needs to be fast and furious and all those things.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class ColumnMatch {
	/* The columnName, from and against. */
	private final String columnName;
	private final RowIndex from;
	private final RowIndex against;
	
	public ColumnMatch(RowIndex _from, String colName, RowIndex againstRI) {
		this.columnName = colName;
		this.from = _from;
		this.against = againstRI;
		
		// Set up a table model listener to recalculate match score.
		from.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				if(from.getColumnIndex(columnName) == e.getColumn()) {
					calculateMatchScores();
				}
			}
		});
		
		// Calculate match scores.
		calculateMatchScores();
	}
	
	private void calculateMatchScores() {
		// Do the calculation!
	}
}
