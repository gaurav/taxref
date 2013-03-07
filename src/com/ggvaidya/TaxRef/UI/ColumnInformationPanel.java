
/*
 *
 *  ColumnInformationPanel
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

package com.ggvaidya.TaxRef.UI;

import com.ggvaidya.TaxRef.Common.ArrayMapOperation;
import com.ggvaidya.TaxRef.Model.*;
import com.ggvaidya.TaxRef.Model.Datatype.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * This panel sits on the right side of TaxRef and displays column level
 * information. It also allows you to modify how columns are interpreted.
 * In general, we try to foist all our column-fiddling code here, while all
 * cell-modification code goes below the table.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class ColumnInformationPanel extends JPanel implements ItemListener {
	private MainFrame mainFrame;
	private JComboBox list_columns;
	private JComboBox list_treat_as;
	private JComboBox list_add_column;
	private JPanel panel_commands;
	private DarwinCSV currentCSV;
	private int currentCol = -1;
	
	public ColumnInformationPanel(MainFrame mf) {
		mainFrame = mf;
		
		initPanel();
	}
	
	/**
	 * Create the components on this panel.
	 */
	private void initPanel() {
		setLayout(new BorderLayout());
		
		list_columns = new JComboBox();
		list_columns.setEditable(false);
		list_columns.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				mainFrame.goToSpecificColumn(list_columns.getSelectedIndex());
			}
		});
		

		/*
		 * 
		// Set up an item listener for when the operations bar changes.
		operations.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() != ItemEvent.SELECTED)
					return;
				
				String actionCmd = (String) e.getItem();
				String colName = null;
				
				if(actionCmd.startsWith("Summarize name identification")) {
					colName = null;
				} else if(actionCmd.startsWith("Summarize column '")) {
					colName = actionCmd.split("'")[1];
				}
				
				// TODO: item state changed.
				
				if(currentCSV != null)
					results.setText("O NO");
				else
					results.setText("No file loaded.");
				
				results.setCaretPosition(0);
			}
		});
		 */
		
		add(list_columns, BorderLayout.NORTH);
		
		panel_commands = new JPanel();
		panel_commands.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		
		list_treat_as = new JComboBox();
		list_treat_as.setEditable(false);
		list_treat_as.addItem("Treat column as text");
		list_treat_as.addItem("Treat column as scientific name");
		list_treat_as.addItem("Treat column as primary key");
		list_treat_as.addItemListener(this);
		
		list_add_column = new JComboBox();
		list_add_column.setEditable(false);
		list_add_column.addItem("Add column ...");
		list_add_column.addItemListener(this);
		
		panel_commands.setLayout(new GridLayout(2, 1));
		panel_commands.add(list_treat_as);
		panel_commands.add(list_add_column);

		JPanel middle = new JPanel();
		middle.add(panel_commands, BorderLayout.NORTH);
		add(middle);
		
		loadedFileChanged(null);
	}
	
	public void loadedFileChanged(DarwinCSV csv) {
		currentCSV = csv;
		
		// Refresh our list of columns.
		if(csv == null) {
			list_columns.removeAllItems();
			list_columns.setEnabled(false);
			
			activateCommandsPanel(false);
			
			list_columns.addItem("Please load a file.");
			
		} else {
			list_columns.removeAllItems();
			list_columns.setEnabled(true);
			
			// Set up the 'operations' variable.
			for(String column: csv.getRowIndex().getColumnNames()) {
				list_columns.addItem("Column: " + column);
			}
			
			activateCommandsPanel(true);
		}
	}
	
	public void columnChanged(int newColumn) {
		currentCol = newColumn;
		
		// Change the list.
		if(currentCol == -1) {
			list_columns.setSelectedIndex(0);
		} else {
			list_columns.setSelectedIndex(currentCol);
		}
		
		// Change contents.
		if(currentCSV == null || currentCol == -1)
			return;
		
		RowIndex rowIndex = currentCSV.getRowIndex();
		
		// list_treat_as
		Class colClass = rowIndex.getColumnClass(currentCol);
		if(colClass.isAssignableFrom(Name.class)) {
			list_treat_as.setSelectedIndex(1);
		} else if(colClass.isAssignableFrom(PrimaryKey.class)) {
			list_treat_as.setSelectedIndex(2);
		} else {
			list_treat_as.setSelectedIndex(0);
		}
		
		// list_add_column
		// TODO: Disable for non-Name columns.
	}

	private void activateCommandsPanel(boolean enabledState) {
		for(Component comp: panel_commands.getComponents()) {
			comp.setEnabled(enabledState);
		}
	}
	
	public void matchChanged(RowIndexMatch match) {
		list_add_column.removeAllItems();
		
		if(match == null) {
			list_add_column.addItem("Add columns based on matched data");
			list_add_column.setEnabled(false);
		} else {
			for(String colName: match.getAgainst().getColumnNames()) {
				list_add_column.addItem("Add column based on '" + colName + "'");
			}
			list_add_column.setEnabled(true);
		}
		mainFrame.getMainFrame().pack();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if(currentCSV == null || currentCol == -1) {
			// Nothing selected, do nothing.
		} else {
			RowIndex rowIndex = currentCSV.getRowIndex();
			
			String colName = rowIndex.getColumnName(currentCol);
			
			if(e.getSource().equals(list_treat_as)) {
				Class treatAsClass = String.class;
				Class originalClass = rowIndex.getColumnClass(currentCol);
				
				switch(list_treat_as.getSelectedIndex()) {
					case 0:
						treatAsClass = String.class;
						break;
					case 1:
						treatAsClass = Name.class;
						break;
					case 2:
						treatAsClass = PrimaryKey.class;
						break;
				}
				
				if(originalClass == treatAsClass)
					return;
				
				System.err.println("Setting class on " + colName + " to " + treatAsClass);
				try {
					rowIndex.changeColumnClass(colName, treatAsClass);
				} catch (NoSuchMethodException ex) {
					Class fromClass = rowIndex.getColumnClass(currentCol);
					System.err.println("Sorry, unable to convert column " + colName + " (class " + fromClass + ") to " + treatAsClass);
					columnChanged(currentCol);
				}
				mainFrame.getJTable().repaint();
			} else if(e.getSource().equals(list_add_column)) {
				int colToInsert = list_add_column.getSelectedIndex();
				if(colToInsert < 0)
					return;
				
				// Name classes only, please!
				if(!Name.class.isAssignableFrom(currentCSV.getRowIndex().getColumnClass(currentCol)))
					return;
				
				RowIndexMatch match = mainFrame.getCurrentMatch();
				if(match == null) return;
				
				RowIndex against = match.getAgainst();
				if(against == null) return;
				
				String newColName = "matched_" + rowIndex.getColumnName(currentCol) + "_to_" + against.getColumnName(colToInsert);
				
				class Joiner implements ArrayMapOperation {
					int fromCol;
					RowIndex against;
					int againstCol;
					
					public Joiner(int fromCol, RowIndex against, int againstCol) {
						this.fromCol = fromCol;
						this.against = against;
						this.againstCol = againstCol;
					}
					
					@Override
					public Object mapTo(Object[] values) {
						Name name = (Name) values[fromCol];
						
						if(against.hasName(name)) {
							java.util.List<Object[]> rows = against.getNameRows(name);
							
							if(rows.size() > 1)
								return "(multiple)";
							else {
								Object[] row = rows.get(0);
								
								System.err.println("Going for againstCol of " + againstCol + " but we only have " + row.length + " lengths.");
								Object val = row[againstCol];
								
								if(val == null)
									return "(null)";
								else
									return val;
							}
						} else {
							return "(not matched)";
						}
					}
				};
				
				rowIndex.setColumnClass(newColName, String.class);
				rowIndex.createNewColumn(newColName, currentCol + 1, new Joiner(currentCol, against, colToInsert));
			}
		}
	}
}
