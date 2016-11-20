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

    /**
     * Reads in the input parameters and starts the process by creating a Main object.
     * @param args  input parameters
     */
    public static void main(String[] args) {
        String message = "";
        BufferedImage image = null;
        Boolean decrypt = false;

        if (args.length == 0) {
            System.out.println("Please read the enclosed readme text file in order to get an overview of the possible commands.");
            System.exit(1);
        }

        for (int i = 0; i < args.length; i++) {

            if (args[i].equals("-m"))
                message = args[i+1];

            if (args[i].contains(".png"))
                image = loadImage(args[i]);

            if (args[i].equals("-f"))
                message = readFile(args[i + 1]);

            if (args[i].equals("-r"))
                decrypt = true;
        }

        if (image != null) {
            new Main(message, image, decrypt);
        } else {
            System.err.println("Please give me an image to work with! (e.g. example.png)");
            System.exit(-1);
        }
    }

    /**
     * Loads an image from the given input path.
     * It is important that the BufferedImage received from the ImageIO.read() function
     * has a ColorModel which does not support the saving of alpha channels. Therefore
     * we have to create a new BufferedImage with a different ColorModel that supports
     * changing the alpha channel.
     * @param input     path to image
     * @return          BufferedImage of image
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

    /**
     * Reads the input text from the file and saves it as a String.
     * @param input     the path to the input text file
     * @return          a String of the text of the input text file
     */
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

    /**
     * Is created to start the main processes, which are either retrieving or adding
     * a message to an image.
     * @param secretMessage     The message that should be added to the image.
     * @param image             The path to the input image
     * @param decrypt           A flag for whether the image should be decrypted.
     */
    private Main(String secretMessage, BufferedImage image, Boolean decrypt) {
        if (decrypt) {
            this.retrieveMessageFromImage(image);
        } else {
            this.addMessageToImage(secretMessage, image);
        }
    }

    /**
     * Retrieves a message from a given input image by collecting the last 2 bits
     * of each Red, Green, Blue, and Alpha component of each pixel starting at the
     * top left of the picture. The retrieval process stops once the end block, which is
     * "11111111" is encountered. The message is then saved to a text file.
     * @param image     The input image as a BufferedImage
     */
    private void retrieveMessageFromImage(BufferedImage image) {
        String retrievedMessage = "";

        outerloop:
        for (int row = 0; row < image.getHeight(); row++) {
            for (int column = 0; column < image.getWidth(); column++) {
                int rgb = image.getRGB(column, row);
                Color color = new Color(rgb, true);

                ArrayList<String> rgba = getColorBinaries(color);

                String block = retrieveLastBinaryPair(rgba);

                if (block.equals("11111111")) {
                    break outerloop;
                } else {
                    int charCode = Integer.parseInt(block, 2);
                    retrievedMessage += Character.toString((char) charCode);
                }
            }
        }

        saveText(retrievedMessage);
    }

    /**
     * Retrieves the last 2 bits of the Red, Green, Blue, and Alpha channel of a
     * given ArrayList containing the values for these channels as a binary String.
     * @param rgba      The binary strings of the Red, Green, Glue, and Alpha channels of a pixel
     * @return          A concatenation of all last 2 bits of the binary strings.
     */
    private String retrieveLastBinaryPair(ArrayList<String> rgba) {
        String out = "";

        for (String e : rgba)
            out += e.substring(6);

        return out;
    }

    /**
     * The main method to add a message to an image. It runs through every pixel starting
     * at the top left and adds a character represented by 8 bits to the 4 color channels
     * (Red, Green, Blue, Alpha) of the pixel. It replaces the last 2 bits of the binary value
     * for each channel by 2 bits of the character binaries. The adding process is finished
     * by adding an end block of "11111111" to a pixel.
     * @param secretMessage     The message to add to the image.
     * @param image             The image as BufferedImage to which the message is added.
     */
    private void addMessageToImage(String secretMessage, BufferedImage image) {
        String binaryMessage = this.convertStringToBinary(secretMessage);
        Boolean endBlockAppended = false;

        outerloop:
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
                    break outerloop;
            }
        }

        this.saveImage(image);
    }

    /**
     * Checks whether the changed values of the color components are larger than 255.
     * If they are they are replaced with 255. This functions as a safeguard in case something
     * goes wrong somewhere else as the maximum value of an 8 bit Integer representation can
     * only be 255 max. However, I left this in for safety reasons.
     * @param input     The changed color component values as Integers.
     * @return          Color component values which are not larger than 255.
     */
    private ArrayList<Integer> checkMaxValuesOfComponents(ArrayList<Integer> input) {
        for (Integer e : input)
            input.set(input.indexOf(e), e < 255 ? e : 255);

        return input;
    }

    /**
     * Converts an ArrayList of Binaries as Strings to an ArrayList of their Integer values.
     * @param input     An ArrayList of color component values as binaries as strings.
     * @return          An ArrayList of color component values as Integers.
     */
    private ArrayList<Integer> rgbaToInt(ArrayList<String> input) {
        ArrayList<Integer> out = new ArrayList<>();

        for (String e : input)
            out.add(Integer.parseInt(e, 2));

        return out;
    }

    /**
     * Converts the Integer values of the color components to Binary values as Strings.
     * @param color     The color which color components are wanted.
     * @return          An ArrayList of Strings containing the color component values as binaries.
     */
    private ArrayList<String> getColorBinaries(Color color) {
        ArrayList<String> out = new ArrayList<>();
        out.add(intToBinaryString(color.getRed()));
        out.add(intToBinaryString(color.getGreen()));
        out.add(intToBinaryString(color.getBlue()));
        out.add(intToBinaryString(color.getAlpha()));

        return out;
    }

    /**
     * Saves a BufferedImage to a .png file at the location of this program.
     * @param image     The BufferedImage to be saved.
     */
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

    /**
     * Saves a String to a .txt file at the location of this program.
     * @param text      The String to be saved.
     */
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

    /**
     * Converts a String of Characters to a String of their values as Binaries.
     * @param input     The String to be converted.
     * @return          A String containing the binary values of the input characters.
     */
    private String convertStringToBinary(String input) {
        String out = "";

        char[] charMessage = input.toCharArray();
        for (char c : charMessage){
            String temp = Integer.toBinaryString(c);
            out += fillString(temp);
        }

        return out;
    }

    /**
     * Converts an Integer to a Binary String.
     * @param input     The Integer to be converted.
     * @return          A String containing the binary value of the input Integer.
     */
    private String intToBinaryString(int input) {
        return fillString(Integer.toBinaryString(input));
    }

    /**
     * Adds 0's to the beginning of a String of binaries to ensure that the string
     * is exactly 8 bits long. This is necessary as the Integer.toBinaryString() does not always
     * return a String with 8 bits. The leading bits are omitted from the Integer function if
     * they are 0's.
     * @param input     A String of bits.
     * @return          A String of exactly 8 bits.
     */
    private String fillString(String input) {
        for (int i = input.length(); i < 8; i++)
            input = "0" + input;

        return input;
    }
}
