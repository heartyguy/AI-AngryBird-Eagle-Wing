/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2015, Team DataLab Birds: 
 ** Karel Rymes, Radim Spetlik, Tomas Borovicka
 ** Team HeartyTian: Tian Jian Wang
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************/

package hearty.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class is used to read parameters from a file.
 */
public class HeartyReader {
	int i = 0;
	int k = 0;

	private String _filename = "";

	public String text = "";

	public String[] textArray = null;

	public String splitRegEx = "[,\\s]{1,2}";

	public double[] doubleArray = null;

	/**
	 * constructor with the path to the file
	 */
	public HeartyReader(String filename) {
		_filename = filename;

		readIt();
	}

	/**
	 * reads the parameters
	 */
	public void readIt() {
		// reading
		try {
			InputStream ips = new FileInputStream(_filename);
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String line;

			while ((line = br.readLine()) != null) {
				text += line;
			}

			br.close();

		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	public double[] splitToDoubleArray() {
		// split(String Delimiter)
		textArray = text.split(splitRegEx);

		// System.out.println("Array :" + textArray.length);

		doubleArray = new double[textArray.length];

		for (int i = 0; i < textArray.length; i++) {
			// System.out.println("array " + i + ": " +
			// Double.parseDouble(textArray[i]));
			doubleArray[i] = Double.parseDouble(textArray[i]);
		}

		return doubleArray;
	}
}
