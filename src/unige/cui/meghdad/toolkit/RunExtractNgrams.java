/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unige.cui.meghdad.toolkit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Executes ExtractNgrams method from Class Tools. 
 * Defines and accepts several command line options. 
 * 
 * 
 * @author Meghdad Farahmand
 * 
 *  
 */
public class RunExtractNgrams {

    public static void main(String[] args) throws ParseException, FileNotFoundException, IOException {

        System.out.println();
        System.out.println("--- RunExtractNgram ---");
        System.out.println();

        //using apache commons CLI to parse command line arguments
        // create Options object
        Options options = new Options();

        //required options
        options.addOption("p2corpus", true, "Path 2 Corpus");
        

        //optional options
        options.addOption("p2outdir", true, "Path 2 Output Directory");
        options.addOption("isPosTagged", true, "Is the input corpus postagged?");//default 1
        options.addOption("outPutPosTagged", true, "Should the output be postagged?");//default 1
        options.addOption("freqTh", true, "Threshold on frequency of to be extracted words");//default 1
        options.addOption("order", true, "order of ngrams");//default 2
        options.addOption("sort", true, "whether or not output list be sorted");//default 0
        options.addOption("ignoreCase", true, "whether or not ignore case");//default 1
        
        //options.addOption("mostFreqPos", true, "whether or not return ngram with most frequent pos tag set");//default 1

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        //creating errors for required options
        if (cmd.hasOption("p2corpus")) {
            System.out.println("Reading corpus from: " + cmd.getOptionValue("p2corpus"));
        } else {
            System.out.println("Erros: Path to corpus (-p2corpus)) is not set.");
            return;
        }

        //set default option values:
        //get the parent of the class path, initialize path2output with it
        File f1 = new File(Tools.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getParentFile();
        String path2output = f1.getPath();
        if (cmd.hasOption("p2outdir")) {
            path2output = cmd.getOptionValue("p2outdir");
        }

        boolean isPosTagged = true;
        if (cmd.hasOption("isPosTagged")) {
            if (cmd.getOptionValue("isPosTagged").equals("0")) {
                isPosTagged = false;
            }
        }
        boolean outPutPosTagged = true;
        if (cmd.hasOption("outPutPosTagged")) {
            if (cmd.getOptionValue("outPutPosTagged").equals("0")) {
                outPutPosTagged = false;
            }
        }
        if(outPutPosTagged && !isPosTagged){
            System.out.println("Can't return pos tagged ngrams from untagged corpus. ");
            return;
        }
        
        
        
        boolean sort = false;
        if (cmd.hasOption("sort")) {
            if (cmd.getOptionValue("sort").equals("1")) {
                sort = true;
            }
        }

        int freqTh = 1;
        if (cmd.hasOption("freqTh")) {
            freqTh = Integer.parseInt(cmd.getOptionValue("freqTh"));
        }
        int order = 2;
        if (cmd.hasOption("order")) {
            order = Integer.parseInt(cmd.getOptionValue("order"));
        }
        boolean ignoreCase = true;
        if(cmd.hasOption("ignoreCase")){
            if(cmd.getOptionValue("ignoreCase").equals("1")){
                ignoreCase = true;
            }else if(cmd.getOptionValue("ignoreCase").equals("0")){
                ignoreCase = false;
            }else{
                System.out.println("-ignorecase can only have 0 and 1 values.");
                return;
            }
        }

        Tools T = new Tools();

       
        
        //ExtractNgrams_v2(String p2corpus, int freqThreshold, int order, boolean isPosTagged)
        
        //List<HashMap<String,Integer>> ngsPlainngsPos = T.ExtractNgrams(cmd.getOptionValue("p2corpus"), freqTh, order, isPosTagged,ignoreCase); 
        //HashMap<String,Integer> ngsPlain = ngsPlainngsPos.get(0);
        //HashMap<String,Integer> ngsPos = ngsPlainngsPos.get(1);
        

        //overloaded version
        HashMap<String,Integer> ngs = T.ExtractNgrams(cmd.getOptionValue("p2corpus"), freqTh, order, isPosTagged,outPutPosTagged,ignoreCase); 
        
        
        Map<String, Integer> finalNgs;

        if (sort) {
            finalNgs = new TreeMap<String, Integer>(ngs);
        } else {
            finalNgs = new HashMap<String, Integer>(ngs);
        }
        
        
        
        
        //writing results:
        
        File ngrDir = new File(path2output + "/"+String.valueOf(order) + "-grams");
        ngrDir.mkdir();
        System.out.println("Writing results in: "+ngrDir);
        
        
        
            if(outPutPosTagged){

            Writer b_pos = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(ngrDir + "/" + String.valueOf(order) + "-grams-pos-gte-"+ freqTh+".txt"), "UTF-8"));
            
            
            //write with POS
              for (String unigramEtPos : finalNgs.keySet()) {
                    b_pos.write(unigramEtPos + " " + finalNgs.get(unigramEtPos) + "\n");
              }
              b_pos.close();
            
        }else{
            
            Writer b = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(ngrDir + "/" + String.valueOf(order) + "-grams-gte-" + freqTh+".txt"), "UTF-8"));
            
            
            //write without POS
              for (String ngPlain : finalNgs.keySet()) {
                  b.write(ngPlain + " " + finalNgs.get(ngPlain) + "\n");
              }
              
              b.close();
              
        }
        
        
        
        
        
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        System.out.println("Latency: "+ (endTime-startTime));
        
    }

}
