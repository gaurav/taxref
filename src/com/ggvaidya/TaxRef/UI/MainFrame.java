
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
package com.ggvaidya.TaxRef.UI;

import com.ggvaidya.TaxRef.Model.*;
import com.ggvaidya.TaxRef.*;
import com.ggvaidya.TaxRef.Net.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.table.*;

/**
 * MainFrame is the main UI element for TaxonValid: it displays the input file
 * in a format which can be 
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class MainFrame {
	JFrame mainFrame = new JFrame(TaxRef.getName() + "/" + TaxRef.getVersion());
	JTable table = new JTable();
	JComboBox operations = new JComboBox();
	JTextArea results = new JTextArea("Please choose an operation from the dropdown above.");
	DarwinCSV currentCSV = null;

	public MainFrame() {
		setupFrame();

		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
	}
	
	private void loadFile(File file, int type) {
		operations.removeAllItems();
		
		try {
			currentCSV = new DarwinCSV(file, type);
			table.removeAll();
			table.setDefaultRenderer(Name.class, currentCSV);	
		table.setModel(currentCSV);
			table.repaint();
		} catch(IOException ex) {
			MessageBox.messageBox(mainFrame, 
				"Could not read file '" + file + "'", 
				"Unable to read file '" + file + "': " + ex
			);
		}
		
		operations.addItem("Summarize name identification");
		for(String column: currentCSV.columns()) {
			operations.addItem("Summarize column '" + column + "'");
		}
	}
	
	private JMenuBar setupMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		/* File */
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		
		/* File -> Open CSV */
		JMenuItem miFileOpenCSV = new JMenuItem(new AbstractAction("Open CSV") {
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
		fileMenu.add(miFileOpenCSV);
		
		/* File -> Open tab-delimited */
		JMenuItem miFileOpenTab = new JMenuItem(new AbstractAction("Open tab-delimited") {
			@Override
			public void actionPerformed(ActionEvent e) {
				FileDialog fd = new FileDialog(mainFrame, "Open Darwin tab-delimited file ...", FileDialog.LOAD);
				fd.setVisible(true);
				File file = new File(fd.getFile());
				if(fd.getDirectory() != null) {
					file = new File(fd.getDirectory(), fd.getFile());
				}
				
				loadFile(file, DarwinCSV.FILE_TAB_DELIMITED);
			}
		});
		fileMenu.add(miFileOpenTab);
		
		
		/* File -> Save CSV */
		JMenuItem miFileSave = new JMenuItem(new AbstractAction("Save as CSV") {
			@Override
			public void actionPerformed(ActionEvent e) {
				FileDialog fd = new FileDialog(mainFrame, "Save Darwin CSV file ...", FileDialog.SAVE);
				fd.setVisible(true);
				File file = new File(fd.getFile());
				if(fd.getDirectory() != null) {
					file = new File(fd.getDirectory(), fd.getFile());
				}
				
				try {
					currentCSV.saveToFile(file, DarwinCSV.FILE_CSV_DELIMITED);
				} catch(IOException ex) {
					MessageBox.messageBox(mainFrame, "Could not write file: " + file, "Could not write file " + file + ": " + ex);
				}
			}
		});
		fileMenu.add(miFileSave);
		
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
				DarwinCSV csv = DownloadITIS.getIt(mainFrame);
				currentCSV.match(csv);
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
		
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		
		panel.setLayout(new BorderLayout());
		panel.add(operations, BorderLayout.NORTH);
		panel.add(new JScrollPane(results));
		
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
				
				if(currentCSV != null)
					results.setText(currentCSV.generateTextSummaryOfColumn(colName));
				else
					results.setText("No file loaded.");
				
				results.setCaretPosition(0);
			}
		});
		
		operations.addItem("No file loaded.");
		
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, new JScrollPane(table), panel);
		mainFrame.add(split);
		mainFrame.pack();
	}
}