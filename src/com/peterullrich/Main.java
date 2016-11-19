package com.peterullrich;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {
    private String secretMessage;
    private BufferedImage picture;

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

    public Main(String secretMessage, BufferedImage picture) {
        this.secretMessage = secretMessage;
        this.picture = picture;
    }
}
