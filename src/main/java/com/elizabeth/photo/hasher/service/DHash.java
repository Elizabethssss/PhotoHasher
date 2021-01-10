package com.elizabeth.photo.hasher.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class DHash {

    public static final double MINIMUM_SIMILARITY_THRESHOLD = 0.9;
    public static final int SIZE = 9;

    public long getHash(BufferedImage bufferedImage) {
        BufferedImage image = prepareImage(bufferedImage);
        int[][] pixels = createMatrixFromPixels(image);
        return getDiagonalDifference(pixels);
    }

    private BufferedImage prepareImage(BufferedImage bufferedImage) {
        BufferedImage grayScale = discolorImage(bufferedImage);
        return resizeImage(grayScale);
    }

    private BufferedImage discolorImage(BufferedImage bufferedImage) {
        BufferedImage grayScale = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        grayScale.createGraphics().drawImage(bufferedImage, 0, 0, null);

        return grayScale;
    }

    private BufferedImage resizeImage(BufferedImage img) {
        Image tmp = img.getScaledInstance(SIZE, SIZE, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(SIZE, SIZE, img.getType());
        resized.createGraphics().drawImage(tmp, 0, 0, null);

        return resized;
    }

    private int[][] createMatrixFromPixels(BufferedImage image) {
        int[][] matrix = new int[SIZE][SIZE];

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                matrix[i][j] = image.getRGB(i, j);
            }
        }

        return matrix;
    }

    public long getDiagonalDifference(int[][] matrix) {
        final int size = matrix.length;
        long hash = 0;
        for (int i = 1; i < size; i++) {
            for (int j = 1; j < size; j++) {
                if(matrix[i][j] >= matrix[i-1][j-1]) {
                    hash |= 1;
                }
                hash <<= 1;
            }
        }
        return hash;
    }

    public double getSimilarity(long hash1, long hash2) {
        int distance = this.getDistance(hash1, hash2);
        int matrixSize = SIZE * SIZE;
        return 1.0 - ((double) distance / (double) matrixSize);

    }

    public int getDistance(long hash1, long hash2) {
        long xor = hash1 ^ hash2;
        int distance = 0;

        if (xor < 0) {
            for (int i = 0; i < 64; i++) {
                distance += xor & 1;
                xor >>= 1;
            }
        }
        else {
            while (xor > 0) {
                distance += xor & 1;
                xor >>= 1;
            }
        }

        return distance;
    }

    public boolean areSimilarHashes(long hash1, long hash2, double threshold) {
        return getSimilarity(hash1, hash2) >= threshold / 100;
    }
}
