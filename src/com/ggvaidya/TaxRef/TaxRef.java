
/*
 *
 *  TaxRef
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
package com.ggvaidya.TaxRef;

import com.ggvaidya.TaxRef.Cmdline.CmdController;

/**
 * The main class for TaxRef.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class TaxRef {
	
	/**
	 * Entrypoint. But really just bungs it on to
	 * {@link CmdController.handle}.
	 * 
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		CmdController.handle(args);
	}

	/**
	 * What are we calling this program this week. 
	 * 
	 * @return A name to use for this program.
	 */
	public static String getName() {
		return "TaxRef";
	}
	
	/**
	 * What is the current version number.
	 * 
	 * @return The current version number, generally in an A.B.C format, but
	 * possibly in A.B.C-D.
	 */
	public static String getVersion() {
		return "0.0.2";
	}
	
	/**
	 * A one-sentence description of this program.
	 * 
	 * @return A one-sentence description.
	 */
	public static String getDescription() {
		return "A basic taxon name validator";
	}
	
	/**
	 * The copyright notice that accompanies this program.
	 * 
	 * @return The copyright notice that accompanies this program.
	 */
	public static String getCopyright() {
		return "Copyright (C) 2012 Gaurav Vaidya\n\n" +
			"TaxRef is free software: you can redistribute it and/or modify\n" +
			"it under the terms of the GNU General Public License as published by\n" +
			"the Free Software Foundation, either version 3 of the License, or\n" +
			"(at your option) any later version.\n\n" + 
			"TaxRef is distributed in the hope that it will be useful,\n" +
			"but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
			"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
			"GNU General Public License for more details.\n\n" +
			"You should have received a copy of the GNU General Public License\n" + 
			"along with TaxRef.  If not, see <http://www.gnu.org/licenses/>."
		;
	}
	
}
