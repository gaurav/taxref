
/*
 *
 *  MainFrame
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
package com.ggvaidya.TaxonValid.UI;

import com.ggvaidya.TaxonValid.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * MainFrame is the main UI element for TaxonValid: it displays the input file
 * in a format which can be 
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class MainFrame {
	JFrame mainFrame = new JFrame(TaxonValid.getName() + "/" + TaxonValid.getVersion());
	JTable table = new JTable();

	public MainFrame() {
		setupFrame();

		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
	}

	private void setupFrame() {
	TableModel dataModel = new AbstractTableModel() {
		public int getColumnCount() { return 10; }
        public int getRowCount() { return 10;}
        public Object getValueAt(int row, int col) { return new Integer(row*col); }
    };
		table.setModel(dataModel);
		mainFrame.add(table);
	}
}
