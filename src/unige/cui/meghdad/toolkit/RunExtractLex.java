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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *Executes ExtractLex() method from Class Tools. 
 *Defines and accepts several command line options. 
 * 
 * 
 *@author Meghdad Farahmand
 * 
 * 
 */
public class RunExtractLex {
    
    public static void main(String[] args) throws ParseException, FileNotFoundException, IOException, URISyntaxException {
        
        System.out.println();
        System.out.println("--- RunExtractLex ---");
        System.out.println();

        ////////////////////////////////////////////////////////////////
        //Command line options using apache commons CLI 
        ////////////////////////////////////////////////////////////////
        
        // create Options object
        Options options = new Options();
        
        //required options
        options.addOption("p2corpus", true, "Path 2 Corpus");
        
        //optional options
        options.addOption("p2outdir", true, "Path 2 Output Directory");
        options.addOption("isPosTagged", true, "Is the input corpus postagged?");//default 1
        options.addOption("freqTh", true, "Threshold on frequency of to be extracted words");//default 1
        options.addOption("ignoreCase", true, "Whether or not ignore case");//default 1
        
        
        
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        
        //create errors and required options
        if (cmd.hasOption("p2corpus")) {
            System.out.println("Reading corpus from: " + cmd.getOptionValue("p2corpus"));
        } else {
            System.out.println("Error: Path to corpus (-p2corpus) not set.");
            return;
        }
        
        //set default option values:
        
        //get the parent of the class path, initialize path2output with it
        File f1 = new File(Tools.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getParentFile();
        String  path2output = f1.getPath();
        if (cmd.hasOption("p2outdir")) {
            path2output = cmd.getOptionValue("p2outdir");
        }
        
        
        
        boolean isPosTagged = true;
        if(cmd.hasOption("isPosTagged")){
            if(cmd.getOptionValue("isPosTagged").equals("0")){
                isPosTagged = false;
            }   
        }
        int freqTh = 1;
        if(cmd.hasOption("freqTh")){
                freqTh = Integer.parseInt(cmd.getOptionValue("freqTh"));
        }
        boolean ignoreCase = true;
        if(cmd.hasOption("ignoreCase")){
            if(cmd.getOptionValue("ignoreCase").equals("1")){
                ignoreCase = true;
            }else if(cmd.getOptionValue("ignoreCase").equals("0")){
                ignoreCase = false;
            }else{
                System.out.println("-ignorecase can only have 0 and 1 values.");
            }
        }
        ////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////
        
        //create output directory:
        File lexDir = new File(path2output + "/lex");
        lexDir.mkdir();
        System.out.println("Writing results in: "+lexDir);
        
        
        Tools T = new Tools();
        List<HashMap<String,Integer>> lexPlainLexPos = T.ExtractLex(cmd.getOptionValue("p2corpus"),freqTh, isPosTagged,ignoreCase);
        HashMap<String,Integer> lexPlain = lexPlainLexPos.get(0);
        HashMap<String,Integer> lexPos = lexPlainLexPos.get(1);
        
        
        if(isPosTagged){
            
            Writer b = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(lexDir + "/lex-gte-" + freqTh + ".txt"), "UTF-8"));
            //file that contains unigrams and their POS
            Writer b_Pos = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(lexDir + "/lex-pos-gte-" + freqTh + ".txt"), "UTF-8"));
            
            //write with POS
              for (String unigramEtPos : lexPos.keySet()) {
                    b_Pos.write(unigramEtPos + " " + lexPos.get(unigramEtPos) + "\n");
              }
              //write without POS
              for (String unigram : lexPlain.keySet()) {
                  b.write(unigram + " " + lexPlain.get(unigram) + "\n");
              }
              
              b_Pos.close();
              b.close();
            
        }else{
            
            Writer b = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(lexDir + "/lex-gte-" + freqTh + ".txt"), "UTF-8"));
            
            
            //write without POS
              for (String unigram : lexPlain.keySet()) {
                  b.write(unigram + " " + lexPlain.get(unigram) + "\n");
              }
              
              b.close();
            
        }
        
    }
    
}
