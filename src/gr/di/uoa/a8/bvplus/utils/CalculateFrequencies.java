package gr.di.uoa.a8.bvplus.utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

public class CalculateFrequencies {


	private static void calculateFrequencies(File file, int D) throws NumberFormatException, IOException {

		Map<String, Integer> freqs = new HashMap<String, Integer>();
		Map<String, Integer> freqsX = new HashMap<String, Integer>();
		ValueComparator bvc = new ValueComparator(freqsX);
		TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String line;
		int max = -1;
		while ((line = br.readLine()) != null) {
			String[] temp = line.split("\\s+");
			int a = Integer.parseInt(temp[0]);
			int b = Integer.parseInt(temp[1]);
			if (a >= max)
				max = a + 1;
			if (b >= max)
				max = b + 1;
		}
		int size = max;
		System.out.println("Size: " + size);
		br.close();
		br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		int largest = BVPlusUtils.getSerialization(size - 1, size - 1, size, D);
		byte[] array = new byte[largest / 8 + (largest % 8 != 0 ? 1 : 0)];
		while ((line = br.readLine()) != null) {
			String[] temp = line.split("\\s+");
			int a = Integer.parseInt(temp[0]);
			int b = Integer.parseInt(temp[1]);
			if (BVPlusUtils.isDiagonal(a, b, D)) {
				int no = BVPlusUtils.getSerialization(a, b, size, D);
				try {
					array[no / 8] = BVPlusUtils.set_bit(array[no / 8], no % 8);
				} catch (Exception e) {
					System.err.println(no / 8 + " " + array.length);
				}
			}

		}
		br.close();
		for (int i = 0; i < size; i++) {
			String number = "";
			int count = 0;
			for (int j = i - D; j < i + D + 1; j++) {
				try {
					int no = BVPlusUtils.getSerialization(i, j, size, D);
					if (BVPlusUtils.isSet(array[no / 8], no % 8)) {
						number += "1";
						count++;
					} else {
						number += "0";
					}

				} catch (Exception e) {
					number += "0";
				}

			}
			if (!freqs.containsKey(number)) {
				freqs.put(number, 1);
				freqsX.put(number, count);
			}

			else {
				freqs.put(number, freqs.get(number) + 1);
				freqsX.put(number, freqsX.get(number) + count);
			}

		}

		System.out.println("Possible values of diagonal: " + freqs.size() + " and size of diagonal: " + D);

		// code for adaptive dictionary
		sorted_map.putAll(freqsX);
		System.out.println(D + ": " + sorted_map);
		
		Collection<Integer> map_values = sorted_map.values();
		String powerLaw = "{";
		int count=1;
		for(int value : map_values)
		{
			powerLaw += "{"+count+","+value+"}, ";
			count++;
		}
		powerLaw = powerLaw.substring(0, powerLaw.length()-2)+"}";
		System.out.println("PowerLaw: "+powerLaw);
		
		Set<Entry<String, Integer>> set = sorted_map.entrySet();
		String powerLaw2 = "{";
		for(Entry<String, Integer> entry : set)
			powerLaw2 += "{"+Integer.parseInt(entry.getKey(), 2)+","+entry.getValue()+"}, ";
		powerLaw2 = powerLaw2.substring(0, powerLaw2.length()-2)+"}";
		System.out.println("PowerLaw: "+powerLaw2);
		
		
		Object[] keys = sorted_map.keySet().toArray();
		for (int bits = 1; bits <= D; bits++) {
			int pv = freqs.size();
			int new_edges = 0, captured_edges = 0, total_edges = 0;
			for (int i = ((int) Math.pow(2, bits) - 1); i < pv - 1; i++) {
				int temp = -1, temp2 = 0;
//				String key = "";
				for (int j = 0; j < ((int) Math.pow(2, bits) - 1); j++) {
					temp2 = BVPlusUtils.subset((String) keys[j], (String) keys[i]);
					if (temp2 > temp) {
						temp = temp2;
//						key = (String) keys[j];
					}

				}
				// System.out.println((String)keys[i]+" " + key + " " +
				// temp+" "+freqsX.get((String)keys[i])+" "+freqsX.get((String)keys[i])*SiVaCUtils.fracture((String)keys[i],
				// temp));
				captured_edges += freqsX.get((String) keys[i]) * fracture((String) keys[i], temp);
			}
			new_edges = captured_edges;
			for (int i = 0; i < ((int) Math.pow(2, bits) - 1); i++)
				captured_edges += freqsX.get((String) keys[i]);
			for (int i = 0; i < pv; i++)
				total_edges += freqsX.get((String) keys[i]);
			System.out.println(captured_edges + " " + total_edges + " " + (double) captured_edges / total_edges + " " + (double) new_edges / total_edges);
			System.out.println("bits/edge: " + bits * size / (double) captured_edges + " with " + bits + " bits.");

		}

	}

	public static String padLeft(String s, int n) {
		return StringUtils.leftPad(s, n, "0");
	    //return String.format("%1$" + n + "s", s);  
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
	
	public static double fracture(String string, int b) {
		if(b<0)
			return 0;
		char[] array  = string.toCharArray();
		double counter = 0;
		for(int i = 0; i < array.length; i++)
		        if (array[i] == '1')
		        	counter++;
		return b/counter;
	}


	public static void main(String[] args) throws FileNotFoundException, IOException {

		String filename = args[0];
		int iterations = Integer.parseInt(args[1]);
		for (int i = 1; i <= iterations; i++)
			calculateFrequencies(new File(filename), i);
	}

}
