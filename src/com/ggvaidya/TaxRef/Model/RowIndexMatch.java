
/*
 *
 *  RowIndexMatch
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

/**
 * Carries out and stores the result of a match.
 * These are joins basically.
 *
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class RowIndexMatch {
	private RowIndex from;
	private RowIndex against;
	
	public RowIndexMatch(RowIndex from, RowIndex against) {
		this.from = from;
		this.against = against;
		
		doMatch();
	}
	
	private void doMatch() {
		
	}

	public RowIndex getAgainst() {
		return against;
	}
}