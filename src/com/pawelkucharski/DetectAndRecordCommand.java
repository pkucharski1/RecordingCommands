package com.pawelkucharski;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;


public class DetectAndRecordCommand {
    public static void main(String[] args) {
        TargetDataLine line = null;
        AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("The line is not supported.");
        }

        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
        } catch (LineUnavailableException ex) {
            System.out.println("The TargetDataLine is Unavailable.");
        }

        List<Byte> byteArrayList = new ArrayList<>();
        byte[] bytes = new byte[line.getBufferSize() / 10]; 
        File audioFile = new File("C:\\Shared_folder2\\command.wav");
        int threshold = 5;
        System.out.println("Starting recording...");

        while (true) {
            line.read(bytes, 0, bytes.length);
            if (calculate(bytes) > threshold) {
                System.out.println("Sound detected...");
                for (byte b : bytes) {
                    byteArrayList.add(b);
                }
            }
            if (calculate(bytes) < threshold && byteArrayList.size() > 70000) {
                System.out.println("Saving sound...");
                byte[] bytesRecorded = new byte[byteArrayList.size()];

                for (int i = 0; i < byteArrayList.size(); i++) {
                    bytesRecorded[i] = byteArrayList.get(i);
                }

                AudioInputStream stream = new AudioInputStream(
                        new ByteArrayInputStream(bytesRecorded),
                        format, bytesRecorded.length);

                try {
                    AudioSystem.write(stream, AudioFileFormat.Type.WAVE, audioFile);
                    System.out.println("File saved");
                    byteArrayList.clear();

                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }else if (calculate(bytes) < threshold && !byteArrayList.isEmpty()) {
                System.out.println("Just noise... clearing byteArrayList");
                byteArrayList.clear();
            }
//            if (byteArrayList.isEmpty()) {
//                System.out.println("Sound not yet detected...");
//            }
        }
    }

    public static int calculate(byte[] audioData) {
        double sum = 0.0;
        for (byte num : audioData)
            sum += num * num;
        int result = (int) round(Math.sqrt(sum / audioData.length),0);
        return result;
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}


