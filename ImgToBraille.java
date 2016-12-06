import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;

public class ImgToBraille {
	private BufferedImage read, write;
	
	// 出力イメージ用の空白部分定義
	private int OFFSET_W = 10;		// イメージ全体のオフセット
	private int OFFSET_H = 10;
	private int MARGIN_W = 5;		// 点字と点字の間の幅
	private int MARGIN_H = 5;
	private int PADDING_W = 2;		// 点と点の間の幅
	private int PADDING_H = 2;
	
	private static final int BGCOLOR = 0xffffff;	// 出力イメージ背景色
	
	// 描画用ポインタ
	private int x_in = 0;			// 入力イメージx座標
	private int y_in = 0;			// 入力イメージy座標
	private int x_out = 0;			// 出力イメージx座標
	private int y_out = 0;			// 出力イメージy座標
	
	private int blockNum_w;			// 入力イメージのブロック数(横)
	private int blockNum_h;			// 入力イメージのブロック数(縦)

	// イメージサイズ
	private int width_in;			// 入力イメージの横幅
	private int height_in;			// 入力イメージの縦幅
	private int width_out;			// 出力イメージの横幅
	private int height_out;			// 出力イメージの縦幅
	
	public void convert(String inFileName, String outFileName) {
		
		Properties properties = new Properties();
		String file = "ini.properties";
		
		try{
			InputStream inputStream = new FileInputStream(file);
			properties.load(inputStream);
			inputStream.close();
			
			// 値の取得
			OFFSET_W = Integer.parseInt(properties.getProperty("OFFSET_W"));
			OFFSET_H = Integer.parseInt(properties.getProperty("OFFSET_H"));
			MARGIN_W = Integer.parseInt(properties.getProperty("MARGIN_W"));
			MARGIN_H = Integer.parseInt(properties.getProperty("MARGIN_H"));
			PADDING_W = Integer.parseInt(properties.getProperty("PADDING_W"));
			PADDING_H = Integer.parseInt(properties.getProperty("PADDING_H"));
			
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
			
		
		// 画像を取得
		imgRead(inFileName);
		
		// 下準備
		calculate();

		// 出力イメージの容れ物を作成
		write = new BufferedImage(width_out, height_out,
			 BufferedImage.TYPE_INT_RGB);
		
		// 出力イメージ描画
		draw();
		
		// 画像を書き出し
		imgWrite(outFileName);
	}

	// 画像の読み込み
	private void imgRead(String inFileName) {
		try {
			read = ImageIO.read(new File(inFileName));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	// 画像の書き出し
	private void imgWrite(String outFileName) {
		try {
			ImageIO.write(write, "png", new File(outFileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 画像サイズなど、必要な数値の計算
	private void calculate() {
		width_in = read.getWidth();		// 入力イメージの横幅
		height_in = read.getHeight();	// 入力イメージの縦幅
		
		// 途中計算用
		int mod_w = width_in % 2;			// 点字サイズに変換したときの余り(横)
		int mod_h = height_in % 3;			// 点字サイズに変換したときの余り(縦)

		// たてよこが割り切れない場合、はみだし分を削っておく
		if ( mod_w == 1 ) {
			width_in = width_in - 1;
		}
		if ( mod_h == 1 ) {
			height_in = height_in - 1;
		} else if ( mod_h == 2 ) {
			height_in = height_in - 2;
		}
		
		// 点字の数を計算
		blockNum_w = width_in / 2;
		blockNum_h = height_in / 3;

		// 出力イメージのサイズ計算をおこなう。
		// もとのイメージに、オフセット、パディング分、マージン分を加算
		width_out = width_in
			+ OFFSET_W * 2
			+ PADDING_W * blockNum_w
			+ MARGIN_W * (blockNum_w - 1);

		height_out = height_in
			+ OFFSET_H * 2
			+ PADDING_H * 2 * blockNum_h
			+ MARGIN_H * (blockNum_h -1);
	}
	
	// 改行(入力イメージ)
	private void inNL() {
		x_in = 0;
		y_in++;
	}
	
	// 改行(出力イメージ)
	private void outNL() {
		x_out = 0;
		y_out++;
	}
	
	/**
	 *  出力イメージを描画。
	 *  入力イメージを1pxずつ読み込みながら、
	 *  そのピクセル情報を出力イメージにマッピングしていく。
	 */
	private void draw() {
		
		// 計算用一時変数
		int target;
		
		int cnt1;
		int cnt2;
		int pixel;
		
		// 上オフセット部
		while( y_out < OFFSET_H ){
			while( x_out < width_out ){
				write.setRGB(x_out, y_out, BGCOLOR);
				x_out++;	// 1コマ進める
			}
			outNL();		// 改行
		}
		
		cnt2 = 1;
		// 点字部
		for( int i = 0; i < blockNum_h; i++ ){
		
			cnt1 = 1;
			// 点字1行分。1列の点々の描画を3回繰り返す
			while ( true ) {
			
				// 1. 点のある行
				// 1.1. 左オフセット
				target = x_out + OFFSET_W;
				while( x_out < target ){
					write.setRGB(x_out, y_out, BGCOLOR);
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
						write.setRGB(x_out, y_out, BGCOLOR);
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
						write.setRGB(x_out, y_out, BGCOLOR);
						x_out++;
					}
				}
				
				// 1.4. 右オフセット
				target = x_out + OFFSET_W;
				while( x_out < target ){
					write.setRGB(x_out, y_out, BGCOLOR);
					x_out++;
				}
				
				// 1.5. 改行
				inNL();
				outNL();
				
				// 3周目はパディングは不要
				if ( cnt1 == 3 ) break;
				
				// 2. パディング部
				target = y_out + PADDING_H;
				while( y_out < target ){
					while( x_out < width_out ){
						write.setRGB(x_out, y_out, BGCOLOR);
						x_out++;	// 1コマ進める
					}
					outNL();		// 改行
				}
				
				// カウントアップ
				cnt1++;
			}
			
			// 最後のブロックはマージン不要
			if (cnt2 == blockNum_h) break;
			
			// 2. マージン部 (部品)
			target = y_out + MARGIN_H;
			while( y_out < target ){
				while( x_out < width_out ){
					write.setRGB(x_out, y_out, BGCOLOR);
					x_out++;	// 1コマ進める
				}
				outNL();		// 改行
			}
			
			// カウントアップ
			cnt2++;
		}
		
		// 下オフセット部
		target = y_out + OFFSET_H;
		while( y_out < target ){
			while( x_out < width_out ){
				write.setRGB(x_out, y_out, BGCOLOR);
				x_out++;	// 1コマ進める
			}
			outNL();		// 改行
		}
	}
	
}
