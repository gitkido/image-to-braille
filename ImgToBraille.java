import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageTest {
	private static BufferedImage read, write;
	private static final int OFFSET_W = 1;
	private static final int OFFSET_H = 1;
	private static final int MARGIN_W = 1;
	private static final int MARGIN_H = 1;
	private static final int PADDING_W = 1;
	private static final int PADDING_H = 1;

	public static void main(String[] args) {
		// 画像を取得
		imgRead();

		int width = read.getWidth();
		int height = read.getHeight();
		int blockNum_w;
		int blockNum_h;
		int mod_w = width % 2;
		int mod_h = height % 3;
		int width_out;
		int height_out;

		if (mod_w == 0) {
			blockNum_w = width / 2;
		} else {
			blockNum_w = width / 2 + 1;
		}

		if (mod_h == 0) {
			blockNum_h = height / 3;
		} else {
			blockNum_h = height / 3 + 1;
		}

		width_out = width
			+ OFFSET_W * 2
			+ PADDING_W * blockNum_w
			+ MARGIN_W * (blockNum_w - 1);

		height_out = height
			+ OFFSET_H * 2
			+ PADDING_H * 2 * blockNum_h
			+ MARGIN_H * (blockNum_h -1);

		write = new BufferedImage(width_out, height_out,
			 BufferedImage.TYPE_INT_RGB);

		for(int y = 0; y < height_out; ++y) {
			for(int x = 0; x < width_out; ++x) {
				int pixel = read.getRGB(x, y);
				if(pixel != -1) {
					pixel = 0xff0000;
				}
				write.setRGB(x, y, pixel);
			}
		}

		// 画像を書き出し
		imgWrite();
	}

	// 画像の読み込み
	private static void imgRead() {
		try {
			read = ImageIO.read(new File("test.png"));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	// 画像の書き出し
	private static void imgWrite() {
		try {
			ImageIO.write(write, "png", new File("test2.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
