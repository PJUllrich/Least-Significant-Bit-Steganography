package com.peterullrich;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
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
        }

        if (message != null && pictureBitMap != null) {
            new Main(message, pictureBitMap);
        } else {
            System.err.println("Could not read in the necessary input options.");
            System.exit(-1);
        }
    }

    private Main(String secretMessage, BufferedImage picture) {
        this.addMessageToImage(secretMessage, picture);
    }

    private void addMessageToImage(String secretMessage, BufferedImage picture) {
        String binaryMessage = this.convertStringToBinary(secretMessage);

        for (int row = 0; row < picture.getHeight(); row++) {
            int startIndex = row * picture.getWidth();
            int endIndex = (row + 1) * picture.getWidth() - 1;

            if (endIndex > binaryMessage.length()) {
                endIndex = binaryMessage.length() - 1;
            } else if (startIndex > binaryMessage.length()) {
                return;
            }

            String messageLine = binaryMessage.substring(startIndex, endIndex);

            for (int column = 0; column < picture.getWidth(); column++) {
                String messageBlock = messageLine.substring(column * 6, column * 6 + 5);
                int rgb = picture.getRGB(column, row);

                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                String sRed = Integer.toBinaryString(red);
                String sGreen = Integer.toBinaryString(green);
                String sBlue = Integer.toBinaryString(blue);

                String newRed = sRed.substring(0, 5) + messageBlock.substring(0, 1);
                String newGreen = sGreen.substring(0, 5) + messageBlock.substring(2, 3);
                String newBlue = sBlue.substring(0, 5) + messageBlock.substring(4, 5);

                Color color = new Color(Integer.parseInt(newRed), Integer.parseInt(newGreen), Integer.parseInt(newBlue));
                picture.setRGB(column, row, color.getRGB());
            }
        }
    }

    private String convertStringToBinary(String text) {
        String out = "";

        char[] charMessage = text.toCharArray();
        for (char c : charMessage)
            out += Integer.toBinaryString(c);

        return out;
    }
}
