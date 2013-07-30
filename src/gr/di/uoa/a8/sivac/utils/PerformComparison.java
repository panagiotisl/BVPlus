package gr.di.uoa.a8.sivac.utils;

import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PerformComparison {

	
	private static Set<Integer> nodes = new HashSet<Integer>();
	private static int edges = 0;
	private static HashMap<Integer,Long> sizes = new HashMap<Integer, Long>();
	
	private static void createNoDFile(String filename, int D) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		File file = new File(filename+".noD"+D);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		String line;
		while ((line = br.readLine()) != null) {
			String[] temp = line.split("\\s+");
			int a = Integer.parseInt(temp[0]);
			int b = Integer.parseInt(temp[1]);
			// only for one iteration
			if(D==1)
			{
				edges++;
				nodes.add(a);
				nodes.add(b);
			}
			if(a>=b-D && a<=b+D)
			{
				
			}
			else
			{
				bw.write(line+'\n');	
			}
		}
		br.close();
		bw.close();
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {

		String filename = args[0];
		int iterations = Integer.parseInt(args[1]);
		boolean social = Boolean.parseBoolean(args[2]);
		
		System.out.println(filename);
		
		for(int i=1;i<=iterations;i++)
		{
			createNoDFile(filename, i);
		}
		
		for(int i=0;i<=iterations;i++)
		{
			String temp = filename + (i!=0 ? ".noD"+i : "");
			ImmutableGraph ig  = ArcListASCIIGraph.loadOnce(new FileInputStream(new File(temp)));
			ImmutableGraph.store(BVGraph.class, ig, temp+".boldi");
			sizes.put(i, new File(temp+".boldi.graph").length()+new File(temp+".boldi.offsets").length()+new File(temp+".boldi.properties").length());
		}
		
		System.out.println("Boldi: "+(sizes.get(0)*8/(float)edges));
		for(int i=1;i<=iterations;i++)
		{
			System.out.println(i+": "+((sizes.get(i)*8 + nodes.size()*2*i+(social ? 0 : 1))/(float)edges)+" ("+((sizes.get(i)*8)/(float)edges)+")");
		}
	}

}
