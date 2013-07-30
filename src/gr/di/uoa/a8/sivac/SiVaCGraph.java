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
import java.util.HashSet;

import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;

public class SiVaCGraph extends ImmutableGraph{

	private static final String SiVaC_EXTENSION = "a8";
	private ImmutableGraph ig;
	private int D;
	private int stripe_size;
	private int size;
	private File diagonal /* file descriptor for the diagonal file */;
	private File tempD /* file descriptor  for a temp file with the arc list for the diagonal part */;
	private File tempNoD /* file descriptor  for a temp file with the arc list for the non diagonal part */;
	private HashSet<Integer> nodes;
	
	public SiVaCGraph(InputStream is, int d) throws IOException {
		this.D = d;
		this.stripe_size = 2*D+1;
		this.nodes = new HashSet<Integer>();
		createTempFiles(is);
		this.ig = ArcListASCIIGraph.loadOnce(new FileInputStream(tempNoD));
		this.size = nodes.size();
	}

	/** Function that reads the arc list file and splits into two temp files, one for the diagonal and on for the non diagonal part
	 *  and creates a list of nodes
	 * */
	private boolean createTempFiles(InputStream is) throws IOException
	{
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		tempD = File.createTempFile("SiVaC-D-", ".a8");
		tempNoD = File.createTempFile("SiVaC-NoD-", ".a8");
		tempD.deleteOnExit();
		tempNoD.deleteOnExit();
		BufferedWriter bwD = new BufferedWriter(new FileWriter(tempD.getAbsoluteFile()));
		BufferedWriter bwNoD = new BufferedWriter(new FileWriter(tempNoD.getAbsoluteFile()));
		String line;
		while ((line = br.readLine()) != null) {
			String[] temp = line.split("\\s+");
			int a = Integer.parseInt(temp[0]);
			int b = Integer.parseInt(temp[1]);
			nodes.add(a);
			nodes.add(b);
			if(a>=b-D && a<=b+D)
			{
				// in the diagonal
				bwD.write(line+'\n');
			}
			else
			{
				// outside the diagonal
				bwNoD.write(line+'\n');	
			}
		}
		br.close();
		bwD.close();
		bwNoD.close();
		return true;
	}
	
	/** get position in file from node pair */
	public int getSerialization(int a, int b)
	{
		// check if input is valid
		if((a>b+D || a<b-D) || (a<0 || b<0) || a >= size || b >= size)
			throw new IllegalArgumentException("not a valid node pair: ("+a+", "+b+")");
		// calculate position
		int no = a*this.stripe_size+b+D-a;
		int temp = D;
		// remove missing from beginning
		for(int i=0;i<D;i++)
		{
			if(a>=i)
			{
				no-=temp;
				temp--;
			}
		}
		//TODO correct?
		temp = 1;
		// remove missing from end
		for(int i=this.size+1-this.D;i<this.size;i++)
		{
			if(a>=i)
			{
				no-=temp;
				temp++;
			}
		}
		return no;
	}

	// tests if bit is set in a byte
	private static boolean isSet(byte my_byte, int pos)
	{
		if(pos>7 || pos <0)
			throw new IllegalArgumentException("not a valid bit position: "+pos);
	   return (my_byte & (1 << pos))!=0;
	} 

	// set a bit in a byte
	private static byte set_bit(byte my_byte, int pos)
	{
		if(pos>7 || pos <0)
			throw new IllegalArgumentException("not a valid bit position: "+pos);
		return (byte) (my_byte | (1 << pos));
	}
	
	// unset a bit in a byte
	private static byte unset_bit(byte my_byte, int pos)
	{
		if(pos>7 || pos <0)
			throw new IllegalArgumentException("not a valid bit position: "+pos);
		return (byte) (my_byte & ~(1 << pos));
	}
	
	public static SiVaCGraph loadOnce(InputStream is)
	{
		return loadOnce(is, 1);
	}

	public static SiVaCGraph loadOnce(InputStream is, int d)
	{
		SiVaCGraph sg;
		try {
			sg = new SiVaCGraph(is, d);
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

	@Override
	public int outdegree(int arg0) {
		return ig.outdegree(arg0);
	}

	@Override
	public boolean randomAccess() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean store(String basename)
	{
		//store diagonal part
		// TODO check! one more byte is needed (i think)
		byte[] array = new byte[getSerialization(this.size, this.size)/8];
		String line;
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tempD)));
			while ((line = br.readLine()) != null) {
				String[] temp = line.split("\\s+");
				int a = Integer.parseInt(temp[0]);
				int b = Integer.parseInt(temp[1]);
				int no = getSerialization(a, b);
				set_bit(array[no/8], no%8);
			}
			br.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(basename+"."+SiVaC_EXTENSION);
			fos.write(array);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// store non diagonal part as BVGraph
		try {
			ImmutableGraph.store(BVGraph.class, this.ig, basename);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		SiVaCGraph a = SiVaCGraph.loadOnce(new FileInputStream(new File("/var/www/graphs/cnr-2000/cnr-2000.txt")),1);
	}
	
}
