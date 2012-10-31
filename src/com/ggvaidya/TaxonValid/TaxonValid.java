
/*
 *
 *  TaxonValid
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
package com.ggvaidya.TaxonValid;

import com.ggvaidya.TaxonValid.Cmdline.*;

/**
 * The main class for TaxonValid.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class TaxonValid {
	
	public static void main(String[] args) {
		CmdController.main(args);
	}

	public static String getName() {
		return "TaxonValid";
	}
	
	public static String getVersion() {
		return "0.0.1";
	}
	
	public static String getDescription() {
		return "A basic taxon validator";
	}
	
	public static String getCopyright() {
		return "Copyright (C) 2012 Gaurav Vaidya\n\n" +
			"TaxonValid is free software: you can redistribute it and/or modify\n" +
			"it under the terms of the GNU General Public License as published by\n" +
			"the Free Software Foundation, either version 3 of the License, or\n" +
			"(at your option) any later version.\n\n" + 
			"TaxonValid is distributed in the hope that it will be useful,\n" +
			"but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
			"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
			"GNU General Public License for more details.\n\n" +
			"You should have received a copy of the GNU General Public License\n" + 
			"along with TaxonValid.  If not, see <http://www.gnu.org/licenses/>."
		;
	}
	
}
