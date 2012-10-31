
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
package com.ggvaidya.TaxonValid.Cmdline;

import org.apache.commons.cli.*;

import com.ggvaidya.TaxonValid.*;

/**
 * The CmdLine Controller class handles command line instructions to
 * TaxonValid. Unlike TaxonDNA, TaxonValid should be as completely
 * command-line driven as possible.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class CmdController {
	public static void main(String[] args) {
		Options cmdLineOptions = new Options();
		setupOptions(cmdLineOptions);
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(cmdLineOptions, args);
		} catch (ParseException ex) {
			System.err.println("Could not parse command line: " + ex);
			System.exit(1);
			return;
		}
		
		if(cmd.hasOption("version")) {
			System.err.println(TaxonValid.getName() + "/" + TaxonValid.getVersion());
			System.err.println(TaxonValid.getDescription());
			System.err.println(TaxonValid.getCopyright());
			System.err.println();
			System.exit(0);
		}
	}

	private static void setupOptions(Options cmdLineOptions) {
		cmdLineOptions.addOption(
			"version", 
			false, 
			"Display version information for this software"
		);
	}
	
}
