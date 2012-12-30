
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
	/* CONSTANTS OR CONSTANT-LIKE THINGS */
	/** A basic title -- we'll stick the filename on at the end if necessary. */
	private String basicTitle = TaxRef.getName() + "/" + TaxRef.getVersion();
	
	/* USER INTERFACE VARIABLES */
	/** The main frame encapsulated herein. */
	private final JFrame mainFrame;
	/** The table which displays the names. */
	private final JTable table = new JTable();
	/** The operations drop down, which controls the right rightPanel. */
	private final JComboBox operations = new JComboBox();
	/** The text area which shows what's happening on the right rightPanel. */
	private final JTextArea results = new JTextArea("Please choose an operation from the dropdown above.");
	/** A progress bar which displays memory usage continuously. */
	private final JProgressBar progressBar = new JProgressBar(0, 100);
	/** A match information rightPanel. */
	MatchInformationPanel matchInfoPanel;
	
	/* VARIABLES */
	/** 
	 * The DarwinCSV which is currently open. Remember that this encapsulates
	 * a RowIndex, but it can also be used to write the file back out. I'm not
	 * really sure what the plan here is.
	 */
	DarwinCSV currentCSV = null;
	/** The match currently in progress. */
	RowIndexMatch currentMatch = null; 
	/** A blank data model. */
	TableModel blankDataModel = new AbstractTableModel() {
		public String getColumnName(int x) { return ""; }
		public int getColumnCount() { return 6; }
		public int getRowCount() { return 6;}
		public Object getValueAt(int row, int col) { return ""; }
	};
	
	/* CLASSES */
	private class MainFrameWorker extends SwingWorker<Object, Object> {
		protected Object input;
		
		public MainFrameWorker() {
			// Turn on indeterminate when initialized.
			progressBar.setIndeterminate(true);
		}
		
		public MainFrameWorker(Object input) {
			this.input = input;
		}
		
		@Override
		protected Object doInBackground() throws Exception {
			// Needs to be overridden.
			throw new UnsupportedOperationException("Not supported yet.");
		}
		
		@Override
		protected void done() {
			// Turn off indeterminate.
			progressBar.setIndeterminate(false);
			
			// Check for exceptions, and display them if necessary.
			try {
				get();
			} catch(Exception e) {
				MessageBox.messageBox(
					mainFrame, 
					"Error during processing", 
					"The following error occurred during processing: " + e.getMessage(), 
					MessageBox.ERROR
				);
			}
		}
	};
	
	/**
	 * Create a new, empty, not-visible TaxRef window. Really just activates 
	 * the setup frame and setup memory monitor components, then starts things 
	 * off.
	 */
	public MainFrame() {
		setupMemoryMonitor();
		
		// Set up the main frame.
		mainFrame = new JFrame(basicTitle);
		mainFrame.setJMenuBar(setupMenuBar());
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set up the JTable.
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setSelectionBackground(COLOR_BACKGROUND);
		table.setShowGrid(true);
		
		// Add a blank table model so that the component renders properly on
		// startup.
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
		
		// Set up the left panel.
		JPanel leftPanel = new JPanel();
		
		matchInfoPanel = new MatchInformationPanel();
		leftPanel.setLayout(new BorderLayout());
		leftPanel.add(matchInfoPanel, BorderLayout.SOUTH);
		leftPanel.add(new JScrollPane(table));
		
		// Set up the right panel.
		JPanel rightPanel = new JPanel();
		rightPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		
		progressBar.setStringPainted(true);
		
		rightPanel.setLayout(new BorderLayout());
		rightPanel.add(operations, BorderLayout.NORTH);
		rightPanel.add(new JScrollPane(results));
		rightPanel.add(progressBar, BorderLayout.SOUTH);
		
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
				
				if(currentCSV != null)
					results.setText("O NO");
				else
					results.setText("No file loaded.");
				
				results.setCaretPosition(0);
			}
		});
		operations.addItem("No files loaded as yet.");	
		
		// Set up a JSplitPane to split the panels up.
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, leftPanel, rightPanel);
		split.setResizeWeight(1);
		mainFrame.add(split);
		mainFrame.pack();
		
		// Set up a drop target so we can pick up files 
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
						
						// If we're given multiple files, pick up only the last file and load that.
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
	
	/**
	 * Set up the menu bar.
	 * 
	 * @return The JMenuBar we set up.
	 */
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
				File file;
				if(fd.getDirectory() != null) {
					file = new File(fd.getDirectory(), fd.getFile());
				} else if(fd.getFile() != null) {
					file = new File(fd.getFile());
				} else {
					return;
				}
				
				// Clear out old file.
				loadFile(null);
				
				// SwingWorker MAGIC!
				new MainFrameWorker(file) {
					@Override
					protected Object doInBackground() throws Exception {
						loadFile((File)input, DarwinCSV.FILE_CSV_DELIMITED);
						
						return null;
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

				File file;
				if(fd.getDirectory() != null) {
					file = new File(fd.getDirectory(), fd.getFile());
				} else if(fd.getFile() != null) {
					file = new File(fd.getFile());
				} else {
					return;
				}
				
				// Clear out old file
				loadFile(null);
				
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
				
				File file;
				if(fd.getDirectory() != null) {
					file = new File(fd.getDirectory(), fd.getFile());
				} else if(fd.getFile() != null) {
					file = new File(fd.getFile());
				} else {
					return;
				}
				
				// Clear out old file
				loadFile(null);
				
				// SwingWorker MAGIC!
				new MainFrameWorker(file) {
					@Override
					protected Object doInBackground() throws Exception {
						loadFile((File)input, DarwinCSV.FILE_TAB_DELIMITED);
						
						return null;
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
				
				File file;
				if(fd.getDirectory() != null) {
					file = new File(fd.getDirectory(), fd.getFile());
				} else if(fd.getFile() != null) {
					file = new File(fd.getFile());
				} else {
					return;
				}
				
				// SwingWorker MAGIC!
				new MainFrameWorker(file) {
					@Override
					protected Object doInBackground() throws Exception {
						currentCSV.saveToFile((File)input, DarwinCSV.FILE_CSV_DELIMITED);
						
						return null;
					}
				}.execute();
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
				
				if(fd.getFile() == null)
					return;
				
				File file = new File(fd.getFile());
				if(fd.getDirectory() != null) {
					file = new File(fd.getDirectory(), fd.getFile());
				}
				
				// Clear out old match against.
				matchAgainst(null);
				
				// SwingWorker MAGIC!
				new MainFrameWorker(file) {
					@Override
					protected Object doInBackground() throws Exception {
						DarwinCSV csv_matcher = new DarwinCSV((File)input, DarwinCSV.FILE_CSV_DELIMITED);
						matchAgainst(csv_matcher);
						
						return null;
					}
				}.execute();
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
		if(csv != null) {
			table.setModel(currentCSV.getRowIndex());
		} else {
			table.setModel(blankDataModel);
		}
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
	
	private void loadFile(File file) {
		// Eventually, this will be some code to figure out what kind of file
		// it is. But for now ...
		loadFile(file, DarwinCSV.FILE_CSV_DELIMITED);
	}
	
	private void loadFile(File file, short type) {
		if(file == null) {
			mainFrame.setTitle(basicTitle);
			setCurrentCSV(null);
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
	
	/*
	public String generateTextSummaryOfColumn(String colName) {
		StringBuilder builder = new StringBuilder();
		
		if(colName == null) {
			// Report on the name and the match.
		
			builder.append("Currently loaded file: ").append(file.getAbsolutePath()).append("\n");
			builder.append("  Number of rows: ").append(data.size()).append("\n");
			builder.append("  Scientific name column: ").append(getColumnInformation(col_scientificname)).append("\n");
			builder.append("  Canonical name column: ").append(getColumnInformation(col_canonicalname)).append("\n");
			
		} else {
			// Report on the column.
			int colIndex = column(colName);
			
			builder.append("Information about column: ").append(getColumnInformation(colIndex)).append("\n");
			
			// This should really be cached!
			int blank_rows = 0;
			int total_non_blank = 0;
			int total_matched = 0;
			int total_genus_matched = 0;
			int total_not_matched = 0;
			HashMap<String, Integer> uniqueValues = new HashMap<String, Integer>();
			HashSet<String> matchedNames = new HashSet<String>();
			HashSet<String> matchedGenusNames = new HashSet<String>();
			boolean matched = false;
			
			for(String[] row: data) {
				String val = row[colIndex];
				if(val == null || val.equals("")) {
					blank_rows++;
				} else {
					total_non_blank++;
					
					if(!uniqueValues.containsKey(val)) {
						uniqueValues.put(val, new Integer(1));
					} else {
						uniqueValues.put(val, new Integer(uniqueValues.get(val).intValue() + 1));
					}
					
					if(matcher != null && (colIndex == col_family || colIndex == col_scientificname || colIndex == col_acceptedname || colIndex == col_canonicalname)) {
						matched = true;
						
						if(matcher.hasName(val)) {
							matchedNames.add(val);
							total_matched++;
						} else if(matcher.hasName(new Name(val).getGenus())) {
							matchedGenusNames.add(val);
							total_genus_matched++;
						} else {
							total_not_matched++;
						}
					}
				}
			}
			
			int possible_values = uniqueValues.size();
			builder.append("  Possible values: ").append(possible_values).append("\n");
			builder.append("    Blank rows: ").append(number_and_percentage(blank_rows, data.size())).append("\n");
			builder.append("    Non-blank rows: ").append(number_and_percentage(total_non_blank, data.size())).append("\n");
			
			if(matched) {
				builder.append("    Names were matched against ").append(matcher.toString()).append(". (percentages are against non-blank rows)\n");
				builder.append("      Matched names: ").append(number_and_percentage(total_matched, total_non_blank)).append("\n");
				builder.append("      Matched genus names: ").append(number_and_percentage(total_genus_matched, total_non_blank)).append("\n");
				builder.append("      Unmatched names: ").append(number_and_percentage(total_not_matched, total_non_blank)).append("\n");
			}
			
			builder.append("\n");
			builder.append("    Values: (percentages refer to total non-blank rows)\n");
		
			ValueComparator<String, Integer> comparator = new ValueComparator<String, Integer>(uniqueValues);
			TreeSet<String> sortByValues = new TreeSet<String>(comparator);
			sortByValues.addAll(uniqueValues.keySet());
			
			for(String val: sortByValues.descendingSet()) {
				if(val == null)
					val = "(null)";
				Integer count = uniqueValues.get(val);
				if(count == null)
					count = new Integer(-1);
				
				String s_matched = "";
				if(matched) {
					if(matchedNames.contains(val))
						s_matched = "\tmatched";
					else if(matchedGenusNames.contains(val))
						s_matched = "\tmatched to genus";
					else
						s_matched = "\tnot matched";
				}
				
				builder.append("\t").append(val).append(s_matched).append("\t").append(number_and_percentage(count.intValue(), total_non_blank)).append("\n");
			}
		}
		
		return builder.toString();
	}
	* */
}