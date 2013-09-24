package gr.di.uoa.a8.sivac.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.commons.lang.StringUtils;

public class SiVaCUtils {

	
	/** get position in file from node pair */
	public static int getSerialization(int a, int b, int size, int D) {
		// check if input is valid
		if ((a > b + D || a < b - D) || (a < 0 || b < 0) || a >= size || b >= size)
			throw new IllegalArgumentException("not a valid node pair: (" + a + ", " + b + ")");
		// calculate position
		int no = a * (2 * D + 1) + b + D - a;
		int temp = D;
		// remove missing from beginning
		for (int i = 0; i < D; i++) {
			if (a >= i) {
				no -= temp;
				temp--;
			}
		}
		temp = 1;
		// remove missing from end
		for (int i = size + 1 - D; i < size; i++) {
			if (a >= i) {
				no -= temp;
				temp++;
			}
		}
		return no;
	}

	// tests if bit is set in a byte
	public static boolean isSet(byte my_byte, int pos) {
		if (pos > 7 || pos < 0)
			throw new IllegalArgumentException("not a valid bit position: " + pos);
		return (my_byte & (1 << pos)) != 0;
	}

	// set a bit in a byte
	public static byte set_bit(byte my_byte, int pos) {
		if (pos > 7 || pos < 0)
			throw new IllegalArgumentException("not a valid bit position: " + pos);
		return (byte) (my_byte | (1 << pos));
	}

	// unset a bit in a byte
	public static byte unset_bit(byte my_byte, int pos) {
		if (pos > 7 || pos < 0)
			throw new IllegalArgumentException("not a valid bit position: " + pos);
		return (byte) (my_byte & ~(1 << pos));
	}

	public static BidiMap calculateFrequencies(byte[] array, int size, int D, int bits) throws NumberFormatException, IOException {
		Map<String, Integer> freqsX = new HashMap<String, Integer>();
		ValueComparator bvc = new ValueComparator(freqsX);
		TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);
		for (int i = 0; i < size; i++) {
			String number = "";
			int count = 0;
			for (int j = i - D; j < i + D + 1; j++) {
				try {
					int no = SiVaCUtils.getSerialization(i, j, size, D);
					if (SiVaCUtils.isSet(array[no / 8], no % 8)) {
						number += "1";
						count++;
					} else {
						number += "0";
					}

				} catch (Exception e) {
					number += "0";
				}

			}
			if (!freqsX.containsKey(number)) {
				freqsX.put(number, count);
			}

			else {
				freqsX.put(number, freqsX.get(number) + count);
			}

		}
		sorted_map.putAll(freqsX);
		Object[] keys = sorted_map.keySet().toArray();
		BidiMap map = new DualHashBidiMap();
		char[] chars = new char[bits];
		Arrays.fill(chars, '0');
		char[] morechars = new char[(2*D+1)];
		Arrays.fill(morechars, '0');
		map.put(new String(morechars), new String(chars));
		for(int i=0;i<((int) Math.pow(2, bits) - 1)&&i<keys.length; i++)
		{
			try
			{
				map.put((String)keys[i], padLeft(Integer.toBinaryString(i+1), bits));
			}catch(ArrayIndexOutOfBoundsException e)
			{
				System.err.println("Not enough values with such a small diagonal "+D+" "+bits);
			}
		}
		return map;
	}
	
	public static String padLeft(String s, int n) {
		return StringUtils.leftPad(s, n, "0");
	}
	
	private static class ValueComparator implements Comparator<String> {

		Map<String, Integer> base;

		public ValueComparator(Map<String, Integer> base) {
			this.base = base;
		}

		@Override
		public int compare(String a, String b) {
			if (base.get(a) >= base.get(b)) {
				return -1;
			} else {
				return 1;
			} // returning 0 would merge keys
		}
	}
	
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
