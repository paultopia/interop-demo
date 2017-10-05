package thumbnail;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import org.imgscalr.Scalr;

public class Thumbnail {

    public static BufferedImage readImageFile(String filename) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(filename));
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return image;
    }

    public static BufferedImage scale(BufferedImage inImage){
        BufferedImage outImage = Scalr.resize(inImage, 200);
        return outImage;
    }

    public static void main(String[] args) {
        String inFile = args[0];
        String outFile = args[1];
        try {
            BufferedImage inImage = readImageFile(inFile);
            BufferedImage outImage = scale(inImage);
            ImageIO.write(outImage, "jpg", new File(outFile));
        }
        catch (IOException e){
            System.err.println(e.getMessage());
        }
    }
}
