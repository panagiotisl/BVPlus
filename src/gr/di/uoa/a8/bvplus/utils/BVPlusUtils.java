package gr.di.uoa.a8.bvplus.utils;

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

public class BVPlusUtils {

	/**
	 * get position in file from node pair
	 * 
	 * @param a
	 *            first node
	 * @param b
	 *            second node
	 * @param size
	 *            graph size (number of nodes)
	 * @param k
	 *            diagonal parameter
	 * @return position in the diagonal
	 * */
	public static int getSerialization(int a, int b, int size, int k) {
		// check if input is valid
		if ((a > b + k || a < b - k) || (a < 0 || b < 0) || a >= size || b >= size)
			throw new IllegalArgumentException("not a valid node pair: (" + a + ", " + b + ")");
		// calculate position
		int no = a * (2 * k + 1) + b + k - a;
		int temp = k;
		// remove missing from beginning
		for (int i = 0; i < k; i++) {
			if (a >= i) {
				no -= temp;
				temp--;
			}
		}
		temp = 1;
		// remove missing from end
		for (int i = size + 1 - k; i < size; i++) {
			if (a >= i) {
				no -= temp;
				temp++;
			}
		}
		return no;
	}

	/**
	 * tests if bit is set in a byte
	 * 
	 * @param my_byte
	 *            the byte to be tested
	 * @param pos
	 *            the position in the byte to be tested
	 * @returns true or false depending on the bit being set
	 * 
	 * */
	public static boolean isSet(byte my_byte, int pos) {
		if (pos > 7 || pos < 0)
			throw new IllegalArgumentException("not a valid bit position: " + pos);
		return (my_byte & (1 << pos)) != 0;
	}

	/**
	 * tests if bit is set in a byte
	 * 
	 * @param my_byte
	 *            the byte to be updated
	 * @param pos
	 *            the position in the byte to be set
	 * @returns the updated byte
	 * 
	 * */
	public static byte set_bit(byte my_byte, int pos) {
		if (pos > 7 || pos < 0)
			throw new IllegalArgumentException("not a valid bit position: " + pos);
		return (byte) (my_byte | (1 << pos));
	}

	/**
	 * tests if bit is set in a byte
	 * 
	 * @param my_byte
	 *            the byte to be updated
	 * @param pos
	 *            the position in the byte to be unset
	 * @returns the updated byte
	 * 
	 * */
	public static byte unset_bit(byte my_byte, int pos) {
		if (pos > 7 || pos < 0)
			throw new IllegalArgumentException("not a valid bit position: " + pos);
		return (byte) (my_byte & ~(1 << pos));
	}

	/**
	 * Creates a {@link BidiMap} to hold the chosen representations to be used for the compressed diagonal
	 * 
	 * @param array uncompressed diagonal
	 * @param size total number of nodes
	 * @param k diagonal size parameter
	 * @param b compressed diagonal size parameter
	 * */
	public static BidiMap calculateFrequencies(byte[] array, int size, int k, int b) throws NumberFormatException, IOException {
		Map<String, Integer> freqsX = new HashMap<String, Integer>();
		ValueComparator bvc = new ValueComparator(freqsX);
		TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);
		// for all nodes
		for (int i = 0; i < size; i++) {
			String number = "";
			int count = 0;
			// for all diagonal array cell
			for (int j = i - k; j < i + k + 1; j++) {
				// fix string representation
				try {
					int no = BVPlusUtils.getSerialization(i, j, size, k);
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
			// and add it to the HashMap
			if (!freqsX.containsKey(number)) {
				freqsX.put(number, count);
			}
			else {
				freqsX.put(number, freqsX.get(number) + count);
			}

		}
		// sort the map
		sorted_map.putAll(freqsX);
		Object[] keys = sorted_map.keySet().toArray();
		// create a bidirectional hash map to hold the selected representations
		BidiMap map = new DualHashBidiMap();
		char[] chars = new char[b];
		Arrays.fill(chars, '0');
		char[] morechars = new char[(2 * k + 1)];
		Arrays.fill(morechars, '0');
		map.put(new String(morechars), new String(chars));
		for (int i = 0; i < ((int) Math.pow(2, b) - 1) && i < keys.length; i++) {
			try {
				map.put((String) keys[i], StringUtils.leftPad(Integer.toBinaryString(i + 1), b, "0"));
			} catch (ArrayIndexOutOfBoundsException e) {
				System.err.println("Not enough values with such a small diagonal " + k + " " + b);
			}
		}
		return map;
	}

	/**
	 * A {@link Comparator} implementation for sorting frequencies of the arrays
	 * in the diagonal
	 */
	private static class ValueComparator implements Comparator<String> {

		Map<String, Integer> base;

		public ValueComparator(Map<String, Integer> base) {
			this.base = base;
		}

		@Override
		public int compare(String first, String second) {
			if (base.get(first) >= base.get(second)) {
				return -1;
			} else {
				return 1;
			} // returning 0 would merge keys
		}
	}

	/**
	 * Measures the percentage of edges residing in the diagonal of a given size
	 * 
	 * @param filename
	 *            the input graph file
	 * @param k
	 *            diagonal size parameter
	 * @return the percentage of edges residing in the diagonal
	 * 
	 * */
	public static float percentageInDiagonal(String filename, int k) {
		String line;
		int total = 0, diagonal = 0;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename))));
			while ((line = br.readLine()) != null) {
				String[] temp = line.split("\\s+");
				int a = Integer.parseInt(temp[0]);
				int b = Integer.parseInt(temp[1]);
				if (a >= b - k && a <= b + k) {
					// in the diagonal
					diagonal++;
				}
				total++;
			}
			br.close();
			System.out.println(k + ": In Diagonal: " + diagonal + " Total: " + total + " Percentage: " + (float) 100 * diagonal / total);
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		return (float) diagonal / total;
	}

	/**
	 * Measures the bits per edge for the diagonal part for a diagonal of a
	 * specific size (utility function not used by BVPlus)
	 * 
	 * @param filename
	 *            the input graph file
	 * @param k
	 *            diagonal size parameter
	 * @param social
	 *            boolean indicating if graph permits self links
	 * @return the bits per edge for the diagonal part
	 * 
	 * */
	public static float bitsPerEdge(String filename, int k, boolean social) {
		String line;
		int diagonal = 0, max = -1, size;
		HashSet<Integer> nodes = new HashSet<Integer>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename))));
			while ((line = br.readLine()) != null) {
				String[] temp = line.split("\\s+");
				int a = Integer.parseInt(temp[0]);
				int b = Integer.parseInt(temp[1]);
				nodes.add(a);
				nodes.add(b);
				if (a + 1 > max)
					max = a + 1;
				if (b + 1 > max)
					max = b + 1;
				if (a >= b - k && a <= b + k) {
					diagonal++;
				}
			}
			br.close();
			size = max;
			System.out.println(k + ": bits/edge: " + (float) (social ? k * 2 * size : (k * 2 + 1) * size) / diagonal + "\tN: " + size);
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		return (float) (social ? k * 2 * size : (k * 2 + 1) * size) / diagonal;
	}

	/**
	 * Checks if an edge resides in the diagonal
	 * 
	 * @param a
	 *            first node
	 * @param b
	 *            second node
	 * @param k
	 *            diagonal size parameter
	 * @returns true or false depending on whether the edge resides in the
	 *          diagonal
	 * */
	public static boolean isDiagonal(int a, int b, int k) {
		if (a >= b - k && a <= b + k)
			return true;
		return false;
	}

	/**
	 * Counts the number of edges that the input strings have in common
	 * 
	 * @param a
	 *            first string
	 * @param b
	 *            second string
	 * @return number of common edges or -1 if a is not a subset of b
	 * */
	public static int subset(String a, String b) {
		char[] first = a.toCharArray();
		char[] second = b.toCharArray();

		int minLength = (int) Math.min(first.length, second.length);
		int counter = 0;

		for (int i = 0; i < minLength; i++) {
			if (first[i] != second[i] && first[i] == '1')
				return -1;
			if (first[i] == second[i] && first[i] == '1')
				counter++;
		}
		return counter;
	}

}
