package gr.di.uoa.a8.bvplus.boldi;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import it.unimi.dsi.law.graph.LayeredLabelPropagation;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.NodeIterator;
import it.unimi.dsi.webgraph.Transform;


public class LLPTest {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		ImmutableGraph ig = ArcListASCIIGraph.loadOnce(new FileInputStream(new File("/home/panagiotis/Desktop/twitter.txt")));
		ImmutableGraph.store(BVGraph.class, ig, "/tmp/example");
		
		ig = BVGraph.load("/tmp/example");
//		NodeIterator it = ig.nodeIterator();
//		while(it.hasNext()){}
		ig = Transform.filterArcs(ig, Transform.NO_LOOPS);
		ig = Transform.symmetrize(ig, new ProgressLogger());
		
		LayeredLabelPropagation llp = new LayeredLabelPropagation(ig, 0);
		
		int[] f = llp.computePermutation(new double[]{-5,-2,-.5,-.1,.001,.05,.1,.15,0.2,.3,.5,.8,1,2,3}, null);
//		int[] f = llp.computePermutation(new double[]{.0005,.0008,.001,.0012,.0015,.002,.0025}, null);
//		int[] f = llp.computePermutation(new double[]{2}, null);
		
		ig = Transform.map(ig, f);
		
		NodeIterator it = ig.nodeIterator();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/home/panagiotis/Desktop/twitter-llp.txt")));
		while(it.hasNext())
		{
			Integer node = it.next();
			LazyIntIterator its = ig.successors(node);
			int suc;
			while((suc = its.nextInt()) !=-1)
			{
				bw.write(node+"\t"+suc+"\n");
//				System.out.println(node+"\t"+suc);
			}
		}
		bw.close();
	}
	
}
