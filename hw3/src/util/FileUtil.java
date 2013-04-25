package util;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class FileUtil {

	public static List<String> readFile(String filePathName) {
		List<String> lines = new ArrayList<String>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(filePathName));
			String line = br.readLine();
			while (line != null && !line.trim().equals("")) {
				lines.add(line.trim());
				line = br.readLine();
			}
			br.close();
		} catch (Exception e) {
		}
		return lines;
	}

	public static void writeToFile(String filename, StringBuffer buffer) {
		BufferedWriter bufferedWriter = null;

		try {
			// Construct the BufferedWriter object
			bufferedWriter = new BufferedWriter(new FileWriter(filename));

			// Start writing to the output stream
			bufferedWriter.write(buffer.toString());

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			// Close the BufferedWriter
			try {
				if (bufferedWriter != null) {
					bufferedWriter.flush();
					bufferedWriter.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

}
