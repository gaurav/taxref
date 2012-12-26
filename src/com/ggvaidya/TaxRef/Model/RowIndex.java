
/*
 *
 *  RowIndex
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

import java.awt.Component;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
		
/**
 * A RowIndex indexes rows.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
/**
 * A RowIndex indexes rows.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class RowIndex implements TableModel {
	private UUID uuid;
	private int row_count = 0;		// used only for UUID generation.
	
	// TODO: is LinkedLists the best way to do this? Remember that
	// neither rows nor columns are updated much after the first
	// run through.
	private List<Row> rows = new LinkedList<Row>();
	private List<String> columns = new LinkedList<String>();
	private List<String> columnsLowercase = new LinkedList<String>();
	private HashMap<String, Class> columnClasses = new HashMap<String, Class>();
	
	private HashMap<Name, List<Row>> nameIndex = new HashMap<Name, List<Row>>();
	
	public RowIndex() {
		uuid = UUID.randomUUID();
	}
	
	public Row createRow() {
		return new Row(this, uuid + " (row #" + (++row_count) + ")");
	}
	
	public void addRow(Row row) {
		rows.add(row);
	}
	
	public void addName(Row row, Name name) {
		// Index the names.
		for(int x = 0; x < getColumnCount(); x++) {
			if(getColumnClass(x).equals(Name.class)) {
				Name nameToIndex = ((Name) row.get(getColumnName(x)));
				
				if(!nameIndex.containsKey(nameToIndex)) {
					nameIndex.put(nameToIndex, new LinkedList<Row>());
				}
				
				nameIndex.get(nameToIndex).add(row);
			}
		}
	}
	
	public Set<Name> getAllNames() {
		return nameIndex.keySet();
	}
	
	public void addColumn(String colName) {
		addColumn(colName, String.class);
	}
	
	public void addColumn(String colName, Class colClass) {
		if(!columnsLowercase.contains(colName.toLowerCase())) {
			columns.add(colName);
			columnsLowercase.add(colName.toLowerCase());
			columnClasses.put(colName, colClass);
		} else {
			throw new RuntimeException("Can't add the same column twice, sorry! (new column name: '" + colName + "')");
		}
	}
	
	public boolean containsColumn(String colName) {
		return columnsLowercase.contains(colName.toLowerCase());
	}
	
	public List<Object> getColumn(String colName) {
		if(!columnsLowercase.contains(colName)) {
			return null;
		} else {
			ArrayList<Object> column = new ArrayList<Object>();
		
			for(Row r: rows) {
				column.add(r.get(colName));
			}
			
			return column;
		}
	}
	
	public void createNewColumn(String newColumn, String fromColumn, MapOperation mop) {
		for(Row r: rows) {
			r.createNewColumn(newColumn, fromColumn, mop);
		}
	}
	
	/**
	 * Duplicates a column. If this gets too slow, I'll set up a quicker
	 * lookup.
	 * 
	 * @param newColumn
	 * @param fromColumn 
	 */
	public void addColumnAlias(String newColumn, String fromColumn) {
		for(Row r: rows) {
			r.addColumnAlias(newColumn, fromColumn);
		}
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}
	
	public int size() {
		return rows.size();
	}

	@Override
	public int getColumnCount() {
		return columns.size();
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columns.get(columnIndex);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columnClasses.get(getColumnName(columnIndex));
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Row row = rows.get(rowIndex);
		String colName = getColumnName(columnIndex);
		
		return row.get(colName);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		Row row = rows.get(rowIndex);
		String colName = getColumnName(columnIndex);
		
		row.put(colName, aValue);
	}
	
	private List<TableModelListener> listeners = new LinkedList<TableModelListener>();

	@Override
	public void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

	/* Matching code */
	public RowIndexMatch matchAgainst(RowIndex against) {
		return new RowIndexMatch(this, against);
	}

	public List<String> getColumnNames() {
		return new ArrayList(columns);
	}
	
	public boolean hasName(Name n) {
		return nameIndex.containsKey(n);
	}
	
	public boolean hasName(String str) {
		return hasName(new Name(str));
	}

	public List<Row> getRows() {
		return new ArrayList(rows);
	}

}
