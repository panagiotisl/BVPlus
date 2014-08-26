package gr.di.uoa.a8.bvplus.k2;

import gr.di.uoa.a8.bvplus.utils.BVPlusUtils;

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
//		int size = 326186;
		int size = 325557;
		int box_dimensions = 1;
		try
		{
//			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/home/panagiotis/graphs/cnr-2000/cnr-2000.txt"))));
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/home/panagiotis/matlab/cnr-2000-slashburn.txt"))));
//			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/var/www/graphs/challenge/citation-PaperJSON_199star.graph.txt"))));
//			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/var/www/graphs/challenge/citation-csconf-mult15-PaperJSON.graph.txt"))));
//			line = br.readLine();
//			size = Integer.parseInt(line);
			while ((line = br.readLine()) != null) {
				String[] temp = line.split("\\s+");
				int a = Integer.parseInt(temp[0]);
				int b = Integer.parseInt(temp[1]);
//				if(!BVPlusUtils.isDiagonal(a, b, 2))
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
