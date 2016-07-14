/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unige.cui.meghdad.toolkit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Executes MostFrequentPos method from Class Tools for a set of bigrams.
 * MostFrequentPos accepts any order ngrams but this method (RunMostFrequentPos)
 * was designed for only bigrams.
 *
 *
 *
 * Command line arguments: args[0] : path to pos tagged bigrams args[1] : path
 * to output
 *
 * @author Meghdad Farahmand<meghdad.farahmand@gmail.com>
 */
public class RunMostFrequentPos {

    public static void main(String[] args) throws FileNotFoundException, IOException {

        //create instance of class Tools
        Tools T = new Tools();
        System.out.println("Reading list of bigrams...");

        //read a list of pos tagged bigrams (with duplicates) from file
        BufferedReader bigrams = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(args[0])));

        HashMap<String, Integer> bigrMap = new HashMap();

        //state_NN police_NN 23
        Pattern posBigrFreqP = Pattern.compile("^([\\w_\\s]+)\\s+(\\d+)$");

        
        String s = "";
        while ((s = bigrams.readLine()) != null) {

            Matcher posBigrFreqM = posBigrFreqP.matcher(s);
            if (posBigrFreqM.find()) {

//                System.out.println(s);
                bigrMap.put(posBigrFreqM.group(1), Integer.parseInt(posBigrFreqM.group(2)));

            }
        }

        //generating output (file)
        Writer wr = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(args[1]), "UTF-8"));
        
        HashMap<String, Integer> MostFreqPos = T.MostFrequentPosTagNgram(bigrMap);

        
        System.out.println("Number of bigrams in the list: " + bigrMap.size());
        for (String e : MostFreqPos.keySet()) {

            wr.write(e + " " + MostFreqPos.get(e) + "\n");

        }
    }
}
