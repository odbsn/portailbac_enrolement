package com.officedubac.project.models;

import java.util.Random;

public class CodeGenerator {

    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final Random RANDOM = new Random();

    private static final String LETTERS_ = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String generateCode()
    {
        // Première lettre en majuscule
        char firstLetter = (char) ('A' + RANDOM.nextInt(26));
        // 4 chiffres aléatoires entre 1000 et 9999
        int digits = 1000 + RANDOM.nextInt(9000);

        // Le reste en minuscules
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) { // 7 lettres restantes
            char lower = (char) ('a' + RANDOM.nextInt(26));
            sb.append(lower);
        }

        return firstLetter + sb.toString() + digits + "@";
    }

    public static String generateCode_()
    {
        // 3 lettres aléatoires
        char firstLetter = LETTERS_.charAt(RANDOM.nextInt(LETTERS_.length()));
        char secondLetter = LETTERS_.charAt(RANDOM.nextInt(LETTERS_.length()));
        char thirdLetter = LETTERS_.charAt(RANDOM.nextInt(LETTERS_.length()));

        // 4 chiffres aléatoires entre 1000 et 9999
        int digits = 1000 + RANDOM.nextInt(9000);

        return "" + firstLetter + secondLetter + thirdLetter + digits;
    }
}