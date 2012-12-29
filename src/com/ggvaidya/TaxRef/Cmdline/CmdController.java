
/*
 *
 *  Controller.java
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
package com.ggvaidya.TaxRef.Cmdline;

import au.com.bytecode.opencsv.*;
import com.ggvaidya.TaxRef.*;
import com.ggvaidya.TaxRef.Model.*;
import com.ggvaidya.TaxRef.UI.*;
import java.io.*;
import org.apache.commons.cli.*;

/**
 * The CmdLine Controller class handles command line instructions to
 * TaxonValid. Unlike TaxonDNA, TaxonValid should be as completely
 * command-line driven as possible.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class CmdController {
	public static void handle(String[] args) {
		Options cmdLineOptions = new Options();
		setupOptions(cmdLineOptions);
		
		// Parse command line options (if possible).
		CommandLineParser parser = new PosixParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(cmdLineOptions, args);
		} catch (ParseException ex) {
			System.err.println("Could not parse command line: " + ex);
			System.exit(1);
			return;
		}
		
		// --version: report the version and copyright information, then exit.
		if(cmd.hasOption("version")) {
			System.err.println(TaxRef.getName() + "/" + TaxRef.getVersion());
			System.err.println(TaxRef.getDescription());
			System.err.println(TaxRef.getCopyright());
			System.err.println();
			System.exit(0);
		}
		
		// boolean case_sensitive = (cmd.getOptionValue("case") == null) ? false : true;
		// TODO: actually write code here.
		
		MainFrame mf = new MainFrame();
		mf.getMainFrame().setVisible(true);
	}
	
	// To be recombined once taxref becomes a command line tool.
	private static void excisedCode(boolean case_sensitive, CommandLine cmd, CommandLineParser parser) {
		/*
		System.err.println("Case sensitive: " + case_sensitive);
		
		NameList nl = new NameList();
		if(cmd.hasOption("valids")) {
			String validNameFile = cmd.getOptionValue("valids");
			try {
				BufferedReader fr = new BufferedReader(new FileReader(new File(validNameFile)));
				while(fr.ready()) {
					String line = fr.readLine();
					String name = line.trim();
					if(case_sensitive)
						nl.addName(name);
					else
						nl.addName(name.toLowerCase());
				}
				System.err.println("Valid names file loaded; checking against " + nl.count() + " names.");
			} catch(FileNotFoundException e) {
				System.err.println("Unable to load valid name file '" + validNameFile + "': " + e);
				System.exit(1);
			} catch(IOException e) {
				System.err.println("Error reading valid name file '" + validNameFile + ": " + e);
				System.exit(1);
			}
		} else {
			System.err.println("No valid names to compare names against.");
			System.exit(0);
		}
		
		/* If all else fails, process the names provided on the command line */ /*
		int field = 0;
		String s_field = cmd.getOptionValue("field");
		if(s_field == null)
			field = 0;
		else
			field = Integer.valueOf(s_field).intValue();
		
		processCSVNames(new InputStreamReader(System.in), field, nl, case_sensitive);
		*/
	}

	private static void setupOptions(Options cmdLineOptions) {
		/** -v, --version: Display version information */
		cmdLineOptions.addOption(
			"v",
			"version", 
			false, 
			"Display version information for this software"
		);
		
		/** -vn, --valids: Valid names */
		cmdLineOptions.addOption(
			"vn",
			"valids",
			true,
			"A file containing a plain text list of valid names."
		);
		
		/** -fd, --field: field name */
		cmdLineOptions.addOption(
			"fd",
			"field",
			true,
			"Field index"
		);
		
		/** -cs, --case: case-sensitive comparison */
		cmdLineOptions.addOption(
			"cs",
			"case",
			false,
			"Turn on case-sensitive comparisons."
		);
	}
	
	private static void processCSVNames(Reader names, int field, boolean case_sensitive) {
		/*
		 * Arg NameList nl deleted.
		 * 
		CSVReader reader = new CSVReader(names);
		CSVWriter writer = new CSVWriter(new OutputStreamWriter(System.out));
	
		// Copy the header.
		try {
			String[] header_line = reader.readNext();
			writer.writeNext(header_line);
			
		} catch(IOException e) {
			System.err.println("Could not read header line, exiting.");
			System.exit(1);
		}
		
		int count_total = 0;
		int count_valid = 0;
		int count_invalid = 0;
		
		while(true) {
			String[] line;
			String name;
			
			if(count_total % 10000 == 1) {
				System.err.println(count_total + " names processed.");
			}
			
			try {
				line = reader.readNext();
				if(line == null)
					break;
				
				name = line[field];
				if(!case_sensitive)
					name = name.toLowerCase();
				
			} catch(EOFException e) {
				break;
				
			} catch(IOException e) {
				System.err.println("Unable to read input: " + e);
				return;
			}
			
			count_total++;
			if(nl.hasName(name)) {
				count_valid++;
				// System.out.println(name + "\tvalid");
				writer.writeNext(line);
			} else {
				count_invalid++;
				// System.out.println(name + "\tinvalid");
				//writer.writeNext(line);
			}
		}
		
		// Summary!
		System.err.println("\nNames: " + count_total + "\n" +
			"  Valid names:   " + count_valid + " (" + ((float)count_valid/count_total * 100) + "%)\n" +
			"  Invalid names: " + count_invalid + " (" + ((float)count_invalid/count_total * 100) + "%)\n"
		);
		
		try {
			writer.flush();
			writer.close();
		} catch(IOException e) {
			System.err.println("Could not write output: " + e);
		}
		*/
	}
}
