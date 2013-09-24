package gr.di.uoa.a8.sivac;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.collections.BidiMap;

import com.google.common.io.Files;

import gr.di.uoa.a8.sivac.utils.SiVaCUtils;
import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;

public class SiVaCGraph extends ImmutableGraph {

	/**
	 *  Extension for the file holding the compressed diagonal 
	 *  */
	private static final String SiVaC_EXTENSION = ".a8";
	
	/**
	 * The {@link ImmutableGraph} holding the {@link ArcListASCIIGraph} during
	 * the creation and the {@link BVGraph} from the moment the compression
	 * is ready, or after the graph is loaded
	 * */
	private ImmutableGraph ig;
	
	/**
	 * The input file with the arc list
	 * */
	private File input;
	
	/**
	 * The size of the diagonal
	 * */
	private int D;
	
	/**
	 * The number of bits for the compressed representation of the diagonal
	 * */
	private int bits;
	
	/**
	 * The number of nodes of the graph
	 * */
	private int nodes;
	
	/**
	 * The number of edges of the graph
	 * */
	private int edges;
	
	/** 
	 * The representation of the compressed diagonal of the graph as bytes
	 * */
	private byte[] compressedDiagonal;
	
	/** 
	 * The representation of the uncompressed diagonal of the graph as bytes
	 * Used only during the creation of the graph
	 * */
	private byte[] diagonal;

	/**
	 * The {@link BidiMap} holding the matching between compressed and
	 * uncompressed representations of the diagonal. Its size is equal
	 * to 2^bits - 1
	 * */
	private BidiMap map;
	
	/**
	 * The string holding the base name of the files stored in the file system
	 * */
	private String basename;
	
	/**
	 * One of the two files created during the compression phase. The file
	 * is deleted on exit and holds the arc list for the diagonal part
	 * of the graph
	 * */
	private File tempD;
	
	/**
	 * One of the two files created during the compression phase. The file
	 * is deleted on exit and holds the arc list for the non diagonal part
	 * of the graph
	 * */
	private File tempNoD;


	
	public SiVaCGraph(File input, int D, int bits, String basename){
		// initializing variables
		this.input = input;
		this.D = D;
		this.bits = bits;
		this.basename = basename;
		try
		{
			// create diagonal and non-diagonal file and fill the first
			this.createTempFiles();
			// fill the byte representation of the diagonal and remove the file
			this.createDiagonal();
			this.tempD.delete();
			// create the map with the compressed representations
			// fill the byte representation of the compressed diagonal and
			// release the memory of the byte representation of the diagonal
			this.map = SiVaCUtils.calculateFrequencies(this.diagonal, this.nodes, this.D, this.bits);
			this.createCompressedDiagonal();
			this.diagonal = null;
			// store the compressed diagonal 
			this.storeCompressedDiagonal();
			// load the non-diagonal part to an Immutable Graph
			// remove the file, store the graph as a BVGraph
			// and load it into memory
			this.ig = ArcListASCIIGraph.loadOnce(new FileInputStream(tempNoD));
			this.tempNoD.delete();
			this.storeNonDiagonal();
			this.ig = BVGraph.load(this.basename);
			
		} catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	
	
	public SiVaCGraph(int D, int bits, String basename) {
		this.input = null;
		this.D = D;  // TODO import
		this.bits = bits;  // TODO import
		this.map = null; // TODO import
		this.basename = basename;
		try {
			this.compressedDiagonal = this.loadDiagonal(new File(basename+SiVaC_EXTENSION));
			this.ig = BVGraph.load(this.basename);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.nodes = this.compressedDiagonal.length/bits;
		this.edges = 0; // TODO calculate
		
		}


	/**
	 * Uses an arc list file to create a {@link SiVaCGraph} instance
	 * and load it into memory
	 * 
	 * @param file The input arc list file
	 * @param D The desired size of the diagonal
	 * @param bits The desired number of bits of the compressed diagonal
	 * @param basename The base name for the stored graph files
	 * @return The newly created {@link SiVaCGraph} instance 
	 * */
	public static SiVaCGraph createAndLoad(File file, int D, int bits, String basename) {
		SiVaCGraph sg = new SiVaCGraph(file, D, bits, basename);
		return sg;
	}
	
	
	public static SiVaCGraph load(int D, int bits, String basename)
	{
		SiVaCGraph sg = new SiVaCGraph(D, bits, basename);
		return sg;
	}

	/**
	 * Function that reads the arc list file and splits into two temporary files, one
	 * for the diagonal and one for the non diagonal part and creates a list of
	 * nodes
	 * 
	 * @return the outcome of the operation (true/false)
	 * */
	private boolean createTempFiles() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
		// create temporary files to insert the edges of the graph
	    	tempD = File.createTempFile("diagonal", ".txt");
		tempNoD = File.createTempFile("non-diagonal", ".txt");
		// start reading the original file
		BufferedWriter bwD = new BufferedWriter(new FileWriter(tempD.getAbsoluteFile()));
		String line;
		while ((line = br.readLine()) != null) {
		    // count edges
			this.edges++;
			// convert line to two integers
			String[] temp = line.split("\\s+");
			int a = Integer.parseInt(temp[0]);
			int b = Integer.parseInt(temp[1]);
			// size of the graph (nodes size) is equal to the largest + 1
			if (a >= this.nodes)
				this.nodes = a + 1;
			if (b >= this.nodes)
				this.nodes = b + 1;
			// write to the diagonal temporary file
			if (SiVaCUtils.isDiagonal(a, b, D)) {
				// in the diagonal
				bwD.write(line + '\n');
			}
		}
		br.close();
		bwD.close();
		return true;
	}

    /**
       Function that allocates space for the diagonal, reads the arc list of the diagonal file
       and sets the corresponding bits of the diagonal to 1
       @return the outcome of the operation (true/false)
     */
	private boolean createDiagonal() {
		// store diagonal part
		//TODO
		// is +1 really needed?

	    // calculate the largest possible serialized position in the diagonal and allocate this much space
		int largest = SiVaCUtils.getSerialization(nodes - 1, nodes - 1, nodes, D)+1;
		diagonal = new byte[largest / 8 + (largest % 8 != 0 ? 1 : 0)];
		String line;
		try {
		    // start reading the arc list of the diagonal file
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tempD)));
			while ((line = br.readLine()) != null) {
			    // convert line to two integers
				String[] temp = line.split("\\s+");
				int a = Integer.parseInt(temp[0]);
				int b = Integer.parseInt(temp[1]);
				// find the position in the diagonal
				int no = SiVaCUtils.getSerialization(a, b, nodes, D);
				// set the corresponding bit to 1
				diagonal[no / 8] = SiVaCUtils.set_bit(diagonal[no / 8], no % 8);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

    /**
       Functions that creates the compressed diagonal from the non-copmressed one
       and populates the file with the non-diagonal edges
       @return the outcome of the operation (true/false)
     */
	private boolean createCompressedDiagonal() {
	    // allocate space for the compressed diagonal
		compressedDiagonal = new byte[(nodes * bits) / 8 + (((nodes * bits) % 8 == 0) ? 0 : 1)];
		BufferedReader br;
		BufferedWriter bwNoD;
		// open a reader for the original file and a writer for the file with the arc list
		// of the non-(compressed) diagonal part
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
			bwNoD = new BufferedWriter(new FileWriter(tempNoD.getAbsoluteFile()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		// construct a string with the diagonal for each node
		for (int i = 0; i < nodes; i++) {
			String number = "";
			for (int j = i - D; j < i + D + 1; j++) {
				try {
					int no = SiVaCUtils.getSerialization(i, j, nodes, D);
					if (SiVaCUtils.isSet(diagonal[no / 8], no % 8)) {
						number += "1";
					} else {
						number += "0";
					}
					// when outside of the diagonal assume 0
					// (for first and last nodes)
				} catch (Exception e) {
					number += "0";
				}
			}
			// if the number is not in the map but contains edges fill the file
			// with the non-diagonal part with the ones that cannot be represented
			// (possibly all)
			if (!map.containsKey(number)) {
				if (number.contains("1")) {
					this.putEdgesInNonDiagonalFile(i, number, br, bwNoD);
				}
			// else put the compressed rerpesentation in the diagonal	
			} else {
				this.putCompressedInArray(i, (String)map.get(number));
			}
		}
		try {
			// write the rest of the non diagonal edges from the original file
			this.writeNonDiagonal(-1, br, bwNoD);
			br.close();
			bwNoD.flush();
			bwNoD.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
    
    /**
       Function that finds the less lossy representation for the diagonal of the node
       adds it to the compressed diagonal and the puts the lost edges and the non-diagonal edges
       to a file with the non-diagonal part
       @return the outcome of the operation (true/false)
     */
	private boolean putEdgesInNonDiagonalFile(int node, String number, BufferedReader br, BufferedWriter bwNoD) {
	    // search the map for the best possible representation, the one that captures the diagonal in the
	    // less lossy way (it could possibly be all zeros)
	    int temp = -1, temp2 = 0;
			String representation = "";
			for(Object key : map.keySet())
			{
				temp2 = SiVaCUtils.subset((String) key, number);
				if (temp2 > temp) {
					temp = temp2;
					representation = (String) key;
				}
			}
			// put this representation in the compressed diagonal
		this.putCompressedInArray(node, (String)map.get(representation));
		char[] chars = number.toCharArray();
		char[] rep_chars = representation.toCharArray();
		// write the non-diagonal and the lost edges of the diagonal to the file with
		// the non-diagonal part
		try {
			this.writeNonDiagonal(node, br, bwNoD);
			for (int i = 0; i < chars.length; i++) {
				if (chars[i] == '1' && rep_chars[i] != '1') {
					bwNoD.write(node + " " + (node + i - D) + "\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

    /**
       Function that reads the arc list from the original file and writes all the non-diagonal edges up to
       the first edge that starts from input node
       @input node the id of the node up to where the edges must be written
       @input br a {@link BufferedReader} for the original file
       @input bwNoD a {@link BufferedWriter} for the file containing the non-diagonal part of the graph
     */
	private void writeNonDiagonal(int node, BufferedReader br, BufferedWriter bwNoD) throws IOException {
		String line;
		// continue reading the opened input file
		while ((line = br.readLine()) != null) {
		    // get the edge
			String[] temp = line.split("\\s+");
			int a = Integer.parseInt(temp[0]);
			int b = Integer.parseInt(temp[1]);
			// if it's not in the diagonal add it to the file
			if (!SiVaCUtils.isDiagonal(a, b, D)) {
				bwNoD.write(line + '\n');
			}
			// if the edges of node have been reached break
			// the rest of the edges will be added after the 'lost' diagonal edges
			// of this node will be added
			if (a == node)
				break;
		}
	}
    
    /**
       Function that puts the compressed representation of the diagonal of a node in the correct
       position in the array of the compressed diagonal
       @input node the id of the node that its diagonal is to be written
       @input string the compressed representation of the nodes diagonal
       @return the outcome of the operation (true/false)
     */
	private boolean putCompressedInArray(int node, String string) {
	    // convert the string representation to an array, iterate over it and set the bits
	    // that have to be set
		char[] chars = string.toCharArray();
		int pos = node * chars.length;
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '1')
			{
				compressedDiagonal[(pos / 8)] = SiVaCUtils.set_bit(compressedDiagonal[(pos / 8)], pos % 8);
			}
			pos++;
		}
		return true;
	}

    /**
       Function that stores the byte array that represents the compressed diagonal to
       a file with the appropriate extension
       @return the outcome of the operation (true/false)
     */
	private boolean storeCompressedDiagonal() {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(basename + SiVaC_EXTENSION);
			fos.write(compressedDiagonal);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

    /**
       Function that stores the non-diagonal and the 'lost' diagonal edges using
       a BVGraph
       @return the outcome of the operation (true/false)
     */
	private boolean storeNonDiagonal() {
		// store non diagonal part as BVGraph
		try {
			ImmutableGraph.store(BVGraph.class, ig, basename);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
    
    /**
       Function that turns a binary file to a binary array
       @return the byte array of the diagonal
     */
	private byte[] loadDiagonal(File file) throws IOException {
		return Files.toByteArray(file);
	}

    /**
       Function that checks if an edge is present in the graph
       @input a the outgoing vertex of the edge
       @input b the incoming vertex of the edge
       @return returns true if the edge (a, b) is present in the graph
     */
	public boolean isSuccessor(int a, int b) {
	    // if the edge is in the diagonal, check the compressed diagonal first
	    // and return true if the edge is present
		if (SiVaCUtils.isDiagonal(a, b, D)) {
			if(checkCompressedDiagonal(a, b))
			{
				return true;
			}
		}
		// if the edge was not found, check the rest of the graph
		int[] temp = this.ig.successorArray(a);
		for (int suc : temp) {
			if (suc == b)
				return true;
		}
		return false;
	}

    /**
     Function that constructs the uncompressed diagonal of a node from the compressed
     representation and then checks if the bit that stands for the edge in question
     is set or not
     @input a the outgoing vertex of the edge
     @input b the incoming vertex of the edge
     @return returns true if the edge (a, b) is present in the compressed diagonal
    */
	private boolean checkCompressedDiagonal(int a, int b) {
		char[] chars = new char[bits];
		int pos = a * bits;
		for(int i=0;i<bits;i++)
		{
			chars[i] = (SiVaCUtils.isSet(compressedDiagonal[pos/8], pos%8)) ? '1' : '0';
			pos++;
		}
		return ((String)map.getKey(new String(chars))).toCharArray()[b-a+D]=='1';
	}

//	public LazyIntIterator getSuccessors(int a) {
//		return this.ig.successors(a);
//	}


/**
   Function that reads an input stream and checks if all the edges are present in the graph representation
   @input fis a {@link FileInputStream}
*/
	public void checkAllEdges(FileInputStream fis) throws NumberFormatException, IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
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

	public void printBitsPerEdge()
	{
		double size = new File(basename+SiVaC_EXTENSION).length()+new File(basename+BVGraph.GRAPH_EXTENSION).length()+new File(basename+BVGraph.OFFSETS_EXTENSION).length()+new File(basename+BVGraph.PROPERTIES_EXTENSION).length();
		System.out.println("D="+this.D+"\tb="+this.bits+"\tBitsPerEdge: "+size*8/this.edges);
	}

	@Override
	public ImmutableGraph copy() {
		return null;
	}

	@Override
	public int numNodes() {
		return this.nodes;
	}

	public int numEdges() {
		return this.edges;
	}

	@Override
	public int outdegree(int node) {
		return ig.outdegree(node);
	}

	@Override
	public boolean randomAccess() {
		return false;
	}
	
	public static void main(String[] args) throws NumberFormatException, IOException {
		SiVaCGraph a = null;
		for(int i=5;i<25;i++)
		{
			a = SiVaCGraph.createAndLoad(new File("/var/www/graphs/road/roadNet-CA.txt"), i, 5, "test");
			a.printBitsPerEdge();
		}
//		a = SiVaCGraph.load(5, 5, "test");
		a.checkAllEdges(new FileInputStream(new File("/var/www/graphs/road/roadNet-CA.txt")));
		// a.getSuccessors(5);
		//System.out.println(a.getSuccessors(318).nextInt());
		
	}

}
