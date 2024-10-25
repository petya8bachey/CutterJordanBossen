import java.awt.image.BufferedImage;

public class CutterJordanBossen {

    public int encodePixel(int color, boolean m, double v) {
        double blue = getBlue(color);
        blue += m ? v * getBrightness(color) : -v * getBrightness(color);
        return getColor(getRed(color), getGreen(color), (int) blue);
    }

    private double getBrightness(int color) {
        return 0.2989 * getRed(color) + 0.58662 * getGreen(color) + 0.11448 * getBlue(color);
    }

    private int getRed(int color) {
        return (color >> 16) & 0xFF;
    }

    private int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }

    private int getBlue(int color) {
        return color & 0xFF;
    }

    public int getColor(int red, int green, int blue) {
        return (red << 16) | (green << 8) | blue;
    }

    public static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
    }

    public BufferedImage encode(BufferedImage image, byte[] message, double v, int l) {
        byte[] messageWithSize = new byte[message.length + 4];
        System.arraycopy(intToByteArray(message.length), 0, messageWithSize, 0, 4);
        System.arraycopy(message, 0, messageWithSize, 4, message.length);

        int width = image.getWidth(), height = image.getHeight(), currentBit = 0;
        for (int i = l; i < width - l && currentBit < messageWithSize.length * 8; i += (1 + l)) {
            for (int j = l; j < height - l && currentBit < messageWithSize.length * 8; j += (1 + l)) {
                image.setRGB(i, j, encodePixel(image.getRGB(i, j), getBit(messageWithSize, currentBit), v));
                currentBit++;
            }
        }
        return image;
    }

    private boolean getBit(byte[] byteArray, int bitPosition) {
        return ((byteArray[bitPosition / 8] >> (7 - (bitPosition % 8))) & 1) == 1;
    }

    public static void setBit(byte[] byteArray, int bitPosition, boolean value) {
        int byteIndex = bitPosition / 8, bitIndex = 7 - (bitPosition % 8);
        byteArray[byteIndex] = (byte) (value ? byteArray[byteIndex] | (1 << bitIndex) : byteArray[byteIndex] & ~(1 << bitIndex));
    }

    public byte[] decode(BufferedImage image, int l) {
        int width = image.getWidth(), height = image.getHeight(), currentBit = 0;
        byte[] size = new byte[4], message = new byte[4];
        for (int i = l; i < width - l && currentBit < message.length * 8 + 32; i += (1 + l)) {
            for (int j = l; j < height - l && currentBit < message.length * 8 + 32; j += (1 + l)) {
                double actualBlue = getBlue(image.getRGB(i, j));
                double predictedBlue = predictBlue(image, i, j, l);
                boolean bit = actualBlue > predictedBlue;
                if (currentBit < 31) {
                    setBit(size, currentBit, bit);
                } else if (currentBit == 31) {
                    setBit(size, currentBit, bit);
                    message = new byte[byteArrayToInt(size)];
                } else {
                    setBit(message, currentBit - 32, bit);
                }
                currentBit++;
            }
        }
        return message;
    }

    public static int byteArrayToInt(byte[] bytes) {
        return (bytes[0] << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
    }

    private int predictBlue(BufferedImage image, int x, int y, int l) {
        double sum = 0.0;
        for (int i = 1; i <= l; i++) {
            sum += getBlue(image.getRGB(x, y - i)) + getBlue(image.getRGB(x, y + i)) +
                    getBlue(image.getRGB(x - i, y)) + getBlue(image.getRGB(x + i, y));
        }
        return (int) (sum / (4 * l));
    }
}
