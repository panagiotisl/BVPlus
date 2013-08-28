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
			System.out.println(D+": In Diagonal: "+diagonal+" Total: "+total+" Percentage: "+(float)100*diagonal/total);	
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
		int diagonal = 0, max = -1, size;
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
				if(a+1>max)
					max = a+1;
				if(b+1>max)
					max = b+1;
				if(a>=b-D && a<=b+D)
				{
					// in the diagonal
					diagonal++;
				}
			}
			br.close();
			size = max;
			System.out.println(D + ": bits/edge: "+(float) (social ? D*2*size : (D*2+1)*size)/diagonal + "\tN: " + size);	
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return -1;
		}
		return (float) (social ? D*2*size : (D*2+1)*size)/diagonal;
	}
	
	
	public static boolean isDiagonal(int a, int b, int D)
	{
		if(a>=b-D && a<=b+D)
			return true;
		return false;
	}
	
	public static int subset(String a, String b)
	{
		char[] first  = a.toCharArray();
		char[] second = b.toCharArray();

		int minLength = (int)Math.min(first.length, second.length);
		int counter = 0;

		for(int i = 0; i < minLength; i++)
		{
		        if (first[i] != second[i] && first[i] == '1')
		        	return -1;
		        if (first[i] == second[i] && first[i] == '1')
		        	counter++;
		}
		return counter;
	}

	public static double fracture(String a, int b) {
		if(b<0)
			return 0;
		char[] first  = a.toCharArray();
		double counter = 0;
		for(int i = 0; i < first.length; i++)
		        if (first[i] == '1')
		        	counter++;
		return b/counter;
	}
	
}
