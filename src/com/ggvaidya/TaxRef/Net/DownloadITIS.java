
/*
 *
 *  DownloadITIS
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

package com.ggvaidya.TaxRef.Net;

import com.ggvaidya.TaxRef.UI.*;
import com.ggvaidya.TaxRef.Model.*;
import java.io.*;
import java.net.*;
import org.apache.commons.io.*;
import javax.swing.*;

/**
 * A quick class to download and decompress ITIS-DwCA.
 * 
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class DownloadITIS {
	public static final String url_itisDwCtab = "http://gaurav.github.com/itis-dwca/latest/taxa.txt";
	
	public static DarwinCSV getIt(JFrame mainFrame) {
		try {
			URL url = new URL(url_itisDwCtab);
			
			File tempFile = File.createTempFile("itis_dwctab", ".txt", null);
			FileUtils.copyURLToFile(url, tempFile, 10000, 100000);

			return new DarwinCSV(tempFile, DarwinCSV.FILE_CSV_DELIMITED);
		} catch (MalformedURLException ex) {
			MessageBox.messageBox(mainFrame, "Malformed URL", "Malformed URL (" + ex + "): " + url_itisDwCtab);
		} catch(IOException ex) {
			MessageBox.messageBox(mainFrame, "Unable to download ITIS-DwCA file", ex.getLocalizedMessage());
		}
		return null;
	}

}
