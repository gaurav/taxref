
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

import com.ggvaidya.TaxRef.*;
import com.ggvaidya.TaxRef.Model.*;
import com.ggvaidya.TaxRef.Net.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;

/**
 * MainFrame is the main UI element for TaxonValid: it displays the input file
 * in a format which can be 
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class MainFrame implements TableCellRenderer {
	JFrame mainFrame = new JFrame(TaxRef.getName() + "/" + TaxRef.getVersion());
	JTable table = new JTable();
	JComboBox operations = new JComboBox();
	JTextArea results = new JTextArea("Please choose an operation from the dropdown above.");
	DarwinCSV currentCSV = null;
	RowIndexMatch currentMatch = null; 
	MatchInformationPanel matchInfoPanel;

	public MainFrame() {
		setupFrame();

		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
	}
	
	private void setCurrentCSV(DarwinCSV csv) {
		currentCSV = csv;
		table.removeAll();
		table.setDefaultRenderer(Name.class, this);
		table.setModel(currentCSV.getRowIndex());
		table.repaint();
		matchInfoPanel.matchChanged(currentCSV);
	}
	
	private void matchAgainst(DarwinCSV against) {
		currentCSV.getRowIndex().matchAgainst(against.getRowIndex());
		matchInfoPanel.matchChanged(currentCSV);
	}
	
	private void loadFile(File file, int type) {
		operations.removeAllItems();
		
		try {
			setCurrentCSV(new DarwinCSV(file, type));

		} catch(IOException ex) {
			MessageBox.messageBox(mainFrame, 
				"Could not read file '" + file + "'", 
				"Unable to read file '" + file + "': " + ex
			);
		}
		
		operations.addItem("Summarize name identification");
		for(String column: currentCSV.getRowIndex().getColumnNames()) {
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
				if(fd.getFile() == null)
					return;
				
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
					
					matchAgainst(csv_matcher);
					
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
				matchAgainst(csv);
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
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setShowGrid(true);
		table.setSelectionBackground(Color.ORANGE);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// System.err.println("OK: " + currentCSV.getMatcher());
				
				if(currentCSV == null)
					return;
				
				// System.err.println("Looking up!");
				
				// 1. Figure out what the selected cell is.
				Object o = table.getModel().getValueAt(table.getSelectedRow(), table.getSelectedColumn());
				if(o.getClass().equals(Name.class)) {
					matchInfoPanel.matchSelected((Name) o);
				}
			}
		});
		
	JPanel panel = new JPanel();
	panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		
	JPanel internal = new JPanel();
		
	matchInfoPanel = new MatchInformationPanel();
	internal.setLayout(new BorderLayout());
		internal.add(matchInfoPanel, BorderLayout.SOUTH);
		internal.add(new JScrollPane(table));
		
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
					results.setText("O NO");
				else
					results.setText("No file loaded.");
				
				results.setCaretPosition(0);
			}
		});
		
		operations.addItem("No file loaded.");
		
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, internal, panel);
		split.setResizeWeight(1);
		mainFrame.add(split);
		mainFrame.pack();
		
		mainFrame.setDropTarget(new DropTarget(mainFrame, new DropTargetAdapter() {
			@Override
			public void dragEnter(DropTargetDragEvent dtde) {
				dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
			}

			@Override
			public void dragOver(DropTargetDragEvent dtde) {
			}

			@Override
			public void dropActionChanged(DropTargetDragEvent dtde) {
			}

			@Override
			public void dragExit(DropTargetEvent dte) {
			}

			@Override
			public void drop(DropTargetDropEvent dtde) {
				if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
					
					Transferable t = dtde.getTransferable();
					
					try {
						java.util.List<File> files = (java.util.List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
						
						File f = files.get(files.size() - 1);
						loadFile(f, DarwinCSV.FILE_CSV_DELIMITED);
						
					} catch (UnsupportedFlavorException ex) {
						dtde.dropComplete(false);
					} catch (IOException ex) {
						dtde.dropComplete(false);
					}
				}
			}
		}));
	}
	
	
	private DefaultTableCellRenderer defTableCellRenderer = new DefaultTableCellRenderer();
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component c = defTableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        c.setBackground(Color.WHITE);
		
		if(Name.class.isAssignableFrom(value.getClass())) {
			Name name = (Name) value;
			String str = name.toString();
			
			if(currentMatch == null) {
				if(str.length() == 0) {
					c.setBackground(Color.GRAY);
				} else {
					c.setBackground(new Color(137, 207, 230));
				}
			} else {
				if(str.length() == 0) {
					c.setBackground(Color.GRAY);
				} else if(currentMatch.getAgainst().hasName(str)) {
					c.setBackground(new Color(0, 128, 0));
				} else if(currentMatch.getAgainst().hasName(name.getGenus())) {
					c.setBackground(new Color(255, 117, 24));
				} else {
					c.setBackground(new Color(226, 6, 44));
				}
			}
		}
		
		if(hasFocus)
			c.setBackground(c.getBackground().darker());
		
		return c;
	}

}