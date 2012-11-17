
/*
 *
 *  ValueComparator
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

package com.ggvaidya.TaxRef.Common;

import java.util.*;

/**
 * Provides a Comparator which can be used to sort a SortedMap by
 * the values.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class ValueComparator<K extends Comparable, C extends Comparable> implements Comparator<K> {
	private Map<K, C> map;
	
	public ValueComparator(Map<K, C> m) {
		this.map = m;
	}
				
	@Override
	public int compare(K o1, K o2) {
		C i1 = map.get(o1);
		C i2 = map.get(o2);
			
		int c = i1.compareTo(i2);
		if(c == 0)
			c = o1.compareTo(o2);
		
		return c;
	}
}
