/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unige.cui.meghdad.toolkit;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to keep ngrams and their different tag sequences
 * and frequency of those tag sequences. 
 * 
 * @author Meghdad Farahmand
 */

class TaggedNgram {

    private List<String> tags = new ArrayList();
    private List<Integer> tagFrequency = new ArrayList();

    /**
     * @return the tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * @param tags 
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * @return the tagFrequency
     */
    public List<Integer> getTagFrequency() {
        return tagFrequency;
    }

    /**
     * @param tagFrequency set tagFrequency
     */
    public void setTagFrequency(List<Integer> tagFrequency) {
        this.tagFrequency = tagFrequency;
    }

}
