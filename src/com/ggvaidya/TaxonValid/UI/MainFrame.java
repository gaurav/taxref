
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
import com.ggvaidya.TaxonValid.Net.*;
import com.ggvaidya.TaxonValid.Model.*;
import com.ggvaidya.TaxonValid.UI.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
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
	DarwinCSV currentCSV = null;

	public MainFrame() {
		setupFrame();

		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
	}
	
	private void loadFile(File file, int type) {
		try {
			currentCSV = new DarwinCSV(file, type);
			table.removeAll();
			table.setDefaultRenderer(String.class, currentCSV);
			table.setModel(currentCSV);
			table.repaint();
		} catch(IOException ex) {
			MessageBox.messageBox(mainFrame, 
				"Could not read file '" + file + "'", 
				"Unable to read file '" + file + "': " + ex
			);
		}
	}
	
	private JMenuBar setupMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		/* File */
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		
		/* File -> Open */
		JMenuItem miFileOpen = new JMenuItem(new AbstractAction("Open CSV") {
			@Override
			public void actionPerformed(ActionEvent e) {
				FileDialog fd = new FileDialog(mainFrame, "Open Darwin CSV file ...", FileDialog.LOAD);
				fd.setVisible(true);
				File file = new File(fd.getFile());
				if(fd.getDirectory() != null) {
					file = new File(fd.getDirectory(), fd.getFile());
				}
				
				loadFile(file, DarwinCSV.FILE_CSV_DELIMITED);
			}
		});
		fileMenu.add(miFileOpen);
		
		/* File -> Exit */
		JMenuItem miFileExit = new JMenuItem(new AbstractAction("Exit") {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainFrame.setVisible(false);
				mainFrame.dispose();
			}
		});
		fileMenu.add(miFileExit);
		
		/* Match */
		JMenu matchMenu = new JMenu("Match");
		menuBar.add(matchMenu);
		
		/* Match -> Against CSV */
		JMenuItem miMatchCSV = new JMenuItem(new AbstractAction("Match against CSV") {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				
				FileDialog fd = new FileDialog(mainFrame, "Open Darwin CSV file for matching ...", FileDialog.LOAD);
				fd.setVisible(true);
				File file = new File(fd.getFile());
				if(fd.getDirectory() != null) {
					file = new File(fd.getDirectory(), fd.getFile());
				}
				
				try {
					DarwinCSV csv_matcher = new DarwinCSV(file, DarwinCSV.FILE_CSV_DELIMITED);
					
					currentCSV.match(csv_matcher);
					
				} catch (IOException ex) {
					MessageBox.messageBox(mainFrame, "Unable to open file '" + file + "'", "UNable to open file '" + file + "': " + ex);
				}
			}
		});
		matchMenu.add(miMatchCSV);
		
		/* Match -> Against ITIS */
		JMenuItem miMatchITIS = new JMenuItem(new AbstractAction("Match against ITIS") {
			@Override
			public void actionPerformed(ActionEvent e) {
				DarwinCSV csv = DownloadITIS.doIt(mainFrame);
				table.removeAll();
				table.setModel(csv);
				table.setDefaultRenderer(String.class, csv);
				table.repaint();
			}
		});
		matchMenu.add(miMatchITIS);
		
		/* Help */
		JMenu helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);
		
		/* Help -> Memory information */
		JMenuItem miHelpMemory = new JMenuItem(new AbstractAction("Memory information") {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.gc();
				
				MessageBox.messageBox(mainFrame, "Memory information",
					"Maximum memory: " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB\n" +
					"Total memory: " + Runtime.getRuntime().totalMemory() / (1024 * 1024) + " MB\n" +
					"Used memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024) + " MB\n" +
					"Free memory: " + Runtime.getRuntime().freeMemory() / (1024 * 1024) + " MB\n" +
					"Available memory: " + (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()) / (1024 * 1024) + " MB"
				);
			}
		});
		helpMenu.add(miHelpMemory);
		
		return menuBar;
	}

	private void setupFrame() {
		mainFrame.setJMenuBar(setupMenuBar());
		
		TableModel blankDataModel = new AbstractTableModel() {
			public String getColumnName(int x) { return ""; }
			public int getColumnCount() { return 6; }
			public int getRowCount() { return 6;}
			public Object getValueAt(int row, int col) { return ""; }
		};
		table.setModel(blankDataModel);
		
		/*
		mainFrame.setTransferHandler(new TransferHandler() {
			public boolean importData(JComponent comp, Transferable t) {
				if(t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					try {
						java.util.List<java.io.File> list = (java.util.List<java.io.File>) t.getTransferData(DataFlavor.javaFileListFlavor);
						
						loadFile(list.get(0), DarwinCSV.FILE_CSV_DELIMITED);
						
						return true;
						
					} catch (UnsupportedFlavorException ex) {
						return false;
					} catch (IOException ex) {
						return false;
					}
				}
				
				return false;
			}
		});
		*/
		
		mainFrame.add(new JScrollPane(table));
		mainFrame.pack();
	}
}