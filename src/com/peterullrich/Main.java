package com.peterullrich;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;
import java.io.*;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        String message = "";
        BufferedImage image = null;
        Boolean decrypt = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-m"))
                message = args[i+1];

            if (args[i].contains(".png"))
                image = loadImage(args[i]);

            if (args[i].equals("-f"))
                message = readFile(args[i + 1]);

            if (args[i].equals("-d"))
                decrypt = true;
        }

        if (image != null) {
            new Main(message, image, decrypt);
        } else {
            System.err.println("Could not read in the necessary input image.");
            System.exit(-1);
        }
    }

    /**
     * Loads the given image to a BufferedImage.
     * It is important to not that we make a deepCopy of the
     * BufferedImage of the ImagaeIO.read() function. This is
     * necessary as the output of ImageIO.read() gives a BufferedImage
     * which has premultiplied Alpha values. This denies us to save
     * the changed alpha values back to the image.
     */
    private static BufferedImage loadImage(String input) {
        try {
            File file = new File(input);
            BufferedImage in = ImageIO.read(file);
            BufferedImage newImage = new BufferedImage(
                    in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = newImage.createGraphics();
            g.drawImage(in, 0, 0, null);
            g.dispose();

            return newImage;
        } catch (IOException e) {
            System.err.println("Could not read image with give file path.");
            e.printStackTrace();
        }

        return null;
    }

    private static String readFile(String input) {
        try {
            FileReader fileReader = new FileReader(input);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;
            String out = "";
            while((line = bufferedReader.readLine()) != null)
                out += line + " ";

            return out;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Main(String secretMessage, BufferedImage image, Boolean decrypt) {
        if (decrypt) {
            this.retrieveMessageFromImage(image);
        } else {
            this.addMessageToImage(secretMessage, image);
        }
    }

    private void retrieveMessageFromImage(BufferedImage image) {
        String retrievedMessage = "";

        for (int row = 0; row < image.getHeight(); row++) {
            for (int column = 0; column < image.getWidth(); column++) {
                int rgb = image.getRGB(column, row);
                Color color = new Color(rgb, true);

                ArrayList<String> rgba = getColorBinaries(color);

                String block = retrieveLastBinaryPair(rgba);

                if (block.equals("11111111")) {
                    break;
                } else {
                    int charCode = Integer.parseInt(block, 2);
                    retrievedMessage += Character.toString((char) charCode);
                }
            }
        }

        saveText(retrievedMessage);
    }

    private String retrieveLastBinaryPair(ArrayList<String> rgba) {
        String out = "";

        for (String e : rgba)
            out += e.substring(6);

        return out;
    }

    private void addMessageToImage(String secretMessage, BufferedImage image) {
        String binaryMessage = this.convertStringToBinary(secretMessage);
        Boolean endBlockAppended = false;

        for (int row = 0; row < image.getHeight(); row++) {
            for (int column = 0; column < image.getWidth(); column++) {

                int start = row * image.getWidth() + column * 8;
                int end = start + 8;

                String messageBlock;
                if (end < binaryMessage.length()) {
                    messageBlock = binaryMessage.substring(start, end);
                } else {
                    messageBlock = "11111111";
                    endBlockAppended = true;
                }

                int rgb = image.getRGB(column, row);
                Color oldColor = new Color(rgb, true);

                ArrayList<String> rgbaOld = getColorBinaries(oldColor);
                ArrayList<String> rgbaNew = new ArrayList<>();

                for (int i = 0; i < rgbaOld.size(); i++)
                    rgbaNew.add(rgbaOld.get(i).substring(0, 6) + messageBlock.substring(i * 2, (i + 1) * 2));

                ArrayList<Integer> newColorComponents = rgbaToInt(rgbaNew);
                newColorComponents = checkMaxValuesOfComponents(newColorComponents);

                Color newColor = new Color(newColorComponents.get(0), newColorComponents.get(1), newColorComponents.get(2), newColorComponents.get(3));
                image.setRGB(column, row, newColor.getRGB());

                if (endBlockAppended)
                    break;
            }
        }

        this.saveImage(image);
    }

    private ArrayList<Integer> checkMaxValuesOfComponents(ArrayList<Integer> input) {
        for (Integer e : input)
            input.set(input.indexOf(e), e < 255 ? e : 255);

        return input;
    }

    private ArrayList<Integer> rgbaToInt(ArrayList<String> input) {
        ArrayList<Integer> out = new ArrayList<>();

        for (String e : input)
            out.add(Integer.parseInt(e, 2));

        return out;
    }

    private ArrayList<String> getColorBinaries(Color color) {
        ArrayList<String> out = new ArrayList<>();
        out.add(intToBinaryString(color.getRed()));
        out.add(intToBinaryString(color.getGreen()));
        out.add(intToBinaryString(color.getBlue()));
        out.add(intToBinaryString(color.getAlpha()));

        return out;
    }

    private void saveImage(BufferedImage image) {
        try {
            File output = new File("output.png");
            ImageIO.write(image, "png", output);
            System.out.println("Saved output to: " + output.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Could not write the output to .png file.");
            e.printStackTrace();
        }
    }

    private void saveText(String text) {
        try {
            File output = new File("retrieved-text.txt");
            FileOutputStream fos = new FileOutputStream(output);

            byte[] contentInBytes = text.getBytes();

            fos.write(contentInBytes);
            fos.flush();
            fos.close();

            System.out.println("Saved output to: " + output.getAbsolutePath());

        } catch (FileNotFoundException e) {
            System.err.println("Could not create output text. Check program writing permissions.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Could not write output to file.");
            e.printStackTrace();
        }
    }

    private String convertStringToBinary(String input) {
        String out = "";

        char[] charMessage = input.toCharArray();
        for (int c = 0; c < charMessage.length; c++){
            String temp = Integer.toBinaryString(charMessage[c]);
            out += fillString(temp);
        }

        return out;
    }

    private String intToBinaryString(int input) {
        return fillString(Integer.toBinaryString(input));
    }

    private String fillString(String input) {
        for (int i = input.length(); i < 8; i++)
            input = "0" + input;

        return input;
    }
}
