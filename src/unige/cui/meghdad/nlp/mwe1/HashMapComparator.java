/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unige.cui.meghdad.nlp.mwe1;

import java.util.Comparator;
import java.util.HashMap;

/**
 * Implements comparable and used to sort a hashmap by its values.
 * @author Meghdad Farahmand<meghdad.farahmand@gmail.com>
 */
class HashMapComparator implements Comparator {

    HashMap M;
    String order;

    public HashMapComparator(HashMap M, String order) {
        this.M = M;
        this.order = order;
    }

    @Override
    public int compare(Object o1, Object o2) {

        Comparable firstVal = (Comparable) M.get(o1);
        Comparable sectVal = (Comparable) M.get(o2);

        int ret = 0;
        
        if (order.equals("ascending")) {
            ret =  firstVal.compareTo(sectVal);
        } else if (order.equals("descending")) {
            ret =  -1 * firstVal.compareTo(sectVal);
        }
        
        return ret;
    }

}
