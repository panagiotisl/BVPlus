package gr.di.uoa.a8.sivac.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

public class SiVaCUtils {

	public static float percentageInDiagonal(String filename, int D)
	{
		String line;
		int total = 0, diagonal = 0;
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename))));
			while ((line = br.readLine()) != null) {
				String[] temp = line.split("\\s+");
				int a = Integer.parseInt(temp[0]);
				int b = Integer.parseInt(temp[1]);
				if(a>=b-D && a<=b+D)
				{
					// in the diagonal
					diagonal++;
				}
				total++;
			}
			br.close();
			System.out.println("In Diagonal: "+diagonal+" Total: "+total+" Percentage: "+(float)diagonal/total);	
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return -1;
		}
		return (float)diagonal/total;
	}
	
	public static float bitsPerEdge(String filename, int D, boolean social)
	{
		String line;
		int diagonal = 0;
		HashSet<Integer> nodes = new HashSet<Integer>();
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename))));
			while ((line = br.readLine()) != null) {
				String[] temp = line.split("\\s+");
				int a = Integer.parseInt(temp[0]);
				int b = Integer.parseInt(temp[1]);
				nodes.add(a);
				nodes.add(b);
				if(a>=b-D && a<=b+D)
				{
					// in the diagonal
					diagonal++;
				}
			}
			br.close();
			System.out.println("bits/edge: "+(float) (social ? D*2*nodes.size() : (D*2+1)*nodes.size())/diagonal);	
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return -1;
		}
		return (float) (social ? D*2*nodes.size() : (D*2+1)*nodes.size())/diagonal;
	}
	
	
	public static boolean isDiagonal(int a, int b, int D)
	{
		if(a>=b-D && a<=b+D)
			return true;
		return false;
	}
	
}
