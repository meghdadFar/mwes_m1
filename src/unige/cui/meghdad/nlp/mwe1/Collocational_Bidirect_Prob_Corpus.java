/* 
 * Copyright (C) 2016 Meghdad Farahmand<meghdad.farahmand@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package unige.cui.meghdad.nlp.mwe1;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import static java.util.Collections.list;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
 * Collocational_Bidirect_Prob_Corpus implements the collocation identification
 * method of Meghdad Farahmand and Joakim Nivre 2015: "Modeling the Statistical
 * Idiosyncrasy of Multiword Expressions."
 *
 *
 * @author Meghdad Farahmand
 * @since 6.7.2016
 * @version 9
 *
 *
 *
 */
public class Collocational_Bidirect_Prob_Corpus {

    //============= fields ========================
    private LinkedHashMap<String, Integer> posCandList;
    private HashMap<String, Integer> unigramHM;
    private HashMap<String, Integer> bigramHM;
    private int numOfProcessed = 0; //number of elements that could be processed 

    //============= class constructor =============
    public Collocational_Bidirect_Prob_Corpus(LinkedHashMap<String, Integer> posLabeledCandidates, HashMap<String, Integer> lexMap, HashMap<String, Integer> bigramsMap) {

        posCandList = posLabeledCandidates;
        unigramHM = lexMap;
        bigramHM = bigramsMap;

    }

    //getter for numOfProcessed
    public int getNumProcessed() {
        return numOfProcessed;
    }

    //================================================================= 
    //=================================================================
    /**
     * Method to return p(w1|w2), p(w2|w1), p(w1|syn(w2)) and p(w2|syn(w1)) for
     * posLabeledCandidates.
     *
     *
     * @param igCase ignore case
     *
     * @return LinkedHashMap<String,Double[]> that contains NCs and their
     * p(w1|w2), p(w2|w1), p(w1|syn(w2)) and p(w2|syn(w1)).
     *
     * @throws IOException
     */
    //================================================================= 
    //=================================================================
    public LinkedHashMap<String, Double[]> calculateSynBasedProbs_v2(boolean igCase) throws IOException {

        numOfProcessed = 0;
        DecimalFormat df = new DecimalFormat("0.0000");

        LinkedHashMap<String, Double[]> results = new LinkedHashMap();

        //for each NC (candidate noun compound) do:
        for (String NCetTags : posCandList.keySet()) {

            //System.out.println(NCetTags);
            //declare and init numerator and denominator of the left side of inequality P(w2|w1) > P(w2|Synset(w1))
            double Lnumw2w1 = 0;
            double Ldenumw2w1 = 0;
            //declare and init numerator and denominator of the left side of inequality P(w1|w2) > P(w1|Synset(w2))
            double Lnumw1w2 = 0;
            double Ldenumw1w2 = 0;

            //P(w2|w1)
            double PW2W1 = 0;
            //P(w2|Synset(w1))
            double Pw2SynsetW1 = 0;

            //P(w1|w2)
            double PW1W2 = 0;
            //P(w1|Synset(w2))
            double Pw1SynsetW2 = 0;

            if (igCase) {
                NCetTags = NCetTags.toLowerCase();
            }
            //NPetTags format: Labour_NNP Party_NNP
            //split this postagged bigram into its components and their tags
            String[] WsTs = NCetTags.split(" ");
            String[] w1t1 = WsTs[0].split("_");
            String[] w2t2 = WsTs[1].split("_");

            //create untagged bigram
            String NC = w1t1[0] + " " + w2t2[0];
            /*
             calculate PW1W2 and PW2W1.
             lll is flag to show wether or not p(w2|w1) and p(w1|w2) could be created. i.e., w1 and w2 
             exist in the unigramHM and w1w2 exists in the bigramHM.
             */
            boolean lll = false;
            if (unigramHM.containsKey(w1t1[0])
                    && unigramHM.containsKey(w2t2[0])) {
                Lnumw2w1 = posCandList.get(NCetTags);
                Lnumw1w2 = Lnumw2w1;

                Ldenumw2w1 = unigramHM.get(w1t1[0]);
                Ldenumw1w2 = unigramHM.get(w2t2[0]);

                if ((Lnumw2w1 <= Ldenumw2w1) && (Lnumw1w2 <= Ldenumw1w2)) {
                    /*
                     maximum likelihood estimation for P(w2|w1) and P(w1|w2)
                     smoothing:
                     PW2W1 = (double) (Lnumw2w1+1.0) / (double) (Ldenumw2w1 + bigramHM.size());
                     PW1W2 = (double) (Lnumw1w2+1.0) / (double) (Ldenumw1w2 +bigramHM.size());
                     */
                    //w/o smoothing
                    PW2W1 = (double) (Lnumw2w1) / (double) (Ldenumw2w1);
                    PW1W2 = (double) (Lnumw1w2) / (double) (Ldenumw1w2);

                    lll = true;

                } else {

                    /*
                     (B)
                     The numerator of PW2W1 or PW1W2 was greater than its denominator.
                     This could have happen due to regex inconsistencies while retrieving 
                     words and candidates. For instance one problematic example was:
                    
                     hondt_nn method_nn
                    
                     It is extracted from: d'hondt_nn method_nn
                    
                     The nn-nn extractor implementation identifies it as a compound, 
                     but the unigram extractor does not identify hondt as a word 
                     (being part of d'hondt). 
                    
                     If such cases happen, in this if block nothing will be executed. 
                     In the else block flas lll will become false. 
                     */
                }
            } else {
                /*
                 Either (B) occured,
                 or either of w1 or w2 were not found in the corpus therefore 
                 division by zero due to unavailble denominator count occured.
                 */
                lll = false;
            }
            /*
             if the p(w2|w1) and p(w1|w2) could be calculated (lll==true), calculate p(w2|Synset(w1)) and P(w1|Synset(w2))
             */
            if (lll) {
                String categW1 = "";
                String categW2 = "";

                if (w1t1[1].startsWith("NN") || w1t1[1].startsWith("nn")) {
                    categW1 = "NOUN";
                } else if (w1t1[1].startsWith("JJ") || w1t1[1].startsWith("jj")) {
                    categW1 = "ADJECTIVE";
                } else if (w1t1[1].startsWith("VB") || w1t1[1].startsWith("vb")) {
                    categW1 = "VERB";

                    /* I map other categories such as RB to NN because there are only a few adverbs in WordNet
                     and there are a few adverbs in my data (e.g. back in back pain)
                     */
                } else {
                    categW1 = "NOUN";
                }

                if (w2t2[1].startsWith("NN") || w2t2[1].startsWith("nn")) {
                    categW2 = "NOUN";
                } else if (w2t2[1].startsWith("JJ") || w2t2[1].startsWith("jj")) {
                    categW2 = "ADJECTIVE";
                } else if (w2t2[1].startsWith("VB") || w2t2[1].startsWith("vb")) {
                    categW2 = "VERB";
                } else {

                    /* I map other categories such as RB to NN because there are only a few adverbs in WordNet
                     and there are a few adverbs in my data (e.g. back in back pain)
                     */
                    categW2 = "NOUN";
                }

                SynsetfromWN OBJ1 = new SynsetfromWN();
                //W1synonyms does not contain W1
                List<String> W1synonyms = OBJ1.ReturnSyns(w1t1[0], categW1);
                //W2synonyms does not contain W2
                List<String> W2synonyms = OBJ1.ReturnSyns(w2t2[0], categW2);

                double pw1Synw2 = 0;
                double pw2Synw1 = 0;

                /*
                 Continue the calculation of the probabilities if synsets are not empty. 
                 If they are empty, for this compound, this method is not conclusive. 
                 Therefore the NC will be discarded. 
                 */
                if ((W1synonyms.size() > 0) && (W2synonyms.size() > 0)) {

                    
                    /*
                    Use of W1/2synFoundInCorpus:
                    Were at least one of the synonyms of W1 and W2 found in the corpus?
                    If not, the method is not able to calculate the 
                    non-substitutability score for this NC. 
                    */
                    boolean W1synFoundInCorpus = false;
                    boolean W2synFoundInCorpus = false;
                    //=============================================
                    //============= P(w2|Synset(w1)) ==============
                    //=============================================
                    double numW2SynW1 = 0;
                    double denumW2SynW1 = 0;
                    for (String o : W1synonyms) {
                        String compound = o + " " + w2t2[0];
                        if (bigramHM.containsKey(compound) && unigramHM.containsKey(o)) {
                            denumW2SynW1 += (double) unigramHM.get(o);
                            numW2SynW1 += (double) bigramHM.get(compound);
                        }
                    }
                    if (denumW2SynW1 != 0) {
                        pw2Synw1 = numW2SynW1 / denumW2SynW1;
                        W1synFoundInCorpus = true;
                    } else 

                    //=============================================
                    //============= P(w1|Synset(w2)) ==============
                    //=============================================
                    numW2SynW1 = 0;
                    denumW2SynW1 = 0;
                    for (String o : W2synonyms) {
                        String compound = w1t1[0] + " " + o;
                        if (bigramHM.containsKey(compound) && unigramHM.containsKey(o)) {
                            denumW2SynW1 += (double) unigramHM.get(o);
                            numW2SynW1 += (double) bigramHM.get(compound);
                        }
                    }
                    if (denumW2SynW1 != 0) {
                        pw1Synw2 = numW2SynW1 / denumW2SynW1;
                        W2synFoundInCorpus = true;
                    } 
                    //=============================================
                    //=============================================

                    if(W1synFoundInCorpus && W2synFoundInCorpus){
                        Double[] tmpArray = {PW1W2, PW2W1, pw1Synw2, pw2Synw1};
                        results.put(NC, tmpArray);
                    }else{
                        /*
                        None of the synonyms of W1 and W2 were observed in the corpus.
                        Thereofre, the non-substitutability score cannot be computed. 
                        Thus, this NC will be discarded. 
                        */
                    }

                } else {
                    /*
                     WN synsets was not found for either w1 or w2, therefore, this method 
                     can not generate a score and sbsequently a rank for this NC. 
                     This NC will be discarded and will not be added to the results. 
                     */
                }

            } else {
                /*
                 PW2W1 or PW1W2 could not be calculated for this NC. 
                 */

                /*
                 Uncomment the following if all of the elements of the candidate set 
                 need to be present. The following statement assign probabilities such that after
                 being ranked, these elements (for which PW2W1 or PW1W2) could not be calculated
                 stay at the bottom of the ranked list. 
                 */
//                Double[] tmpArray = {-5.0, -5.0, 0, 0};
//                results.put(NC, tmpArray);
            }
        }
        return results;
    }

    public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException, ParseException {

        //======================================================================
        //======================  COMMAND LINE ARGUMENTS =======================
        //======================================================================
        
        //use apache commons CLI to parse command line arguments
        // create Options object
        Options options = new Options();

        //required options:
        options.addOption("p2corpus", true, "Path 2 POS tagged corpus.");

        //optional options:
        options.addOption("rc", true, "Ranking criteria: delta_12, delta_21, or combined.");
        options.addOption("maxRank", true, "Return MWEs up to this rank.");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        //initialize options to default values and check required options are set
        if (!cmd.hasOption("p2corpus")) {
            System.out.println("Path to the POS tagged corpus must be set.");
        }

        String rc = "21";
        if (cmd.hasOption("rc")) {
            rc = cmd.getOptionValue("rc");
        }
        int maxRank = 200;
        if (cmd.hasOption("maxRank")) {
            rc = cmd.getOptionValue("maxRank");
        }
        //======================================================================
        //======================================================================
        
        String p2corpus = cmd.getOptionValue("p2corpus");

        Tools T = new Tools();

        System.out.println("Extracting a set of \"nn-nn\" candidates...");
        LinkedHashMap<String, Integer> posCandidates = new LinkedHashMap<String, Integer>(T.extractNCs(p2corpus, "nn-nn", true, true, 50));
        //TODO keep only the candiddates whose components that have at least one synonym in wordnet

        System.out.println("Extracting 1-grams...");
        HashMap<String, Integer> unigrams = T.ExtractUnigram(p2corpus, 1, true, true).get(0);

        System.out.println("Extracting 2-grams...");
        HashMap<String, Integer> bigrams = T.ExtractNgrams(p2corpus, 1, 2, true, false, true);

        System.out.println("Calculating bidirectional synset-baset probabilities...");
        Collocational_Bidirect_Prob_Corpus cpb = new Collocational_Bidirect_Prob_Corpus(posCandidates, unigrams, bigrams);

        LinkedHashMap<String, Double[]> r = cpb.calculateSynBasedProbs_v2(false);

        DecimalFormat df = new DecimalFormat("0.000");

        //calculate the score:
        HashMap<String, Double> scores = new HashMap<String, Double>();
        if (rc.equals("12")) {
            for (String s : r.keySet()) {
                scores.put(s, (r.get(s)[0] - r.get(s)[2]));
            }
        } else if (rc.equals("21")) {
            for (String s : r.keySet()) {
                scores.put(s, (r.get(s)[1] - r.get(s)[3]));
            }
        } else if (rc.equals("hybrid")) {
            for (String s : r.keySet()) {
                scores.put(s, (r.get(s)[0] - r.get(s)[2]) * (r.get(s)[1] - r.get(s)[3]));
            }
        }

        //sort (descending) candidates by their score:
        List<Entry<String, Double>> entryList = new ArrayList<Entry<String, Double>>(scores.entrySet());

        Collections.sort(entryList, new Comparator<Entry<String, Double>>() {
            @Override
            public int compare(Entry<String, Double> e1,
                    Entry<String, Double> e2) {
                return -1 * e1.getValue().compareTo(e2.getValue());
            }
        });

        //print the results:
        System.out.println("Ranking the candidates...\n");
        System.out.println("Returning the ranked candidates for which WordNet synsets were available.");
        int rank = 0;
        for (Entry<String, Double> e : entryList) {
            rank++;
            System.out.println(e.getKey() + " " + df.format(e.getValue()));
            if (rank >= maxRank) {
                break;
            }
        }
    }
}
