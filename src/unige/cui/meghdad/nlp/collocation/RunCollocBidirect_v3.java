/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unige.cui.meghdad.nlp.collocation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import unige.cui.meghdad.toolkit.Tools;

/**
 * 
 * Program for executing Collocational_Prob_Bidirect_v6. 
 * It reads the command line arguments 
 * and required files and generates data
 * structures required by Collocational_Prob_Bidirect_v6. 
 * 
 * @author Meghdad Farahmand
 *
 *
 * New in this version:
 *
 * reads corpus and extracts unigrams, bigrams and candidates directly from 
 * POS tagged corpus. A POS tagged corpus must be provided by means of 
 * -p2corpus flag. 
 * -ignoreCase, -mwePattern options added. 
 *
 *
 */
public class RunCollocBidirect_v3 {

    public static void main(String[] args) throws IOException, ParseException {
//
//        System.out.println();
//        System.out.println("--- RunBidirect_v2_corpusInput ---");
//        System.out.println();
        
        
        //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\\\//\\//\\//
        ////\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

        //                      COMMAND LINE ARGUMENTS
         
        

        //create an object of type tools for later usage
        Tools T = new Tools();

        //using apache commons CLI to parse command line arguments
        
        // create Options object
        Options options = new Options();

        //required options:
        options.addOption("p2corpus", true, "Path 2 corpus");
        
         options.addOption("isPosTagged", true, "Is the input corpus postagged?");
        
        //optional options
        options.addOption("alpha", true, "alpha paramter");
        options.addOption("evalMode", true, "evaluation or extractin mode?");

        options.addOption("bigramTh", true, "Threshold on frequency of bigrams to be extracted by ExtractNgrams");
        options.addOption("wordTh", true, "Threshold on frequency of unigrams to be extracted by LexExtract");

        options.addOption("mwePattern", true, "MWE pattern");
        options.addOption("ignoreCase", true, "ignore case");
        

        //required only in eval mode:
        options.addOption("p2e", true, "Path 2 labeled examples");
        options.addOption("labels", true, "Path 2 file containing labels");
        options.addOption("maxAlpha", true, "maximum value of alpha");
        options.addOption("minAlpha", true, "minimum value of alpha");
        options.addOption("step", true, "steps (for eval mode)");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        //initialize options to default values
        int bigramTh = 3;
        int wordTh = 3;
        if (cmd.hasOption("bigramTh")) {
            bigramTh = Integer.parseInt(cmd.getOptionValue("bigramTh"));
        }
        if (cmd.hasOption("wordTh")) {
            wordTh = Integer.parseInt(cmd.getOptionValue("wordTh"));
        }
        String mwePat = "nn-nn";
        if (cmd.hasOption("mwePattern")) {
            mwePat = cmd.getOptionValue("mwePattern");
        }
        boolean ignoreCase = true;
        if (cmd.hasOption("ignoreCase")) {
            if (cmd.getOptionValue("ignoreCase").equals("1")) {
                ignoreCase = true;
            } else if (cmd.getOptionValue("ignoreCase").equals("0")) {
                ignoreCase = false;
            } else {
                System.out.println("-ignorecase can only have 0 and 1 values.");
            }
        }
        
         boolean isPosTagged = true;
        if (cmd.hasOption("isPosTagged")) {
            if (cmd.getOptionValue("isPosTagged").equals("0")) {
                isPosTagged = false;
            }
        }
        

        //Default value for evalMode 
        //check if evalMode and its required flags are correctly entered
        boolean isEval = false;
        if (cmd.hasOption("evalMode")) {
            if (cmd.getOptionValue("evalMode").equals("1")) {
                isEval = true;

                if (!cmd.hasOption("labels")
                        || !cmd.hasOption("maxAlpha")
                        || !cmd.hasOption("minAlpha")
                        || !cmd.hasOption("p2e")
                        || !cmd.hasOption("step")) {
                    System.err.println("Error: in evaluation mode path to examples (-p2e) path to labels (-labels) -maxAlpha -minAlpha and -step must be set.");
                    return;
                }

            } else if (cmd.getOptionValue("evalMode").equals("0")) {
                isEval = false;

            } else {
                System.out.println("evalMode can only have 0 and 1 values");
                return;
            }
        }

        if (isEval) {
            System.out.println();
            System.out.println("--- Evaluation mode ---");
            System.out.println();
        } else {
            System.out.println();
            System.out.println("--- Extraction mode ---");
            System.out.println();
        }

        if (cmd.hasOption("p2corpus")) {
            System.out.println("Path to corpus: " + cmd.getOptionValue("p2corpus"));
        } else {
            System.out.println("Error: please enter a valid path 2 pos-tagged corpus using -p2corpus option");
            return;
        }

        Double alpha = 2.0;
        if (cmd.hasOption("alpha")) {
            alpha = Double.parseDouble(cmd.getOptionValue("alpha"));
        }

        
        //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\\\//\\//\\//
        ////\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

        
        

        //create candidate list
        HashMap<String, String> posCandidMap = new HashMap<String, String>();

        //if in eval mode, candidate list becomes the labeled examples provided by the user.  
        if (isEval) {

            System.out.println("Path to examples: " + cmd.getOptionValue("p2e"));
            System.out.println("Path to labels: " + cmd.getOptionValue("labels"));

            //reading list of candidates and their annotations
            BufferedReader candidateFile = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(cmd.getOptionValue("p2e")), "UTF8"));

            BufferedReader labels = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(cmd.getOptionValue("labels")), "UTF8"));
            
            String Entry = "";
            String lbl = "";
            Pattern entryETlabel = Pattern.compile("(\\w+_\\w+\\s\\w+_\\w+)$");
            while ((Entry = candidateFile.readLine()) != null) {

                lbl = labels.readLine();

                Matcher entryETlabelM = entryETlabel.matcher(Entry);

                if (entryETlabelM.find()) {
//                System.out.println(entryETlabelM.group(1)+" -- > annotated as: " +lbl);
                    posCandidMap.put(entryETlabelM.group(1), lbl);
                }
            }
            candidateFile.close();
            labels.close();
            //System.out.println("Hashmap of candidates for evaluation constructed.");

            
            //if not in eval mode, candidate list being automatically generated 
            //with respect to mwePat
        } else {

            //create a candidate list:
            System.out.println();
            System.out.println("Creating a candidate set (pattern: " + mwePat + ")...");
            
            posCandidMap = T.extractNCs(cmd.getOptionValue("p2corpus"), mwePat, ignoreCase,true,10);
            
            //Transforming frequencies to -1:
            //posCandidMap is originally a map of candidates and their labels but extractNCs
            //returns a set of NCs and their frequencies. We only need the NCs 
            //and their frequency doesn't matter here
            for (String s : posCandidMap.keySet()) {
                posCandidMap.put(s, "-1");
            }
//            System.out.println();
//            System.out.println("Hashmap of candidates for extraction constructed");
        }


        //create lexicon and set of bigrams
        System.out.println();
        System.out.println("Extracting words and bigrams from corpus (frequency thresholds: words=" + wordTh + ", bigrams=" + bigramTh + ")...");

        List<HashMap<String, Integer>> lexPlainLexPos = T.ExtractLex(cmd.getOptionValue("p2corpus"), wordTh, true, ignoreCase);
        HashMap<String, Integer> plainLexMap = lexPlainLexPos.get(0);

        
        
        HashMap<String,Integer> plainBigramMap = T.ExtractNgrams(cmd.getOptionValue("p2corpus"), bigramTh, 2, isPosTagged,false,ignoreCase);
        
        
        System.out.println("Number of word types: " + plainLexMap.size());
        System.out.println("Number of bigram types: " + plainBigramMap.size());

        //create an instance of Collocational_Prob_Bidirect_v7
        Collocational_Prob_Bidirect_v7 iopobOnedirect = new Collocational_Prob_Bidirect_v7(posCandidMap, plainLexMap, plainBigramMap);

        //if in eval mode evaluate the model on set of labeled examples (provided by the user)
        //for different values of alpha
        if (isEval) {

            System.out.println();
            System.out.println("Precision, Recall and F1 score for the selected range of alpha values:");
            DecimalFormat df = new DecimalFormat("0.00");
            for (double j = Double.parseDouble(cmd.getOptionValue("maxAlpha"));
                    j >= Double.parseDouble(cmd.getOptionValue("minAlpha"));
                    j -= Double.parseDouble(cmd.getOptionValue("step"))) {
                double[] precAndRec = iopobOnedirect.evaluate(j, ignoreCase);
                System.out.println("prec: " + df.format(precAndRec[0]) + "\trec: " + df.format(precAndRec[1]) + "\tF1: " + df.format(precAndRec[2]));
                //System.out.println(j + "\t" + df.format(precAndRec[0]) + "\t" + df.format(precAndRec[1]) + "\t" + df.format(precAndRec[2]) + "\t" + precAndRec[3]);
            }
            System.out.println("-----------------------------------------------------------------");
            System.out.println("Number of examples: " + posCandidMap.size());
            System.out.println("Number of processed examples: " + iopobOnedirect.getNumProcessed());
            System.out.println("Number of examples discarded due to unavailability of synonymy or lexical information: " + (posCandidMap.size() - iopobOnedirect.getNumProcessed()));

        //if in extraction mode extract a set of collocations
        } else {

        //retrieve the path of the parent of the directory that contains class.
            //File f1 = new File(Tools.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getParentFile();
            File f1 = new File(Tools.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
        //creating output directory in the retrieved path. 
            File outDir = new File(f1.getPath() + "/collocation-results");
            outDir.mkdir();
        //create the output file
            Writer b = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outDir + "/" + mwePat + "-collocations-alpha-" + alpha + ".txt"), "UTF-8"));

            Set<String> extractionResults = iopobOnedirect.extract(alpha, ignoreCase);
            //write the results:
            for (String s : extractionResults) {
                b.write(s + "\n");
                //System.out.println(s);
            }
            System.out.println("Results were stored in: " + outDir + "/" + mwePat + "-collocations-alpha-" + alpha + ".txt");
            System.out.println();
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("Number of candidates: " + posCandidMap.size());
            System.out.println("Number of processed candidates: " + iopobOnedirect.getNumProcessed());
            System.out.println("Number of candidates discarded due to unavailability of synonymy or lexical information: " + (posCandidMap.size() - iopobOnedirect.getNumProcessed()));
            System.out.println("Number of collocations identified by bidirectional model: " + extractionResults.size());
            System.out.println("-----------------------------------------------------------------------");
            System.out.println();
            
        }
    }
}
