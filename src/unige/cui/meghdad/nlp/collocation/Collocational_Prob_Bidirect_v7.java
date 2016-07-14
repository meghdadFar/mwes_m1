
package unige.cui.meghdad.nlp.collocation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import static java.util.Collections.list;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collocational_Prob_Bidirect_v6 implements the collocation identification method of 
 * Meghdad Farahmand and Joakim Nivre 2015: "Modeling the Statistical Idiosyncrasy 
 * of Multiword Expressions." 
 *
 * @author Meghdad Farahmand
 * @since 1.7.2014
 * @version 7
 * 
 * 
 * Class for generating and validating P(w1|w2) > P(w1|Synset(w2)), 
 * and P(w2|w1) > P(w2|Synset(w1))
 * 
 * Version 7 only requires the corpus as input and unlike previous versions 
 * bigrams, unigrams and 
 * candidates are being generated form corpus.
 * Also case sensitivity has been added as an option in this version.  
 */


public class Collocational_Prob_Bidirect_v7 {

    //============= fields ========================
    private HashMap<String, String> PosLabeledMap;
    private HashMap<String, Integer> unigramHM;
    private HashMap<String, Integer> bigramHM;
    private int numOfProcessed = 0; //number of elements that could be processed 

    //============= class constructor =============
    public Collocational_Prob_Bidirect_v7(HashMap<String, String> posLabeledCandidates, HashMap<String, Integer> lexMap, HashMap<String, Integer> bigramsMap) {

        PosLabeledMap = posLabeledCandidates;
        unigramHM = lexMap;
        bigramHM = bigramsMap;

    }


    //getter for numOfProcessed
    public int getNumProcessed(){
        return numOfProcessed;
    }
    
    

    //================================================================= 
    //=================================================================
    /**
     * Method to evaluate the model with different values of parameter 
     * alpha. 
     * Used for parameter tuning and finding the right alpha. 
     * 
     * 
     * @param alpha
     * @param igCase ignore case
     * @return list that contains precision, recall,F1, and numOfProcessed respectively
     * 
     * @throws IOException 
     */
    //================================================================= 
    //=================================================================
  
    
    public double[] evaluate(double alpha,boolean igCase) throws IOException {

        //System.out.println();
        //System.out.println("--- evaluate method ---");
        System.out.println();
        System.out.println("Computing bidirectional model performance; Alpha parameter: "+alpha);

        
        
        //initializing confusion matrix elements and return values: precision, recall, etc. 
        numOfProcessed = 0;
        DecimalFormat df = new DecimalFormat("0.0000");
        
        double tp = 0;
        double tn = 0;
        double fp = 0;
        double fn = 0;

        double precision = 0;
        double recall = 0;
        double fallOut = 0;
        double F1 = 0;

        //for each NC (candidate noun compound) do:
        for (String NPetTags : PosLabeledMap.keySet()) {

            int label = Integer.parseInt(PosLabeledMap.get(NPetTags));
            int prediction = 0;
            
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
            
            

            if(igCase){
                NPetTags = NPetTags.toLowerCase();
            }
            //NPetTags format: Labour_NNP Party_NNP
            //split this postagged bigram into its components and their tags
            String[] WsTs = NPetTags.split(" ");
            String[] w1t1 = WsTs[0].split("_");
            String[] w2t2 = WsTs[1].split("_");

          
            //create untagged bigram
            String NP = w1t1[0]+ " " + w2t2[0];

            //flag to show wether or not the left side of the equation was successfully generated 
            boolean lll = false;
            if(unigramHM.containsKey(w1t1[0])
                    && unigramHM.containsKey(w2t2[0])){
//                System.out.println(NP+" was numOfProcessed in bigram list");
//                System.out.println(w1t1[0]+" was numOfProcessed in unigram list");
                if(bigramHM.containsKey(NP)){
                Lnumw2w1 = bigramHM.get(NP);
                }else{
                  Lnumw2w1=0;  
                }
                
                
                Ldenumw2w1 = unigramHM.get(w1t1[0]);
                
                Lnumw1w2 = Lnumw2w1;
                Ldenumw1w2 = unigramHM.get(w2t2[0]);
                //maximum likelihood estimation for P(w2|w1) and P(w1|w2)
                double LEFTw2w1 = (double) Lnumw2w1 / (double) Ldenumw2w1;
                double LEFTw1w2 = (double) Lnumw1w2 / (double) Ldenumw1w2;

                PW2W1 = LEFTw2w1;
                PW1W2 = LEFTw1w2;
                
                
                lll = true;
                
            }else{
                //not enough info to calculate P(W2|W1) and P(W1|W2)
                //(division by zero due to unavailble denominator count)
                lll = false;
                

            }
            //calculae P(w2|Synset(w1)) and P(w1|Synset(w2))
            if (lll) {
                String categW1 = "";
                String categW2 = "";

                if (w1t1[1].startsWith("NN")) {
                    categW1 = "NOUN";
                } else if (w1t1[1].startsWith("JJ")) {
                    categW1 = "ADJECTIVE";
                } else if (w1t1[1].startsWith("VB")) {
                    categW1 = "VERB";
                }
                
                if (w2t2[1].startsWith("NN")) {
                    categW2 = "NOUN";
                } else if (w2t2[1].startsWith("JJ")) {
                    categW2 = "ADJECTIVE";
                } else if (w2t2[1].startsWith("VB")) {
                    categW2 = "VERB";
                }
                
                
                SynsetfromWN OBJ1 = new SynsetfromWN();
                List<String> W1synonyms = OBJ1.ReturnSyns(w1t1[0], categW1);
                List<String> W2synonyms = OBJ1.ReturnSyns(w2t2[0], categW2);
                
//                System.out.println("synonyms for "+w1t1[0]+" : "+synonyms);
//                System.out.println("Decided Cat: "+categ);

                //========================================                
                //============ TEST SNIPPET ==============
                //========================================
//                if(NP.equals("sliced onions")){
//                    System.out.println(NP+ " "+ "w1t1[1]: "+w1t1[1]+" --> "+categ);
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
                    String compound = o+" "+w2t2[0];
                    if (bigramHM.containsKey(compound)) {
                        w2w1RnumSigma += bigramHM.get(compound);
//                        System.out.println("\t"+bigramHM.get(compound));
                    }else{
//                        System.out.println(compound+" not numOfProcessed in bigrams");
                    }
                    if (unigramHM.containsKey(o)) {
                        w2w1RdenumSigma += unigramHM.get(o);
//                        System.out.println("\t"+unigramHM.get(o));
                    }else{
                        //it's possible the synonym is not a unigram
//                        System.out.println(o+" not numOfProcessed in unigrams");
                    }
                }
                //=====================================================
                //====== num and denum of  P(w1|Synset(w2))============
                //=====================================================
                for (String o : W2synonyms) {
                    String compound = w1t1[0]+" "+o;
                    if (bigramHM.containsKey(compound)) {
                        w1w2RnumSigma += bigramHM.get(compound);
//                        System.out.println("\t"+bigramHM.get(compound));
                    }else{
//                        System.out.println(compound+" not numOfProcessed in bigrams");
                    }
                    if (unigramHM.containsKey(o)) {
                        w1w2RdenumSigma += unigramHM.get(o);
                    }else{
                        //it's possible the synonym is not a unigram
//                        System.out.println(o+" not numOfProcessed in unigrams");
                    }   
                }
                //====================================================
                //====================================================
                //====================================================
                
                
                
                
                
                if ((w2w1RdenumSigma != 0) && (w1w2RdenumSigma !=0)) {
                    
                    numOfProcessed++;

                    

                //smoothing numerator: w1w2RnumSigma denominator: bigramHM.size()
                Pw1SynsetW2 = (double) (w1w2RnumSigma) / (double) (w1w2RdenumSigma + bigramHM.size());
                Pw2SynsetW1 = (double) (w2w1RnumSigma) / (double) (w2w1RdenumSigma + bigramHM.size());
                
                
                
                
                //==================================================
                //=================== evaluation ===================
                //==================================================
                    if ((PW2W1 > alpha * Pw2SynsetW1) && (PW1W2 > alpha * Pw1SynsetW2)) {
                        prediction = 1;
                    } else if ( (PW2W1 <= alpha * Pw2SynsetW1) && (PW1W2 <= alpha * Pw1SynsetW2)) {
                        prediction = 0;
                    } 
                }else{
                    
                }
            }

            if (prediction == 1 && label == 1) {
                tp++;
            } else if (prediction == 0 && label == 0) {
                tn++;
            } else if (prediction == 0 && label == 1) {
                fn++;
            } else if (prediction == 1 && label == 0) {
                fp++;
            }

        }

        precision = tp / (tp + fp);
        recall = tp / (tp + fn);
        F1 = 2*(precision*recall)/(precision+recall);
        
        double[] finalResults = new double[]{precision, recall,F1,numOfProcessed};
        return finalResults;
    }
    
    
    //================================================================= 
    //=================================================================
    /**
     * Method to extract collocations.
     * 
     * 
     * @param alpha
     * @param igCase ignore case
     * @return Set of collocations 
     * 
     * @throws IOException 
     */
    //================================================================= 
    //=================================================================
    
    
    
    public Set extract(double alpha,boolean igCase) throws IOException {
        
//        System.out.println();
//        System.out.println("--- extract method ---");
//        System.out.println();
        System.out.println();
        System.out.println("Computing collocational weights of the candidates...");
        
 
        numOfProcessed = 0;
        //return Set
        Set<String> collocations = new HashSet();
        
        //for each selected NC
        for (String NPetTags : PosLabeledMap.keySet()) {
            
            if(igCase){
                NPetTags = NPetTags.toLowerCase();
            }
            
            
            int prediction = 0;

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

            

            //Labour_NNP Party_NNP 
            String[] WsTs = NPetTags.split(" ");
            String[] w1t1 = WsTs[0].split("_");
            String[] w2t2 = WsTs[1].split("_");

          
            
            String NP = w1t1[0]+ " " + w2t2[0];

            //flag to show wether or not the left side of the equation 
            //was successfully generated 
            boolean lll = false;
            if(unigramHM.containsKey(w1t1[0])
                    && unigramHM.containsKey(w2t2[0])){
//                System.out.println(NP+" was numOfProcessed in bigram list");
//                System.out.println(w1t1[0]+" was numOfProcessed in unigram list");
                
                if(bigramHM.containsKey(NP)){
                Lnumw2w1 = bigramHM.get(NP);
                }else{
                  Lnumw2w1=0;  
                }
                
                
                Ldenumw2w1 = unigramHM.get(w1t1[0]);
                
                Lnumw1w2 = Lnumw2w1;
                Ldenumw1w2 = unigramHM.get(w2t2[0]);

                //maximum likelihood estimation for P(w2|w1) and P(w1|w2)
                double LEFTw2w1 = Lnumw2w1 / Ldenumw2w1;
                double LEFTw1w2 = Lnumw1w2 / Ldenumw1w2;
                
                PW2W1 = LEFTw2w1;
                PW1W2 = LEFTw1w2;
                lll = true;
                
            }else{
                lll = false;
                //not enough info to calculate P(W2|W1) and P(W1|W2)
                //(division by zero due to unavailble denominator count)
            }
            //calculae P(w2|Synset(w1)) and P(w1|Synset(w2))
            if (lll) {
                String categW1 = "";
                String categW2 = "";

                if (w1t1[1].startsWith("NN")) {
                    categW1 = "NOUN";
                } else if (w1t1[1].startsWith("JJ")) {
                    categW1 = "ADJECTIVE";
                } else if (w1t1[1].startsWith("VB")) {
                    categW1 = "VERB";
                }
                
                if (w2t2[1].startsWith("NN")) {
                    categW2 = "NOUN";
                } else if (w2t2[1].startsWith("JJ")) {
                    categW2 = "ADJECTIVE";
                } else if (w2t2[1].startsWith("VB")) {
                    categW2 = "VERB";
                }
                
                
                SynsetfromWN OBJ1 = new SynsetfromWN();
                List<String> W1synonyms = OBJ1.ReturnSyns(w1t1[0], categW1);
                List<String> W2synonyms = OBJ1.ReturnSyns(w2t2[0], categW2);
                
//                System.out.println("synonyms for "+w1t1[0]+" : "+synonyms);
//                System.out.println("Decided Cat: "+categ);

                //========================================                
                //============ TEST SNIPPET ==============
                //========================================
//                if(NP.equals("sliced onions")){
//                    System.out.println(NP+ " "+ "w1t1[1]: "+w1t1[1]+" --> "+categ);
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
                    String compound = o+" "+w2t2[0];
                    if (bigramHM.containsKey(compound)) {
                        w2w1RnumSigma += bigramHM.get(compound);
//                        System.out.println("\t"+bigramHM.get(compound));
                    }else{
//                        System.out.println(compound+" not in bigrams");
                    }
                    if (unigramHM.containsKey(o)) {
                        w2w1RdenumSigma += unigramHM.get(o);
//                        System.out.println("\t"+unigramHM.get(o));
                    }else{
                        //it's possible the synonym is not a unigram
                        //System.out.println(o+" ------>   not in unigrams or not a unigram");
                    }
                }
                //=====================================================
                //====== num and denum of  P(w1|Synset(w2))============
                //=====================================================
                for (String o : W2synonyms) {
                    String compound = w1t1[0]+" "+o;
                    if (bigramHM.containsKey(compound)) {
                        w1w2RnumSigma += bigramHM.get(compound);
                    }else{
//                        System.out.println(compound+" not in bigrams");
                    }
                    if (unigramHM.containsKey(o)) {
                        w1w2RdenumSigma += unigramHM.get(o);
                    }else{
                        //it's possible the synonym is not a unigram
//                        System.out.println(o+" not in unigrams or not a unigram");
                    }   
                }
                //====================================================
                //====================================================
                //====================================================
                
                
                
                
                
                if ((w2w1RdenumSigma != 0) && (w1w2RdenumSigma !=0)) {
                    
                    numOfProcessed++;
                    
                    //without smoothing:
                    double RIGHTw2w1 = w2w1RnumSigma / w2w1RdenumSigma;
                    Pw2SynsetW1 = RIGHTw2w1;
                    double RIGHTw1w2 = w1w2RnumSigma / w1w2RdenumSigma;
                    Pw1SynsetW2 = RIGHTw1w2;
                    
                    //with smoothing:
                    
//                    double RIGHTw2w1 = w2w1RnumSigma+1 / w2w1RdenumSigma + unigramHM.size();
//                    Pw2SynsetW1 = RIGHTw2w1;
//                    double RIGHTw1w2 = w1w2RnumSigma+1 / w1w2RdenumSigma + unigramHM.size();
//                    Pw1SynsetW2 = RIGHTw1w2;
                    
                    
                    
                //==================================================
                //======= generate the list of collocations ========
                //==================================================
                    if ((PW2W1 > alpha * Pw2SynsetW1) && (PW1W2 > alpha * Pw1SynsetW2)) {
                        prediction = 1;
                        collocations.add(NP);
                    } else if ( (PW2W1 <= alpha * Pw2SynsetW1) && (PW1W2 <= alpha * Pw1SynsetW2)) {
                        prediction = 0;
                    } 
                }else{
                    //not enough information to compute collocational weight for this entry
                }
            }
        }
        
        //System.out.println("number of processed entries: "+numOfProcessed);
        //System.out.println("number of collocations: "+collocations.size());
        System.out.println("Returning a set of collocations identified by bidirectiona model.");
        return collocations;
        
    }
}
