public class Main {
	public static void main (String[] args) {
		ImgToBraille imgToBraille = new ImgToBraille();
		
		imgToBraille.convert(args[0], args[1]);
		
		return;
	}
}