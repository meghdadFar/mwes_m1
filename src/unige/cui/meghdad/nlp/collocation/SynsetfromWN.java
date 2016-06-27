
package unige.cui.meghdad.nlp.collocation;

import edu.smu.tspell.wordnet.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Extracts synsets form WordNet. 
 * 
 * @author Meghdad Farahmand
 * 
 * 
 * (code samples from JWNL - Java WordNet Library - Dev Guide are used)
 */
public class SynsetfromWN {

    public int CountSyns(String argOne,String argTwo) {
        
        System.setProperty("wordnet.database.dir", "./WordNet-3.0/dict");
        HashMap<String,String> allSynonyms = new HashMap<String,String>();
        String wordForm = argOne.toLowerCase();
        String senType = argTwo;
        SynsetType senseType = SynsetType.NOUN;
        if(senType.equals("NOUN")){
            senseType = SynsetType.NOUN;
        }else if(senType.equals("ADJECTIVE")){
            senseType = SynsetType.ADJECTIVE;
        }else if(senType.equals("VERB")){
            senseType = SynsetType.VERB;
        }
            //  Get the synsets containing the wrod form
            WordNetDatabase database = WordNetDatabase.getFileInstance();
            Synset[] synsets = database.getSynsets(wordForm,senseType);
            //  Display the word forms and definitions for synsets retrieved
            if (synsets.length > 0) {
                for (int i = 0; i < synsets.length; i++) {
                    String[] wordForms = synsets[i].getWordForms();
                    for (int j = 0; j < wordForms.length; j++) {
//                        System.out.print(wordForms[j]+"\n");
                        allSynonyms.put(wordForms[j], "1");
                    }
                }
                //System.out.println("Number of synosyms: "+allSynonyms.size());
            } else {
//                System.err.println("No synsets exist that contain "
//                        + "the word form '" + wordForm + "'");
            }
        allSynonyms.remove(wordForm);
        return allSynonyms.size();
    }
    
    
    
    
    public List<String> ReturnSyns(String argOne,String argTwo) {
         System.setProperty("wordnet.database.dir", "./WordNet-3.0/dict");
        HashMap<String,String> allSynonyms = new HashMap<String,String>();
        String wordForm = argOne.toLowerCase();
        String senType = argTwo;
        SynsetType senseType = SynsetType.NOUN;
        if(senType.equals("NOUN")){
            senseType = SynsetType.NOUN;
        }else if(senType.equals("ADJECTIVE")){
            senseType = SynsetType.ADJECTIVE;
        }else if(senType.equals("VERB")){
            senseType = SynsetType.VERB;
        }
            //  Get the synsets containing the wrod form
            WordNetDatabase database = WordNetDatabase.getFileInstance();
            Synset[] synsets = database.getSynsets(wordForm,senseType);
            //  Display the word forms and definitions for synsets retrieved
            if (synsets.length > 0) {
                for (int i = 0; i < synsets.length; i++) {
                    String[] wordForms = synsets[i].getWordForms();
                    for (int j = 0; j < wordForms.length; j++) {
//                        System.out.print(wordForms[j]+"\n");
                        
                        
                        //if to avoid adding wordForms twoice because of capitalization. 
                        //e.g. in Wordnet, Synset(gas) contains: Ga, and ga. 
                        //I use this if so that I don't add the same element twoice, with different
                        //cases
                        if(!allSynonyms.containsKey(wordForms[j].toLowerCase())){
                        allSynonyms.put(wordForms[j].toLowerCase(), "1");
                        }
                    }
                }
                //System.out.println("Number of synosyms: "+allSynonyms.size());
            } else {
//                System.err.println("No synsets exist that contain "
//                        + "the word form '" + wordForm + "'");
            }
            
        //word form always exists in the synset, so we need to cut it out
        allSynonyms.remove(wordForm);
        List<String> allSynonymList = new ArrayList<>(allSynonyms.keySet());
        
        return allSynonymList;
    }
    
    
    
//    public static void main(String[] args) {
//        
//        SynsetfromWN ONE = new SynsetfromWN();
//        System.out.println(ONE.CountSyns("ground","NOUN"));
//        
//    }
}
