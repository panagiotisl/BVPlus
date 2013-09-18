package gr.di.uoa.a8.sivac;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.collections.BidiMap;

import com.google.common.io.Files;

import gr.di.uoa.a8.sivac.utils.CalculateFrequencies;
import gr.di.uoa.a8.sivac.utils.SiVaCUtils;
import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;

public class SiVaCGraph extends ImmutableGraph {

	private static final String SiVaC_EXTENSION = "a8";
	private ImmutableGraph ig;
	private int D;
	private int bits;
	private int size = 0; /* number of nodes in the graph */
	private int edges;
	private byte[] diagonal /* the diagonal as bytes */;
	private byte[] compressedDiagonal;
	private BidiMap map;
	private File tempD /*
						 * file descriptor for a temp file with the arc list for
						 * the diagonal part
						 */;
	private File tempNoD /*
						 * file descriptor for a temp file with the arc list for
						 * the non diagonal part
						 */;

	// private HashSet<Integer> nodes;

	public SiVaCGraph(File input, int d, int bits, String basename) throws IOException {
		this.D = d;
		this.bits = bits;
		// this.nodes = new HashSet<Integer>();
		createTempFiles(new FileInputStream(input));
		this.diagonal = createDiagonal(this.tempD, this.D, this.size);
		this.map = CalculateFrequencies.calculateFrequencies(this.diagonal, this.size, this.D, this.bits);
		this.compressedDiagonal = createCompressedDiagonal(input, this.tempNoD, this.D, this.size, this.bits, this.map, this.diagonal);
		this.storeCompressedDiagonal(basename);
		this.ig = ArcListASCIIGraph.loadOnce(new FileInputStream(tempNoD));
		this.storeNonDiagonal(basename);
		this.ig = BVGraph.load(basename);
	}

	private static byte[] createCompressedDiagonal(File input, File tempNoD, int D, int size, int bits, BidiMap map, byte[] array) {
		byte[] compressedDiagonal = new byte[(size * bits) / 8 + (((size * bits) % 8 == 0) ? 0 : 1)];
		BufferedReader br;
		BufferedWriter bwNoD;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
			bwNoD = new BufferedWriter(new FileWriter(tempNoD.getAbsoluteFile()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		for (int i = 0; i < size; i++) {
			String number = "";
			for (int j = i - D; j < i + D + 1; j++) {
				try {
					int no = SiVaCGraph.getSerialization(i, j, size, D);
					if (SiVaCGraph.isSet(array[no / 8], no % 8)) {
						number += "1";
					} else {
						number += "0";
					}
				} catch (Exception e) {
					number += "0";
				}
			}
			if (!map.containsKey(number)) {
				if (number.contains("1")) {
					putEdgesInNonDiagonalFile(i, number, tempNoD, br, D, bwNoD);
				}
			} else {
				compressedDiagonal = putCompressedInArray(i, (String)map.get(number), compressedDiagonal);
			}
		}
		try {
			// write the rest of the non diagonal edges
			writeNonDiagonal(-1, br, D, bwNoD);
			br.close();
			bwNoD.flush();
			bwNoD.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return compressedDiagonal;
	}

	private static void putEdgesInNonDiagonalFile(int node, String number, File file, BufferedReader br, int D, BufferedWriter bwNoD) {
		char[] chars = number.toCharArray();
		try {
			int bits = chars.length;
			writeNonDiagonal(node, br, D, bwNoD);
			for (int i = 0; i < chars.length; i++) {
				if (chars[i] == '1') {
					bwNoD.write(node + " " + (node + i - bits / 2) + "\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeNonDiagonal(int node, BufferedReader br, int D, BufferedWriter bwNoD) throws IOException {
		String line;
		while ((line = br.readLine()) != null) {
			String[] temp = line.split("\\s+");
			int a = Integer.parseInt(temp[0]);
			int b = Integer.parseInt(temp[1]);
			if (!SiVaCUtils.isDiagonal(a, b, D)) {
				bwNoD.write(line + '\n');
			}
			if (a == node)
				break;
		}
	}

	private static byte[] putCompressedInArray(int node, String string, byte[] compressedDiagonal) {
		char[] chars = string.toCharArray();
		int pos = node * chars.length;
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '1')
			{
				compressedDiagonal[(pos / 8)] = set_bit(compressedDiagonal[(pos / 8)], pos % 8);
			}
			pos++;
		}
		return compressedDiagonal;
	}

	/**
	 * Function that reads the arc list file and splits into two temp files, one
	 * for the diagonal and one for the non diagonal part and creates a list of
	 * nodes
	 * */
	private boolean createTempFiles(InputStream is) throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		tempD = File.createTempFile("SiVaC-D-", ".a8");
		tempNoD = File.createTempFile("SiVaC-NoD-", ".a8");
		tempD.deleteOnExit();
		tempNoD.deleteOnExit();
		BufferedWriter bwD = new BufferedWriter(new FileWriter(tempD.getAbsoluteFile()));
		String line;
		while ((line = br.readLine()) != null) {
			String[] temp = line.split("\\s+");
			int a = Integer.parseInt(temp[0]);
			int b = Integer.parseInt(temp[1]);
			// size of the graph (nodes size) is equal to the largest + 1
			if (a >= this.size)
				this.size = a + 1;
			if (b >= this.size)
				this.size = b + 1;
			// write to one of the temp files
			if (SiVaCUtils.isDiagonal(a, b, D)) {
				// in the diagonal
				bwD.write(line + '\n');
				// } else {
				// outside the diagonal
				// bwNoD.write(line + '\n');
			}
		}
		br.close();
		bwD.close();
		return true;
	}

	/** get position in file from node pair */
	public static int getSerialization(int a, int b, int size, int D) {
		// check if input is valid
		if ((a > b + D || a < b - D) || (a < 0 || b < 0) || a >= size || b >= size)
			throw new IllegalArgumentException("not a valid node pair: (" + a + ", " + b + ")");
		// calculate position
		// TODO what about social?
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

	public static SiVaCGraph createAndLoad(File file, int d, int nb, String basename) {
		SiVaCGraph sg;
		try {
			sg = new SiVaCGraph(file, d, nb, basename);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return sg;
	}

	@Override
	public ImmutableGraph copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int numNodes() {
		return this.size;
	}

	public int numEdges() {
		return this.edges;
	}

	@Override
	public int outdegree(int arg0) {
		return ig.outdegree(arg0);
	}

	@Override
	public boolean randomAccess() {
		// TODO Auto-generated method stub
		return false;
	}

	private static byte[] createDiagonal(File tempD, int D, int size) {
		// store diagonal part
		int largest = getSerialization(size - 1, size - 1, size, D);
		byte[] diagonal = new byte[largest / 8 + (largest % 8 != 0 ? 1 : 0)];
		String line;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tempD)));
			while ((line = br.readLine()) != null) {
				String[] temp = line.split("\\s+");
				int a = Integer.parseInt(temp[0]);
				int b = Integer.parseInt(temp[1]);
				int no = getSerialization(a, b, size, D);
				diagonal[no / 8] = set_bit(diagonal[no / 8], no % 8);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return diagonal;
	}

	public boolean storeDiagonal(String basename) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(basename + "." + SiVaC_EXTENSION);
			fos.write(this.diagonal);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean storeCompressedDiagonal(String basename) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(basename + "." + SiVaC_EXTENSION);
			fos.write(this.compressedDiagonal);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean storeNonDiagonal(String basename) {
		// store non diagonal part as BVGraph
		try {
			ImmutableGraph.store(BVGraph.class, this.ig, basename);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public byte[] loadDiagonal(File file) throws IOException {
		return Files.toByteArray(file);
	}

	public boolean isSuccessor(int a, int b) {
		if (SiVaCUtils.isDiagonal(a, b, D)) {
			if(checkCompressedDiagonal(a, b , D, bits, compressedDiagonal))
			{
				return true;
			}
		}
		int[] temp = this.ig.successorArray(a);
		for (int suc : temp) {
			if (suc == b)
				return true;
		}
		return false;
	}

	private boolean checkCompressedDiagonal(int a, int b, int D, int bits, byte[] compressedDiagonal) {
		char[] chars = new char[bits];
		int pos = a * bits;
		for(int i=0;i<bits;i++)
		{
			chars[i] = (isSet(compressedDiagonal[pos/8], pos%8)) ? '1' : '0';
			pos++;
		}
//		System.out.println(new String(chars)+" "+(String)map.getKey(new String(chars))+" "+b+" "+a+" "+D);
		return ((String)map.getKey(new String(chars))).toCharArray()[b-a+D]=='1';
	}

	public LazyIntIterator getSuccessors(int a) {
		return this.ig.successors(a);
	}

	private void checkAllEdges(FileInputStream fileInputStream) throws NumberFormatException, IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));
		String line;
		while ((line = br.readLine()) != null) {
			String[] temp = line.split("\\s+");
			int a = Integer.parseInt(temp[0]);
			int b = Integer.parseInt(temp[1]);
			if (!this.isSuccessor(a, b))
				throw new RuntimeException("Edge not found " + a + " " + b);
		}
		br.close();
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		SiVaCGraph a = SiVaCGraph.createAndLoad(new File("/var/www/graphs/cnr-2000/cnr-2000-zero.txt"), 3, 3, "test");
		a.checkAllEdges(new FileInputStream(new File("/var/www/graphs/cnr-2000/cnr-2000-zero.txt")));
		// a.getSuccessors(5);
		System.out.println(a.getSuccessors(318).nextInt());
	}

}
