package gr.di.uoa.a8.sivac.k2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String line;
		Set<Integer> boxes = new HashSet<Integer>();
		int size = 0;
		int box_dimensions = 2;
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/var/www/graphs/challenge/citation-PaperJSON_199star.graph.txt"))));
//			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/var/www/graphs/challenge/citation-csconf-mult15-PaperJSON.graph.txt"))));
			line = br.readLine();
			size = Integer.parseInt(line);
			while ((line = br.readLine()) != null) {
				String[] temp = line.split("\\s+");
				int a = Integer.parseInt(temp[0]);
				int b = Integer.parseInt(temp[1]);
				boxes.add((a/box_dimensions)*size+(b/box_dimensions));
			}
			br.close();
			System.out.println(boxes.size());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

	}

}
