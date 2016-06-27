/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unige.cui.meghdad.toolkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * NgExtract class used by RunExtractNgrams to extract ngrams.
 * 
 * 
 */

public class NgExtract {
    
    
    /**
     * Two methods to help extract ngrams. 
     * 
     * ReturnNgrams() returns a set of ngrams by splitting the sentence into unigrams
     * and calling BuildNg() to concatenate those unigrams. 
     * 
     * @param order
     * @param sent
     * @return 
     */
    
    
    
        public List<String> ReturnNgrams(int order, String sent) {
        
        List<String> ngs = new ArrayList<>();
        //split the sentence around space
        String[] unigrams = sent.split(" ");
        
        
        //List<String> unigramList = Arrays.asList(unigrams);
        //                    /\ /\
        //This is faster than || ||
        // ||  ||  
        // \/  \/  
//        List<String> unigramList = new ArrayList();
//        Collections.addAll(unigramList, unigrams);
        
        
        String ng;
        for (int i = 0; i < unigrams.length - order + 1; i++) {
            ng = BuildNg(unigrams, i, i + order);
            if (!ngs.contains(ng.trim())) {
                ngs.add(ng.trim());
                //ng = "";
            }
            //ng = "";
        }
        return ngs;
    }

    public String BuildNg(String[] unigrams, int f, int l) {
        //StringBuilder is faster than String in concatination 
        StringBuilder ngStr = new StringBuilder();
        for (int i = f; i < l; i++) {
            ngStr.append(i > f ? " " : "").append(unigrams[i]); //changed this from + to chain of append
        }
        return ngStr.toString();
    }
}
