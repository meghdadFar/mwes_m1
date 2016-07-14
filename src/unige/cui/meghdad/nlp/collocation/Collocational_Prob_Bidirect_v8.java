package unige.cui.meghdad.nlp.collocation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import static java.util.Collections.list;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collocational_Prob_Bidirect_v8 implements the collocation identification
 * method of Meghdad Farahmand and Joakim Nivre 2015: "Modeling the Statistical
 * Idiosyncrasy of Multiword Expressions."
 *
 * @author Meghdad Farahmand
 * @since 6.7.2016
 * @version 8
 *
 *
 * Class for generating P(w1|Synset(w2)) and P(w2|Synset(w1))
 *
 * Version 8 only requires the corpus as input and unlike previous versions
 * bigrams, unigrams and candidates are being generated form corpus. Also case
 * sensitivity has been added as an option in this version.
 */
public class Collocational_Prob_Bidirect_v8 {

    //============= fields ========================
    private LinkedHashMap<String,Integer> posCandList;
    private HashMap<String, Integer> unigramHM;
    private HashMap<String, Integer> bigramHM;
    private int numOfProcessed = 0; //number of elements that could be processed 

    //============= class constructor =============
    public Collocational_Prob_Bidirect_v8(LinkedHashMap<String,Integer> posLabeledCandidates, HashMap<String, Integer> lexMap, HashMap<String, Integer> bigramsMap) {

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
     * @return LinkedHashMap<String,Double[]> that contains NCs and their p(w1|w2), p(w2|w1),
     * p(w1|syn(w2)) and p(w2|syn(w1)).
     *
     * @throws IOException
     */
    //================================================================= 
    //=================================================================
    public LinkedHashMap<String,Double[]> calculateSynBasedProbs(boolean igCase) throws IOException {

        //initializing confusion matrix elements and return values: precision, recall, etc. 
        numOfProcessed = 0;
        DecimalFormat df = new DecimalFormat("0.0000");

        
        
        LinkedHashMap<String,Double[]> results = new LinkedHashMap();
        
        
        

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
            //System.out.println(NC);

            /*
             calculate PW1W2 and PW2W1.
             lll is flag to show wether or not p(w2|w1) and p(w1|w2) could be created. i.e., w1 and w2 
             exist in the unigramHM and w1w2 exists in the bigramHM.
             */
            boolean lll = false;
            if (unigramHM.containsKey(w1t1[0])
                    && unigramHM.containsKey(w2t2[0])) {
//                System.out.println(NC+" was numOfProcessed in bigram list");
//                System.out.println(w1t1[0]+" was numOfProcessed in unigram list");

                Lnumw2w1 = posCandList.get(NCetTags);
                Lnumw1w2 = Lnumw2w1;

                Ldenumw2w1 = unigramHM.get(w1t1[0]);
                
                Ldenumw1w2 = unigramHM.get(w2t2[0]);
                //maximum likelihood estimation for P(w2|w1) and P(w1|w2)
                PW2W1 = (double) (Lnumw2w1+1.0) / (double) (Ldenumw2w1 + bigramHM.size());
                PW1W2 = (double) (Lnumw1w2+1.0) / (double) (Ldenumw1w2 +bigramHM.size());



                lll = true;

            } else {

                /*
                 either of w1 or w2 was not found in the corpus therefore 
                 division by zero due to unavailble denominator count.
                 */
                lll = false;

            }
            /*
             if the p(w2|w1) and p(w1|w2) could be calculated, calculate p(w2|Synset(w1)) and P(w1|Synset(w2))
             */
            //
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
                }else{
                    categW1 = "NOUN";
                }

                if (w2t2[1].startsWith("NN") || w2t2[1].startsWith("nn")) {
                    categW2 = "NOUN";
                } else if (w2t2[1].startsWith("JJ") || w2t2[1].startsWith("jj")) {
                    categW2 = "ADJECTIVE";
                } else if (w2t2[1].startsWith("VB") || w2t2[1].startsWith("vb")) {
                    categW2 = "VERB";
                }else{
                    
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

                //========================================                
                //============ TEST BLOCK ===============
                //========================================
//                if(NC.equals("sliced onions")){
//                    System.out.println(NC+ " "+ "w1t1[1]: "+w1t1[1]+" --> "+categ);
//                    System.out.println("-----------------------");
//                    System.out.println("No synonyms numOfProcessed for: "+w1t1[0]+ " category: "+categ);
//                }
                //========================================
                //=========== END OF TEST ================
                //========================================
                double w2w1RnumSigma = 0;
                double w2w1RdenumSigma = 0;

                double w1w2RnumSigma = 0;
                double w1w2RdenumSigma = 0;

                //=============================================
                //===== num and denum of  P(w2|Synset(w1)) ====
                //=============================================
                for (String o : W1synonyms) {
                    String compound = o + " " + w2t2[0];
                    if (bigramHM.containsKey(compound)) {
                        w2w1RnumSigma += bigramHM.get(compound);
                        //smoothing
                        w2w1RnumSigma++;
                        
                    } else {
                    }
                    if (unigramHM.containsKey(o)) {
                        w2w1RdenumSigma += unigramHM.get(o);
                    } else {
                        //it's possible the synonym is not a unigram
//                        System.out.println(o+" not numOfProcessed in unigrams");
                    }
                }
                //=====================================================
                //========= num and denum of  P(w1|Synset(w2)) ========
                //=====================================================
                for (String o : W2synonyms) {
                    String compound = w1t1[0] + " " + o;
                    if (bigramHM.containsKey(compound)) {
                        w1w2RnumSigma += bigramHM.get(compound);
                        //smoothing
                        w1w2RnumSigma++;
                    } else {
                    }
                    if (unigramHM.containsKey(o)) {
                        w1w2RdenumSigma += unigramHM.get(o);
                    } else {
                        //it's possible the synonym is not a unigram
//                        System.out.println(o+" not numOfProcessed in unigrams");
                    }
                }
                //====================================================
                //====================================================
                //====================================================

               // if ((w2w1RdenumSigma != 0) && (w1w2RdenumSigma !=0)) {
                    //numOfProcessed++;
                    //without smoothing:
//                    double RIGHTw2w1 = w2w1RnumSigma / w2w1RdenumSigma;
//                    Pw2SynsetW1 = RIGHTw2w1;
//                    double RIGHTw1w2 = w1w2RnumSigma / w1w2RdenumSigma;
//                    Pw1SynsetW2 = RIGHTw1w2;
                    //add one smoothing:
                
                
                
////                
//                System.out.println("w2w1RnumSigma "+w2w1RnumSigma);
//                System.out.println("w2w1RdenumSigma " + w2w1RdenumSigma);
//                System.out.println("bigramHM.size() "+ bigramHM.size());
//                
//                System.out.println("w1w2RnumSigma "+ w1w2RnumSigma);
//                System.out.println("w1w2RdenumSigma"+ w1w2RdenumSigma);
//                System.out.println("--------------------");
                
                

                
  
                //System.out.println(NC+" "+ncCount + " "+ w1Count + " "+w2Count);
                Pw1SynsetW2 = (double) (w1w2RnumSigma) / (double) (w1w2RdenumSigma + bigramHM.size());
                Pw2SynsetW1 = (double) (w2w1RnumSigma) / (double) (w2w1RdenumSigma + bigramHM.size());

                

                //System.out.println(NC + " " + PW1W2 + " " + PW2W1 + " " + Pw1SynsetW2 + " " + Pw2SynsetW1);

                Double[] tmpArray = {PW1W2,PW2W1,Pw1SynsetW2,Pw2SynsetW1};
                results.put(NC, tmpArray);
                
               // }
            } else {
                /*
                 w1 or w2 did not exist in the unigramHM for this NC
                 */

                //System.out.println(NC + " -1 -1 -1 -1");
                Double[] tmpArray = {-1.0,-1.0,-1.0,-1.0};
                results.put(NC, tmpArray);
            }
        }
        return results;
    }
    
    
    
    
    
    
    public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException{

            //reading list of candidates
        System.out.println("Readin candidates...");
            BufferedReader candidateFile = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream("/Users/svm/Resources/DATA/WN-Syn-Based/eval_farahmand/eval_instances_pos_avail.csv"), "UTF8"));
            LinkedHashMap<String,Integer> posCandLinkedMap = new LinkedHashMap<>();
            String Entry = "";
            Pattern entryETlabel = Pattern.compile("(\\w+_\\w+\\s\\w+_\\w+)\\s(\\d+)$");
            while ((Entry = candidateFile.readLine()) != null) {

                Matcher entryETlabelM = entryETlabel.matcher(Entry);

                if (entryETlabelM.find()) {
                    posCandLinkedMap.put(entryETlabelM.group(1),Integer.parseInt(entryETlabelM.group(2)));
                }
            }
            
            
        //reading unigrams
        System.out.println("Reading unigrams...");
        BufferedReader allunigrams = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream("/Users/svm/Resources/corpora/WIKI-2015/lex/lex-gte-1.txt"), "UTF8"));
        
        
        HashMap<String, Integer> unigramsMap = new HashMap<String, Integer>();
        Pattern puni = Pattern.compile("^(\\w+)\\s+(\\d+)$");
        String uni = "";
        while ((uni = allunigrams.readLine()) != null) {
            Matcher pm1 = puni.matcher(uni);
            if (pm1.find()) {
                unigramsMap.put(pm1.group(1), Integer.parseInt(pm1.group(2)));
            }
        }
        allunigrams.close();
        
        //reading bigrams
        //====================================================================
        //=================== reading list of bigrams ========================
        //====================================================================
        System.out.println("Reading bigrams...");
        //Pattern pbi = Pattern.compile("^(\\d+)\\s+(.+)$");
        
        //railway_NN station_NN	21332
//        Pattern pbi = Pattern.compile("^(\\w+)_(\\w+)\\s(\\w+)_(\\w+)\\s(\\d+)$");
        
        //railway station 21332
        Pattern pbi = Pattern.compile("^(\\w+)\\s(\\w+)\\s(\\d+)$");
        
        BufferedReader allBigrams = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream("/Users/svm/Resources/corpora/WIKI-2015/2-grams/2-grams-gte-1.txt"), "UTF8"));

        HashMap<String, Integer> bigramMap = new HashMap<String, Integer>();
        String bi = "";
        
        while ((bi = allBigrams.readLine()) != null) {
            Matcher pm2 = pbi.matcher(bi);
            if (pm2.find()) {                
                bigramMap.put(pm2.group(1)+" "+pm2.group(2), Integer.parseInt(pm2.group(3)));
            }
        }
        allBigrams.close();
        //====================================================================
        //====================================================================
        //====================================================================
        
            
        

        System.out.println("calculateSynBasedProbs...");
        Collocational_Prob_Bidirect_v8 cpb = new Collocational_Prob_Bidirect_v8(posCandLinkedMap, unigramsMap, bigramMap);
        
        
        LinkedHashMap<String,Double[]> r = cpb.calculateSynBasedProbs(true);
        
        
        DecimalFormat df = new DecimalFormat("0.0000000");
        for(String s : r.keySet()){
            //System.out.println(s+" "+df.format(r.get(s)[0]) + " "+df.format(r.get(s)[1])+" "+df.format(r.get(s)[2])+" "+df.format(r.get(s)[3]));
            System.out.println(df.format(r.get(s)[0]) + " "+df.format(r.get(s)[1])+" "+df.format(r.get(s)[2])+" "+df.format(r.get(s)[3]));
            
        }
        
                
    }

}
