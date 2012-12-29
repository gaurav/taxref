
/*
 *
 *  MainFrame
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
package com.ggvaidya.TaxRef.UI;

import com.ggvaidya.TaxRef.*;
import com.ggvaidya.TaxRef.Model.*;
import com.ggvaidya.TaxRef.Net.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

/**
 * MainFrame is the main window for TaxRef. It can open a file containing 
 * biodiversity information and allow the user to edit it. It is (or should
 * be) designed to allow for maximal ease in editing files.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class MainFrame implements TableCellRenderer {
	JFrame mainFrame;
	JTable table = new JTable();
	JComboBox operations = new JComboBox();
	JTextArea results = new JTextArea("Please choose an operation from the dropdown above.");
	JProgressBar progressBar = new JProgressBar(0, 100);
	
	DarwinCSV currentCSV = null;
	RowIndexMatch currentMatch = null; 
	MatchInformationPanel matchInfoPanel;
	
	String basicTitle = TaxRef.getName() + "/" + TaxRef.getVersion();
	
	/**
	 * Create a new, empty, not-visible TaxRef window. Really just activates the setup frame and
	 * setup memory monitor components, then starts things off.
	 */
	public MainFrame() {
		setupFrame();
		setupMemoryMonitor();

		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	/**
	 * Helper method to actually set the frame up.
	 */
	private void setupFrame() {
		// Set up the main frame.
		mainFrame = new JFrame(basicTitle);
		mainFrame.setJMenuBar(setupMenuBar());

		// Set up the JTable.
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setSelectionBackground(COLOR_BACKGROUND);
		table.setShowGrid(true);
		
		// Add a blank table model so that the component renders properly on
		// startup.
		TableModel blankDataModel = new AbstractTableModel() {
			public String getColumnName(int x) { return ""; }
			public int getColumnCount() { return 6; }
			public int getRowCount() { return 6;}
			public Object getValueAt(int row, int col) { return ""; }
		};
		table.setModel(blankDataModel);
		
		// Add a list selection listener so we can tell the matchInfoPanel
		// that a new name was selected.
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(currentCSV == null)
					return;
				
				int row = table.getSelectedRow();
				int column = table.getSelectedColumn();
				
				Object o = table.getModel().getValueAt(row, column);
				if(Name.class.isAssignableFrom(o.getClass())) {
					matchInfoPanel.nameSelected(currentCSV.getRowIndex(), (Name) o, row, column);
				} else {
					matchInfoPanel.nameSelected(currentCSV.getRowIndex(), null, -1, -1);
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
		
		progressBar.setStringPainted(true);
		
		panel.setLayout(new BorderLayout());
		panel.add(operations, BorderLayout.NORTH);
		panel.add(new JScrollPane(results));
		panel.add(progressBar, BorderLayout.SOUTH);
		
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
		
		operations.addItem("No files loaded as yet.");	
		
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
				final File file;
				if(fd.getDirectory() != null) {
					file = new File(fd.getDirectory(), fd.getFile());
				} else if(fd.getFile() != null) {
					file = new File(fd.getFile());
				} else {
					return;
				}
				
				progressBar.setIndeterminate(true);
				new SwingWorker() {
					@Override
					protected Object doInBackground() throws Exception {
						loadFile(file, DarwinCSV.FILE_CSV_DELIMITED);
						
						return null;
					}
					
					@Override
					protected void done() {
						progressBar.setIndeterminate(false);
					}
				}.execute();
			}
		});
		fileMenu.add(miFileOpenCSV);
		
		/* File -> Open CSV without UI */
		JMenuItem miFileOpenCSVnoUI = new JMenuItem(new AbstractAction("Open CSV without UI") {
			@Override
			public void actionPerformed(ActionEvent e) {
				FileDialog fd = new FileDialog(mainFrame, "Open Darwin CSV file ...", FileDialog.LOAD);
				fd.setVisible(true);
				final File file;
				if(fd.getDirectory() != null) {
					file = new File(fd.getDirectory(), fd.getFile());
				} else if(fd.getFile() != null) {
					file = new File(fd.getFile());
				} else {
					return;
				}
				
				loadFile(file, DarwinCSV.FILE_CSV_DELIMITED);
			}
		});
		fileMenu.add(miFileOpenCSVnoUI);
		
		/* File -> Open tab-delimited */
		JMenuItem miFileOpenTab = new JMenuItem(new AbstractAction("Open tab-delimited") {
			@Override
			public void actionPerformed(ActionEvent e) {
				FileDialog fd = new FileDialog(mainFrame, "Open Darwin tab-delimited file ...", FileDialog.LOAD);
				fd.setVisible(true);
				if(fd.getFile() == null)
					return;
				
				final File file;
				if(fd.getDirectory() != null) {
					file = new File(fd.getDirectory(), fd.getFile());
				} else {
					file = new File(fd.getFile());
				}
				
				progressBar.setIndeterminate(true);
				new SwingWorker() {
					@Override
					protected Object doInBackground() throws Exception {
						loadFile(file, DarwinCSV.FILE_TAB_DELIMITED);
						
						return null;
					}
					
					@Override
					protected void done() {
						progressBar.setIndeterminate(false);
					}
				}.execute();
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
				if(fd == null)
					return;
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

	

	
	private java.util.Timer memoryTimer;
	private void setupMemoryMonitor() {
		memoryTimer = new java.util.Timer("Memory monitor", true);
		
		memoryTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						long value = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
						long max = Runtime.getRuntime().maxMemory() / (1024 * 1024);
						int percentage = (int)(((double)value)/max*100);
						
						progressBar.setMinimum(0);
						progressBar.setMaximum(100);
						progressBar.setValue(percentage);
						
						progressBar.setString(value + " MB out of " + max + " MB (" + percentage + "%)");
					}
					
				});
			}
		
		}, new Date(), 5000);	// Every five seconds.
	}
	
	private void setCurrentCSV(DarwinCSV csv) {
		currentCSV = csv;
		operations.removeAllItems();
		table.removeAll();
		table.setDefaultRenderer(Name.class, this);
		table.setModel(currentCSV.getRowIndex());
		table.repaint();
		matchAgainst(null);
	}
	
	private void matchAgainst(DarwinCSV against) {
		System.err.println("matchAgainst: " + against);
		
		if(against == null) {
			currentMatch = null;
			table.repaint();
			return;
		}
		
		long t1 = System.currentTimeMillis();
		currentMatch = currentCSV.getRowIndex().matchAgainst(against.getRowIndex());
		table.repaint();
		matchInfoPanel.matchChanged(currentMatch);
		long t2 = System.currentTimeMillis();
		System.err.println("Finished: " + (t2 - t1) + " ms");
	}
	
	private void loadFile(File file, int type) {
		if(file == null) {
			mainFrame.setTitle(basicTitle);
			return;
		}
		
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
		
		mainFrame.setTitle(basicTitle + ": " + file.getName() + " (" + String.format("%,d", currentCSV.getRowIndex().getRowCount()) + " rows)");
	}
	
	private DefaultTableCellRenderer defTableCellRenderer = new DefaultTableCellRenderer();
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component c = defTableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        c.setBackground(Color.WHITE);
		
		if(value == null) {
			c.setBackground(COLOR_BACKGROUND);
			return c;
			
		} else if(hasFocus) {
			c.setBackground(COLOR_FOCUS);	
			return c;
			
		} else if(Name.class.isAssignableFrom(value.getClass())) {
			Name name = (Name) value;
			String str = name.toString();
			
			if(currentMatch == null) {
				if(str.length() == 0) {
					c.setBackground(COLOR_BLANK_CELL);
				} else {
					c.setBackground(COLOR_NAME_UNMATCHED);
				}
			} else {
				RowIndex against = currentMatch.getAgainst();
				
				// System.err.println("Checking against: " + against);
				
				if(str.length() == 0) {
					c.setBackground(COLOR_BLANK_CELL);
				} else if(against.hasName(str)) {
					c.setBackground(COLOR_NAME_FULL_MATCH);
				} else if(against.hasName(Name.getName(str).getGenus())) {
					c.setBackground(COLOR_NAME_GENUS_MATCH);
				} else {
					c.setBackground(COLOR_NAME_NO_MATCH);
				}
			}
		} else {
			// Not a name? Note that Strings will NOT make it here: we don't
			// push Strings through this. So this is really just for later.
			c.setBackground(COLOR_BACKGROUND);
		}
		
		if(isSelected)
			c.setBackground(c.getBackground().darker());
		
		return c;
	}
	
	public static final Color COLOR_BACKGROUND = new Color(255, 159, 0);
	public static final Color COLOR_NAME_NO_MATCH = new Color(226, 6, 44);
	public static final Color COLOR_NAME_GENUS_MATCH = new Color(255, 117, 24);
	public static final Color COLOR_NAME_FULL_MATCH = new Color(0, 128, 0);
	public static final Color COLOR_BLANK_CELL = Color.GRAY;
	public static final Color COLOR_NAME_UNMATCHED = new Color(137, 207, 230);
	public static final Color COLOR_FOCUS = Color.RED;

	public JFrame getMainFrame() {
		return mainFrame;
	}
}