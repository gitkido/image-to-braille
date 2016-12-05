import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImgToBraille {
	private static BufferedImage read, write;
	
	// *** 出力イメージ用の空白部分定義 *** //
	private static final int OFFSET_W = 10;		// イメージ全体のオフセット
	private static final int OFFSET_H = 10;
	private static final int MARGIN_W = 5;		// 点字と点字の間の幅
	private static final int MARGIN_H = 5;
	private static final int PADDING_W = 2;		// 点と点の間の幅
	private static final int PADDING_H = 2;

	public static void main(String[] args) {
		// 画像を取得
		imgRead();

		// イメージサイズ
		int width = read.getWidth();	// 入力イメージの横幅
		int height = read.getHeight();	// 入力イメージの縦幅
		int width_out;					// 出力イメージの横幅
		int height_out;					// 出力イメージの縦幅
		
		// 途中計算用
		int blockNum_w;					// 入力イメージのブロック数
		int blockNum_h;
		int mod_w = width % 2;			// 点字サイズに変換したときの余り(横)
		int mod_h = height % 3;			// 点字サイズに変換したときの余り(縦)

		// ピクセルが割り切れない場合、はみだし分を削っておく
		if ( mod_w == 1 ) {
			width = width - 1;
		}
		if ( mod_h == 1 ) {
			height = height - 1;
		} else if ( mod_h == 2 ) {
			height = height - 2;
		}
		
		// 点字の数を計算
		blockNum_w = width / 2;
		blockNum_h = height / 3;
		/*
		// 点字の数を計算 横
		if (mod_w == 0) {
			blockNum_w = width / 2;
		} else {
			blockNum_w = width / 2 - 1;
		}

		// 点字の数を計算 縦
		if (mod_h == 0) {
			blockNum_h = height / 3;
		} else {
			blockNum_h = height / 3 - 1;
		}
		*/

		// 出力イメージのサイズ計算をおこなう。
		// もとのイメージに、オフセット、パディング分、マージン分を加算
		width_out = width
			+ OFFSET_W * 2
			+ PADDING_W * blockNum_w
			+ MARGIN_W * (blockNum_w - 1);

		height_out = height
			+ OFFSET_H * 2
			+ PADDING_H * 2 * blockNum_h
			+ MARGIN_H * (blockNum_h -1);

		// 出力イメージの容れ物を作成
		write = new BufferedImage(width_out, height_out,
			 BufferedImage.TYPE_INT_RGB);

		/**
		 *  出力イメージを描画。
		 *  入力イメージを1pxずつ読み込みながら、
		 *  そのピクセル情報を出力イメージにマッピングしていく。
		 *  マッピングの考え方は、出力イメージのサイズ計算に同じ
		 */
		
		int x_in = 0;	// 入力イメージx座標
		int y_in = 0;	// 入力イメージy座標
		
		int x_out = 0;	// 出力イメージx座標
		int y_out = 0;	// 出力イメージy座標
		
		int x_tmp;
		int y_tmp;
		
		// 上オフセット部
		while( y_out < OFFSET_H ){
			while( x_out < width_out ){
				write.setRGB(x_out, y_out, 0xffffff);
				x_out++;	// 1コマ進める
			}
			x_out = 0; y_out++;		// 改行
		}
		
		int cnt1;
		int cnt2;
		
		cnt2 = 1;
		int pixel;
		
		// 点字部
		for( int i = 0; i < blockNum_h; i++ ){
		
			cnt1 = 1;
			// 点字1行分。1列の点々の描画を3回繰り返す
			while ( true ) {
			
				// 1. 点のある行
				// 1.1. 左オフセット
				x_tmp = x_out;
				while( x_out < x_tmp + OFFSET_W ){
					write.setRGB(x_out, y_out, 0xffffff);
					x_out++;
				}
				
				int blockCnt = 0;	// 何ブロック描画したかのカウンタ
				
				while( true ) {
					// 1.2. 点描画
					// 1.2.1. 第1点
					pixel = read.getRGB(x_in, y_in);
					write.setRGB(x_out, y_out, pixel);
					x_in++;
					x_out++;
					// 1.2.2. 横パディング
					for( int j = 0; j < PADDING_W; j++ ) {
						write.setRGB(x_out, y_out, 0xffffff);
						x_out++;
					}
					// 1.2.3. 第2点
					pixel = read.getRGB(x_in, y_in);
					write.setRGB(x_out, y_out, pixel);
					x_in++;
					x_out++;
					
					blockCnt++;
					if( blockCnt >= blockNum_w ) break;	// マージンは点の部分より一回少なく描画する
					
					// 1.3. マージン描画
					for( int j = 0; j < MARGIN_W; j++ ) {
						write.setRGB(x_out, y_out, 0xffffff);
						x_out++;
					}
				}
				
				// 1.4. 右オフセット
				x_tmp = x_out;
				while( x_out < x_tmp + OFFSET_W ){
					write.setRGB(x_out, y_out, 0xffffff);
					x_out++;
				}
				
				// 1.5. 改行
				x_in = 0; y_in++;
				x_out = 0; y_out++;
				
				// 3周目はパディングは不要
				if ( cnt1 == 3 ) break;
				
				// 2. パディング部
				int haba = y_out + PADDING_H;
				while( y_out < haba ){
					while( x_out < width_out ){
						write.setRGB(x_out, y_out, 0xffffff);
						x_out++;	// 1コマ進める
					}
					x_out = 0; y_out++;		// 改行
				}
				
				// カウントアップ
				cnt1++;
			}
			
			// 最後のブロックはマージン不要
			if (cnt2 == blockNum_h) break;
			
			// 2. マージン部 (部品)
			int haba2 = y_out + MARGIN_H;
			while( y_out < haba2 ){
				while( x_out < width_out ){
					write.setRGB(x_out, y_out, 0xffffff);
					x_out++;	// 1コマ進める
				}
				x_out = 0; y_out++;		// 改行
			}
			
			// カウントアップ
			cnt2++;
		}
		
		// 下オフセット部
		int target = y_out + OFFSET_H;
		while( y_out < target ){
			while( x_out < width_out ){
				write.setRGB(x_out, y_out, 0xffffff);
				x_out++;	// 1コマ進める
			}
			x_out = 0; y_out++;		// 改行
		}
		
		
//				System.out.println("x_out: " + x_out + " y_out: " + y_out);
//				System.out.println("x_cnt: " + x_cnt + " y_cnt: " + y_cnt);
		
/* 参考のために残しておく。★★
		for(int y = 0; y < height; ++y) {
			for(int x = 0; x < width; ++x) {
				System.out.println("x: " + x + " y: " + x);
				int pixel = read.getRGB(x, y);
				if(pixel != -1) {
					pixel = 0xff0000;
				}
				write.setRGB(x, y, pixel);
			}
		}
*/

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
