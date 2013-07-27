package gr.di.uoa.a8.sivac.utils;

public class GetBitsPerEdge {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		for (String arg : args) {
			System.out.println(arg);
		}
		System.out.println();
		if (args.length < 1)
			throw new IllegalArgumentException("Usage: String pathtograph [int diagonal size] [boolean social]");
		if (args.length == 1)
			System.out.println(SiVaCUtils.bitsPerEdge(args[0], 1, false));
		else if (args.length == 2)
			System.out.println(SiVaCUtils.bitsPerEdge(args[0], Integer.parseInt(args[1]), false));
		else if (args.length == 3)
			System.out.println(SiVaCUtils.bitsPerEdge(args[0], Integer.parseInt(args[1]), Boolean.parseBoolean(args[2])));
		else
			throw new IllegalArgumentException("Usage: String pathtograph [int diagonal size] [boolean social]");
	}

}
