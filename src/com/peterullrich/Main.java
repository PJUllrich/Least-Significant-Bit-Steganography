package com.peterullrich;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        String message = null;
        BufferedImage pictureBitMap = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-m"))
                message = args[i+1];

            if (args[i].contains(".png")) {
                try {
                    File file = new File(args[i]);
                    pictureBitMap = ImageIO.read(file);
                } catch (IOException e) {
                    System.err.println("Could not read image with give file path.");
                    e.printStackTrace();
                }
            }

            if (args[i].equals("-f")) {
                try {
                    FileReader fileReader = new FileReader(args[i+1]);

                    // Always wrap FileReader in BufferedReader.
                    BufferedReader bufferedReader = new BufferedReader(fileReader);

                    String line;
                    message = "";
                    while((line = bufferedReader.readLine()) != null) {
                        message += " " + line;
                    }
                } catch (IOException e) {
                    System.err.println("Could not read in given file.");
                    e.printStackTrace();
                }
            }
        }

        if (message != null && pictureBitMap != null) {
            new Main(message, pictureBitMap);
        } else {
            System.err.println("Could not read in the necessary input options.");
            System.exit(-1);
        }
    }

    private static String readFile(String path) {
        File file = new File(path);
        return file.toString();
    }

    private Main(String secretMessage, BufferedImage picture) {
        this.addMessageToImage(secretMessage, picture);
    }

    private void addMessageToImage(String secretMessage, BufferedImage picture) {
        String binaryMessage = this.convertStringToBinary(secretMessage);
        Boolean endBlockAppended = false;

        for (int row = 0; row < picture.getHeight(); row++) {
            for (int column = 0; column < picture.getWidth(); column++) {

                int start = row * picture.getWidth() + column * 8;
                int end = start + 8;

                String messageBlock;
                if (end < binaryMessage.length()) {
                    messageBlock = binaryMessage.substring(start, end);
                } else {
                    messageBlock = "11111111";
                    endBlockAppended = true;
                }

                int rgb = picture.getRGB(column, row);
                Color oldColor = new Color(rgb, true);

                String originalRed = fillString(Integer.toBinaryString(oldColor.getRed()));
                String originalGreen = fillString(Integer.toBinaryString(oldColor.getGreen()));
                String originalBlue = fillString(Integer.toBinaryString(oldColor.getBlue()));
                String originalAlpha = fillString(Integer.toBinaryString(oldColor.getAlpha()));

                String changedRed = originalRed.substring(0, 6) + messageBlock.substring(0, 2);
                String changedGreen = originalGreen.substring(0, 6) + messageBlock.substring(2, 4);
                String changedBlue = originalBlue.substring(0, 6) + messageBlock.substring(4, 6);
                String changedAlpha = originalAlpha.substring(0, 6) + messageBlock.substring(6);

                int newRed = Integer.parseInt(changedRed);
                int newGreen = Integer.parseInt(changedGreen);
                int newBlue = Integer.parseInt(changedBlue);
                int newAlpha = Integer.parseInt(changedAlpha);

                newRed = newRed > 255 ? 255 : newRed;
                newGreen = newGreen > 255 ? 255 : newGreen;
                newBlue = newBlue > 255 ? 255 : newBlue;
                newAlpha = newAlpha > 255 ? 255 : newAlpha;

                Color color = new Color(newRed, newGreen, newBlue, newAlpha);
                picture.setRGB(column, row, color.getRGB());

                if (endBlockAppended)
                    break;
            }
        }

        this.saveFile(picture);
    }

    private void saveFile(BufferedImage picture) {
        try {
            File output = new File("output.png");
            ImageIO.write(picture, "png", output);
            System.out.println("Saved output in: " + output.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Could not write the output to .png file.");
            e.printStackTrace();
        }
    }

    private String convertStringToBinary(String text) {
        String out = "";

        char[] charMessage = text.toCharArray();
        for (int c = 0; c < charMessage.length; c++){
            String temp = Integer.toBinaryString(charMessage[c]);
            out += fillString(temp);
        }

        return out;
    }

    private String fillString(String input) {
        for (int i = input.length(); i < 8; i++)
            input = "0" + input;

        return input;
    }
}
