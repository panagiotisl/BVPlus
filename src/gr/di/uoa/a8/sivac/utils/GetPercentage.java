package gr.di.uoa.a8.sivac.utils;

public class GetPercentage {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		for (String arg : args) {
			System.out.println(arg);
		}
		System.out.println();
		if(args.length<1)
			throw new IllegalArgumentException("Usage: pathtograph [diagonal size]");
		if(args.length==1)
			System.out.println(SiVaCUtils.percentageInDiagonal(args[0],1));
		else if(args.length==2)
			System.out.println(SiVaCUtils.percentageInDiagonal(args[0],Integer.parseInt(args[1])));
		else
			throw new IllegalArgumentException("Usage: pathtograph [diagonal size]");

	}

}
