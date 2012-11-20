TAXREF
Taxonomic Referencing made easy

1. INTRODUCTION

TaxRef is designed to meet three needs:
    1.  Taxonomic validation: given a list of potentially correct
        taxon names, identify the currently accepted name corresponding
        to that taxon name, or record the name as being invalid.
        e.g. given "Fellis tigris", validate the name as a mistype of
        "Felis tigris", which is a junior synonym of "Panthera tigris".
    2.  Taxonomic referencing: given a valid taxon name, fill in
        higher taxonomy corresponding to that name. Note that this
        will need to be against a particular taxonomic checklist.
        e.g. given "Panthera tigris", identify the family ("Felidae"),
        order ("Carnivora"), class ("Mammalia"), phylum ("Chordata"),
        kingdom ("Animalia") and optionally domain ("Eukarya").
    3.  Checklist comparison: given multiple taxonomic checklists,
        identify differences between them and provide summaries as
        to those differences.
        e.g. one checklist might have "80% (n=4) of the names in
        genus 'Panthera'" as the other checklist.

2. HOW TO USE IT

For now, TaxRef is in a very early pre-release. Please use the following
steps to try TaxRef off.

    1.  Check if you have Java installed: http://www.java.com/en/download/installed.jsp
          Mac OS X 10.6 and below: Java should be pre-installed.
          Mac OS X 10.7 (Lion) and above: Java is not pre-installed.
          Windows: depends on your computer manufacturer.
          Linux: probably not, but should be available from your package manager.

    2.  If not, you should be able to install a version of Java for your
        operating system from: http://www.java.com/en/download/manual.jsp
    3.  Choose a CSV dataset to validate. If you can't come up with one, 
        download the DwC-A file from 
        http://ipt.pensoft.net/ipt/resource.do?r=neembucu and unzip it. Find the 
        "taxon.txt" file in the unzipped directory.

	4.  TaxRef doesn't yet support files without a header line; however, it does 
        support tab-delimited files! If you're using your own checklist, make 
        sure that it's either tab-delimited or CSV with a header as the first 
        line of the line.
    5.  Download the ITIS DwC taxa.txt file from 
        http://gaurav.github.com/itis-dwca/ (right-click on 
        http://gaurav.github.com/itis-dwca/latest/taxa.txt and do a Save-As).

    6.  In TaxRef, select menu item "File" -> "Open tab-delimited" and choose 
        the taxon.txt.
            - TaxRef automatically extracts a canonicalName (monomial, binomial 
              or trinomial) from the scientificName, unless a "canonicalName"
              column is already defined in the input file.
            - In some cases (e.g. id #1776), TaxRef cannot extract a canonical 
              name (from "H. leucophlaeos (Mart. ex DC.) Mattos"). However, this 
              is a fully editable work environment -- you can enter the correct 
              name into the canonicalName column if you like.

    7.  Select menu item "Match" -> "Match against CSV", and choose the ITIS 
        CSV file (I'm calling these file "DarwinCSV" files for convenience)
            - The "Match" -> "Match against ITIS" menu option will automatically 
              download the ITIS DwC file you downloaded above and run the match.
            - All matched names -- scientific names and canonical names -- will 
              light up in red (not matched) or green (matched).

    8.  If you want, you  can save this file as a CSV -- the red/green match 
        status isn't exported yet, but the canonical name is. I hope to have it 
        set up so that it adds the taxonId for matched names, which can then be 
        exported to indicate that the name was successfully matched.

3. COPYRIGHT

Copyright (C) 2012 Gaurav Vaidya.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
