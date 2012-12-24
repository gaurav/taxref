
/*
 *
 *  MatchInformationFrame
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

import com.ggvaidya.TaxRef.Model.*;

import java.awt.*;
import javax.swing.*;

/**
 * A MatchInformationFrame contains information about a particular match.
 * @author Gaurav Vaidya <gaurav@ggvaidya.com>
 */
public class MatchInformationPanel extends JPanel {
	private JTextField tf_name_to_match = new JTextField("            ");
	private JTextField tf_accepted_name = new JTextField("            ");
	private JTextField tf_taxonid = new JTextField("            ");
	
	public MatchInformationPanel() {
		super();
		
		initPanel();
	}
	
	private void initPanel() {
		RightLayout rl = new RightLayout(this);
		rl.add(new JLabel("Name to match: "), RightLayout.NONE);
		
		rl.add(tf_name_to_match, RightLayout.BESIDE | RightLayout.STRETCH_X | RightLayout.FILL_3);
		rl.add(new JButton("Search"), RightLayout.BESIDE);
		
		rl.add(new JLabel("Accepted name: "), RightLayout.NEXTLINE);
		rl.add(tf_accepted_name, RightLayout.BESIDE);
		rl.add(new JLabel("TaxonID"), RightLayout.BESIDE);
		rl.add(tf_taxonid, RightLayout.BESIDE);
		rl.add(new JButton("Look up"), RightLayout.BESIDE);
	}
	
	public void setMatchInformation(DarwinCSV csv, Name matchedName) {
		tf_name_to_match.setText(matchedName.toString());
	}
}
