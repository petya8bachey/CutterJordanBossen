import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        double v = 0.1;
        int l = 2;
        CutterJordanBossen cutterJordanBossen = new CutterJordanBossen();
        File inputFile = new File("src/cat.png");
        File outputFile = new File("src/cats.png");
        File compresFile = new File("src/cats.png");

        BufferedImage image = ImageIO.read(inputFile);
        byte[] message = "https://chat.eqing.tech/#/chat".getBytes();

        image = cutterJordanBossen.encode(image, message, v, l);
        ImageIO.write(image, "png", outputFile);

        message = cutterJordanBossen.decode(image, l);
        System.out.println(new String(message));

        image = ImageIO.read(compresFile);
        message = cutterJordanBossen.decode(image, l);
        System.out.println(new String(message));
    }
}