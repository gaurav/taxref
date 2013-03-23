
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
import com.ggvaidya.TaxRef.Common.*;
import com.ggvaidya.TaxRef.Model.*;
import com.ggvaidya.TaxRef.Model.Datatype.*;
import com.ggvaidya.TaxRef.Net.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
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
	/** The text area which shows what's happening on the right rightPanel. */
	private final JTextArea results = new JTextArea("Please choose an operation from the dropdown above.");
	/** A progress bar which displays memory usage continuously. */
	private final JProgressBar progressBar = new JProgressBar(0, 100);
	
	/** A match information rightPanel. */
	MatchInformationPanel matchInfoPanel;
	
	ColumnInformationPanel columnInfoPanel;
	
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

	void lookUpTaxonID(String taxonID) {
		URI url;
		
		try {
			// We should look up the miITIS_TSN status, but since we don't
			// have any options there ...
			url = new URI("http://www.itis.gov/servlet/SingleRpt/SingleRpt?search_topic=TSN&search_value=" + taxonID);
		} catch(URISyntaxException e) {
			throw new RuntimeException(e);
		}
		
		try {
			Desktop desktop = Desktop.getDesktop();
			desktop.browse(url);
		
		} catch(IOException e) {
			MessageBox.messageBox(
				mainFrame, 
				"Could not open URL '" + url + "'", 
				"The following error occurred while looking up URL '" + url + "': " + e.getMessage(), 
				MessageBox.ERROR
			);
		}
	}

	void searchName(String nameToMatch) {
		URI url;
		
		try {
			// We should look up the miITIS_TSN status, but since we don't
			// have any options there ...
			url = new URI("http", "www.google.com", "/search", "q=" + nameToMatch);
			// I think the URI handles the URL encoding?
			
		} catch(URISyntaxException e) {
			throw new RuntimeException(e);
		}
		
		try {
			Desktop desktop = Desktop.getDesktop();
			desktop.browse(url);
		
		} catch(IOException e) {
			MessageBox.messageBox(
				mainFrame, 
				"Could not open URL '" + url + "'", 
				"The following error occurred while looking up URL '" + url + "': " + e.getMessage(), 
				MessageBox.ERROR
			);
		}
	}

	public RowIndex getDisplayedRowIndex() {
		if(currentCSV == null)
			return null;
		else
			return currentCSV.getRowIndex();
	}

	public RowIndexMatch getCurrentMatch() {
		return currentMatch;
	}
	
	/* CLASSES */
	
	/** This class will load a file for you. */
	private class DarwinCSVLoader extends SwingWorker<DarwinCSV, File> {
		File file;
		short darwinCSVFileType;
		DarwinCSV result;
		
		public DarwinCSVLoader(File input, short darwinCSVFileType) {
			file = input;
			this.darwinCSVFileType = darwinCSVFileType;
			
			// Turn on indeterminate when initialized.
			progressBar.setIndeterminate(true);
		}
		
		@Override
		protected DarwinCSV doInBackground() throws Exception {
			return new DarwinCSV(file, darwinCSVFileType);
		}
		
		@Override
		protected void done() {
			// Turn off indeterminate.
			progressBar.setIndeterminate(false);
			
			// Check for exceptions, and display them if necessary.
			try {
				result = get();
			} catch(Exception e) {
				result = null;
				
				StringWriter stack_trace = new StringWriter();
				e.printStackTrace(new PrintWriter(stack_trace));
				
				MessageBox.messageBox(
					mainFrame, 
					"Error while loading file '" + file + "'", 
					"The following error occurred while loading '" + file + "': " + e.getMessage() + "\n\nStack trace: " + stack_trace, 
					MessageBox.ERROR
				);
			}
		}
		
		public DarwinCSV getDarwinCSV() {
			return result;
		}
	};
	
	protected DarwinCSV loadDarwinCSV(File f, short darwinCSVtype) {
		DarwinCSVLoader l = new DarwinCSVLoader(f, darwinCSVtype);		
		l.run();
		
		// Might be null!
		return l.getDarwinCSV();
	}
	
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
		table.setSelectionBackground(COLOR_SELECTION_BACKGROUND);
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
				
				columnInfoPanel.columnChanged(column);
				
				if(row == -1 || column == -1) {
					// nothing selected?
					matchInfoPanel.nameSelected(currentCSV.getRowIndex(), null, -1, -1);
				} else {
					Object o = table.getModel().getValueAt(row, column);
					if(Name.class.isAssignableFrom(o.getClass())) {
						matchInfoPanel.nameSelected(currentCSV.getRowIndex(), (Name) o, row, column);
					} else {
						matchInfoPanel.nameSelected(currentCSV.getRowIndex(), null, -1, -1);
					}
				}
			}
		});
		
		// Set up the left panel (table + matchInfoPanel)
		JPanel leftPanel = new JPanel();
		
		matchInfoPanel = new MatchInformationPanel(this);
		leftPanel.setLayout(new BorderLayout());
		leftPanel.add(matchInfoPanel, BorderLayout.SOUTH);
		leftPanel.add(new JScrollPane(table));
		
		// Set up the right panel (columnInfoPanel)
		JPanel rightPanel = new JPanel();
		
		columnInfoPanel = new ColumnInformationPanel(this);
		
		progressBar.setStringPainted(true);
		
		rightPanel.setLayout(new BorderLayout());
		rightPanel.add(columnInfoPanel);
		rightPanel.add(progressBar, BorderLayout.SOUTH);
		
		// Set up a JSplitPane to split the panels up.
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, leftPanel, rightPanel);
		split.setResizeWeight(1);
		mainFrame.add(split);
		mainFrame.pack();
		
		// Set up a drop target so we can pick up files 
		mainFrame.setDropTarget(new DropTarget(mainFrame, new DropTargetAdapter() {
			@Override
			public void dragEnter(DropTargetDragEvent dtde) {
				// Accept any drags.
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
				// Accept a drop as long as its File List.
				
				if(dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
					
					Transferable t = dtde.getTransferable();
					
					try {
						java.util.List<File> files = (java.util.List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
						
						// If we're given multiple files, pick up only the last file and load that.
						File f = files.get(files.size() - 1);
						loadDarwinCSV(f, DarwinCSV.FILE_CSV_DELIMITED);
						
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
				setCurrentCSV(null);
				DarwinCSV csv = loadDarwinCSV(file, DarwinCSV.FILE_CSV_DELIMITED);
				setCurrentCSV(csv);
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
				setCurrentCSV(null);
				try {
					setCurrentCSV(new DarwinCSV(file, DarwinCSV.FILE_CSV_DELIMITED));
				} catch(IOException exception) {
					MessageBox.messageBox(
						mainFrame, 
						"Could not load from file '" + file + "'", 
						"Error occured while loading from file '" + file + "': " + exception,
						MessageBox.MB_ERROR
					);
				}
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
				setCurrentCSV(null);
				DarwinCSV dcsv = loadDarwinCSV(file, DarwinCSV.FILE_TAB_DELIMITED);
				setCurrentCSV(dcsv);
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
				
				try {
					currentCSV.saveToFile(file, DarwinCSV.FILE_CSV_DELIMITED);
					
					MessageBox.messageBox(
						mainFrame, 
						"Saved successfully to '" + file + "'", 
						currentCSV.getRowIndex().getRowCount() + " rows successfully saved as a CSV to '" + file + "'",
						MessageBox.MB_OK
					);
					
				} catch(IOException exception) {
					MessageBox.messageBox(
						mainFrame, 
						"Could not save to file '" + file + "'", 
						"Error occured to file '" + file + "': " + exception,
						MessageBox.MB_ERROR
					);
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
				
				if(fd.getFile() == null)
					return;
				
				File file = new File(fd.getFile());
				if(fd.getDirectory() != null) {
					file = new File(fd.getDirectory(), fd.getFile());
				}
				
				// Clear out old match against.
				table.setEnabled(false);
				matchAgainst(null);
				DarwinCSV dsv = loadDarwinCSV(file, DarwinCSV.FILE_CSV_DELIMITED);
				matchAgainst(dsv);
				table.setEnabled(true);
			}
		});
		matchMenu.add(miMatchCSV);
		
		/* Match -> Against ITIS */
		JMenuItem miMatchITIS = new JMenuItem(new AbstractAction("Match against ITIS") {
			@Override
			public void actionPerformed(ActionEvent e) {
				table.setEnabled(false);
				DarwinCSV csv = DownloadITIS.getIt(mainFrame);
				matchAgainst(csv);
				table.repaint();
				table.setEnabled(true);
			}
		});
		matchMenu.add(miMatchITIS);
		
		/* TaxonID */
		JMenu taxonIDMenu = new JMenu("TaxonIDs");
		menuBar.add(taxonIDMenu);
		
		/* TaxonID -> Treat TaxonIDs as ... */
		JMenu treatTaxonIDsAs = new JMenu("Treat TaxonIDs as ...");
		taxonIDMenu.add(treatTaxonIDsAs);
		
		/* TaxonID -> Treat -> ITIS TSNs */
		JCheckBoxMenuItem miITIS_TSNs = new JCheckBoxMenuItem(new AbstractAction("ITIS TSNs") {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Don't let the user unselect this.
				((JCheckBoxMenuItem)e.getSource()).setSelected(true);
			}
		});
		miITIS_TSNs.setSelected(true);
		treatTaxonIDsAs.add(miITIS_TSNs);
		
		/* TaxonID -> Duplicate column */
		JMenuItem miTaxonID_duplicateColumn = new JMenuItem(new AbstractAction("Duplicate current column") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(currentCSV == null)
					return;
				
				int col = table.getSelectedColumn();
				if(col == -1)
					return;
				
				RowIndex rowIndex = currentCSV.getRowIndex();
				String colName = rowIndex.getColumnName(col);
				
				rowIndex.setColumnClass(colName + "_duplicate", String.class);
				rowIndex.createNewColumn(colName + "_duplicate", col + 1, colName, new MapOperation() {
					@Override
					public Object mapTo(Object value) {
						return value;
					}
				});
				
				System.err.println("Done, number of columns: " + rowIndex.getColumnCount());
				table.repaint();
			}
		});
		taxonIDMenu.add(miTaxonID_duplicateColumn);
		
		/* TaxonID -> Unroll parentNameUsageId column */
		JMenuItem miTaxonID_unrollParentNameUsageId = new JMenuItem(new AbstractAction("Recursively unroll IDs via primary key") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(currentCSV == null)
					return;
				
				int col = table.getSelectedColumn();
				if(col == -1)
					return;
				
				RowIndex rowIndex = currentCSV.getRowIndex();
				// String colName = rowIndex.getColumnName(col);
				int col_to_recurse = col;
				
				// TODO: replace this with a dialog box.
				int col_for_colname = rowIndex.getColumnIndex("taxonRank");
				int col_for_value = rowIndex.getColumnIndex("scientificName");
				
				// Let's go.
				rowIndex.silenceListeners();
				table.setEnabled(false);
				progressBar.setIndeterminate(true);
				
				/*
				class Unroller extends SwingWorker<String, Integer> {
					RowIndex rowIndex;
					String colName;
					int col_to_recurse;
					int col_for_colname;
					int col_for_value;
					
					private Unroller(RowIndex r, int c, int col_colname, int col_value) {
						rowIndex = r;
						col_to_recurse = c;
						col_for_colname = col_colname;
						col_for_value = col_value;
					}
					
					@Override
					protected String doInBackground() throws Exception {*/
						rowIndex.setColumnClass("rc:status", String.class);
						rowIndex.createNewColumn("rc:status");
						int rc_status_index = rowIndex.getColumnIndex("rc:status");
						
						for(int row_index = 0; row_index < rowIndex.getRowCount(); row_index++) {
							Object[] row = rowIndex.getRow(row_index);
							Object value = row[col_to_recurse];
							
							while(!(value == null || value.equals(""))) {
								// Add this value to the table.
								Object putative_colName = row[col_for_colname];
								if(putative_colName == null) {
									rowIndex.setValueAt("Stopped at #" + value + ": no column name", row_index, rc_status_index);
									break;
								}
								String colName = putative_colName.toString();
								
								if(!rowIndex.hasColumn("rc:" + colName)) {
									rowIndex.setColumnClass("rc:" + colName, 
										rowIndex.getColumnClass(col_for_value)
									);
									rowIndex.createNewColumn("rc:" + colName);
								}
								
								int col_new_col = rowIndex.getColumnIndex("rc:" + colName);
								
								// System.err.println("Setting (row: " + row_index + ", col: " + col_new_col + ") to '" + value + "'");
								Object putative_value = row[col_for_value];
								Object set_value;
								
								if(putative_value == null)
									set_value = value;
								else
									set_value = putative_value;
								
								rowIndex.setValueAt(set_value, row_index, col_new_col);
								
								// Keep recursin'
								java.util.List<Object[]> rows = rowIndex.getPrimaryKeyRows(value.toString());
								if(rows == null || rows.isEmpty())
									break;
								
								if(rows.size() > 1) {
									rowIndex.setValueAt("Multiple values found for #" + value.toString(), row_index, rc_status_index);
									break;
								}
								
								Object new_value = rows.get(0)[col_to_recurse];
								if(new_value == value) {
									rowIndex.setValueAt("", row_index, rc_status_index);
									break;
								}
								
								// System.err.println("Recursing to '" + new_value + "'");
								value = new_value;
							}
						}
						/*
						return "";
					}
					
					@Override
					protected void done() {*/
						rowIndex.unsilenceListeners();
						table.setEnabled(true);
						progressBar.setIndeterminate(false);
						
						MessageBox.messageBox(mainFrame, "Done!", "Unrolled!");
					/*}
				}
				
				Unroller unroller = new Unroller(rowIndex, col, col_for_colname, col_for_value);
				unroller.run();
				*/
			}
		});
		taxonIDMenu.add(miTaxonID_unrollParentNameUsageId);
		
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
	
	/**
	 * The Memory Monitor sets up a 5 second Timer which displays the amount
	 * of remaining memory (compared to the total memory we have).
	 */
	private void setupMemoryMonitor() {
		memoryTimer = new java.util.Timer("Memory monitor", true);
		
		memoryTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				// We need to set this off in the Event Thread.
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						// Calculate the memory we have.
						long value = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
						long max = Runtime.getRuntime().maxMemory() / (1024 * 1024);
						int percentage = (int)(((double)value)/max*100);
						
						// Set the progress bar.
						progressBar.setMinimum(0);
						progressBar.setMaximum(100);
						progressBar.setValue(percentage);
						
						progressBar.setString(value + " MB out of " + max + " MB (" + percentage + "%)");
					}
					
				});
			}
		
		}, new Date(), 5000);	// Every five seconds.
	}
	
	/**
	 * Set the current open DarwinCSV. You should set this to null before 
	 * loading a new one to avoid running out of memory.
	 * 
	 * @param csv The new DarwinCSV object.
	 */
	private void setCurrentCSV(DarwinCSV csv) {
		if(!SwingUtilities.isEventDispatchThread()) {
			System.err.println("setCurrentCSV(" + csv + ") invoked from outside an event dispatch thread!");
		}
		
		// Clear the old currentCSV object and matchAgainst object.
		currentCSV = null;
		matchAgainst(null);
		
		// Load the new currentCSV object.
		currentCSV = csv;
		table.removeAll();
		table.setDefaultRenderer(Name.class, this);
		table.setDefaultRenderer(PrimaryKey.class, this);
		
		// Set the currentCSV 
		if(csv != null) {
			File file = csv.getFile();
			mainFrame.setTitle(basicTitle + ": " + file.getName() + " (" + String.format("%,d", currentCSV.getRowIndex().getRowCount()) + " rows)");
			table.setModel(currentCSV.getRowIndex());
		} else {
			mainFrame.setTitle(basicTitle);
			table.setModel(blankDataModel);
		}
		
		columnInfoPanel.loadedFileChanged(csv);
		columnInfoPanel.columnChanged(-1);
		table.repaint();
	}
	
	/**
	 * Set the DarwinCSV to match this against.
	 * 
	 * @param against The DarwinCSV object to match against.
	 */
	private void matchAgainst(DarwinCSV against) {
		if(currentCSV == null) {
			currentMatch = null;
			return;
		}
		
		if(against == null) {
			return;
		}
		
		// long t1 = System.currentTimeMillis();
		currentMatch = currentCSV.getRowIndex().matchAgainst(against.getRowIndex());
		table.repaint();
		matchInfoPanel.matchChanged(currentMatch);
		columnInfoPanel.matchChanged(currentMatch);
		// long t2 = System.currentTimeMillis();
		
		// System.err.println("matchAgainst finished: " + (t2 - t1) + " ms");
	}
	
	/**
	 * The default table cell renderer. We pass on cell rendering instructions to it.
	 */
	private DefaultTableCellRenderer defTableCellRenderer = new DefaultTableCellRenderer();
	
	/**
	 * Renders a table cell in the main JTable. As a TableCellRenderer, we have 
	 * to implement this method, but we use it to colour different types of
	 * matches in different ways. Remember that this is run every time a cell
	 * is displayed on the screen, so it needs to be as fast as can be.
	 * 
	 * @param table The table which needs rendering.
	 * @param value The object which needs rendering. For now, this can only be
	 *	a Name object, but later on we might colour different types of cells in
	 *	different ways.
	 * @param isSelected Is this cell selected, i.e. is the row selected?
	 * @param hasFocus Is this cell focused, i.e. is this individual cell selected?
	 * @param row The row coordinate of this cell.
	 * @param column The column coordinate of this cell.
	 * @return A component representing this cell.
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		// TODO: Check if we can get a saving out of this by just rendering a JLabel/JTextField directly.
		Component c = defTableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
		// Set all backgrounds to white.
        c.setBackground(Color.WHITE);
		
		if(value == null) {
			// Null values look null-ish.
			c.setBackground(COLOR_NULL);
			return c;
			
		} else if(hasFocus) {
			// ANY cell with focus should look focussed.
			c.setBackground(COLOR_FOCUS);	
			return c;
			
		} else if(currentCSV != null && PrimaryKey.class.isAssignableFrom(currentCSV.getRowIndex().getColumnClass(column))) {
			int rows = currentCSV.getRowIndex().getPrimaryKeyRows((String) value).size();
			if(rows > 1) {
				c.setBackground(COLOR_PK_MULTIPLE);
			} else {
				c.setBackground(COLOR_PK_SINGLE);
			}
			
		} else if(Name.class.isAssignableFrom(value.getClass())) {
			// Aha, a Name! Color it special.
			Name name = (Name) value;
			int str_length = name.toString().length();
			
			if(currentMatch == null) {
				// No current match? Then just colour blank cells blank,
				// and unmatched name colours special so people know that
				// they have been recognized as names.
				
				if(str_length == 0) {
					c.setBackground(COLOR_BLANK_CELL);
				} else {
					c.setBackground(COLOR_NAME_UNMATCHED);
				}
			} else {
				// So which RowIndex is the match against?
				RowIndex against = currentMatch.getAgainst();
				
				// System.err.println("Checking against: " + against);
				
				if(str_length == 0) {
					// Mark blank cells as such.
					c.setBackground(COLOR_BLANK_CELL);
				} else if(against.hasName(name)) {
					// Perfect match!
					c.setBackground(COLOR_NAME_FULL_MATCH);
				} else if(against.hasName(name.getGenus())) {
					// Genus-match.
					c.setBackground(COLOR_NAME_GENUS_MATCH);
				} else {
					// No match!
					c.setBackground(COLOR_NAME_NO_MATCH);
				}
			}
			
		} else {
			// Not a name? Note that Strings will NOT make it here: we don't
			// push Strings through this. So this is really just for later.
			c.setBackground(COLOR_NULL);
		}
		
		// If the row is selected, make it darker.
		if(isSelected)
			c.setBackground(c.getBackground().darker());
		
		return c;
	}
	
	/** A blank or non-existent cell. */
	public static final Color COLOR_NULL = Color.gray;
	
	/** A name which could not be matched. */
	public static final Color COLOR_NAME_NO_MATCH = new Color(226, 6, 44);
	
	/** A name which was matched at the genus level but not the species level. */
	public static final Color COLOR_NAME_GENUS_MATCH = new Color(255, 117, 24);
	
	/** A name which was completely and properly matched. */
	public static final Color COLOR_NAME_FULL_MATCH = new Color(0, 128, 0);
	
	/** A cell which has a valid Name, which has a zero-length string. */
	public static final Color COLOR_BLANK_CELL = Color.GRAY;
	
	/** A Name cell which has not matched against anything. */
	public static final Color COLOR_NAME_UNMATCHED = new Color(137, 207, 230);
	
	/** A focused Name cell has its own, distinctive colour, so you know that you
	 can edit it. Maybe? */
	public static final Color COLOR_FOCUS = Color.RED;
	
	// PK: primary key, red: multiple matches, green: single match.
	public static final Color COLOR_PK_MULTIPLE = Color.RED;
	public static final Color COLOR_PK_SINGLE = Color.GREEN;

	/** 
	 * The colour of non-Name cells which have been selected. Note that selected
	 * Name cells will currently be in COLOR_NAME_UNMATCHED.darker() or
	 * COLOR_NAME_*_MATCH.darker(). Also, ALL focused cells are COLOR_FOCUS,
	 * whether Name or not.
	 */
	public static final Color COLOR_SELECTION_BACKGROUND = Color.BLUE;

	/** Return the JFrame which is the main frame of this application. */
	public JFrame getMainFrame() {
		return mainFrame;
	}
	
	/** Return the JTable which is at the heart of this application. */
	public JTable getJTable() {
		return table;
	}
	
	/**
	 * Move the currently selected cell up or down by one cell.
	 * 
	 * @param direction -1 for previous, +1 for next.
	 */
	public void goToRow(int direction) {
		int row = table.getSelectedRow();
		int column = table.getSelectedColumn();
		
		table.changeSelection(row + direction, column, false, false);
	}

	public void goToColumn(int direction) {
		// TODO
	}
	
	void goToSpecificColumn(int selectedIndex) {
		int row = table.getSelectedRow();
		
		table.changeSelection(row, selectedIndex, true, true);
	}
}