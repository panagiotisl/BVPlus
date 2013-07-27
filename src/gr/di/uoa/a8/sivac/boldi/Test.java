package gr.di.uoa.a8.sivac.boldi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import it.unimi.dsi.webgraph.ArcListASCIIGraph;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;


public class Test {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
//		ImmutableGraph ig = ArcListASCIIGraph.loadOnce(new FileInputStream(new File("citation-csconf-mult5-PaperJSON.graph-no-duplicates.txt")));
////		System.out.println(ig.numNodes()+" "+ig.numArcs());
//		ImmutableGraph.store(BVGraph.class, ig, "citation-csconf-mult5-PaperJSON");
//		
//		ig = ArcListASCIIGraph.loadOnce(new FileInputStream(new File("citation-PaperJSON_199star.graph-no-duplicates.txt")));
//		ImmutableGraph.store(BVGraph.class, ig, "citation-PaperJSON_199star");
//		
//		ig = ArcListASCIIGraph.loadOnce(new FileInputStream(new File("citation-csconf-even-PaperJSON.graph-no-duplicates.txt")));
//		ImmutableGraph.store(BVGraph.class, ig, "citation-csconf-even-PaperJSON");
//		
//		ig = ArcListASCIIGraph.loadOnce(new FileInputStream(new File("citation-csconf-mult10-PaperJSON.graph-no-duplicates.txt")));
//		ImmutableGraph.store(BVGraph.class, ig, "citation-csconf-mult10-PaperJSON");
//		
//		ig = ArcListASCIIGraph.loadOnce(new FileInputStream(new File("citation-csconf-mult15-PaperJSON.graph-no-duplicates.txt")));
//		ImmutableGraph.store(BVGraph.class, ig, "citation-csconf-mult15-PaperJSON");
//		
//		ig = ArcListASCIIGraph.loadOnce(new FileInputStream(new File("citation-csconf-PaperJSON.graph-no-duplicates.txt")));
//		ImmutableGraph.store(BVGraph.class, ig, "citation-csconf-PaperJSON");
//		
//		ig = ArcListASCIIGraph.loadOnce(new FileInputStream(new File("citation-PaperJSON_19star.graph-no-duplicates.txt")));
//		ImmutableGraph.store(BVGraph.class, ig, "citation-PaperJSON_19star.graph");
		
//		ImmutableGraph ig  = ArcListASCIIGraph.loadOnce(new FileInputStream(new File("eu2005.txt.noD1")));
//		ImmutableGraph.store(BVGraph.class, ig, "eu.noD1.graph");
		
//		ImmutableGraph ig  = ArcListASCIIGraph.loadOnce(new FileInputStream(new File("dblp2010.txt.noD1")));
//		ImmutableGraph.store(BVGraph.class, ig, "dblp2010.noD1.graph");
		
		ImmutableGraph ig  = ArcListASCIIGraph.loadOnce(new FileInputStream(new File("/tmp/SiVaC-3404334658347105956.a8")));
		ImmutableGraph.store(BVGraph.class, ig, "ljournal-2008/ljournal-2008.noD2.graph");
	}

}
