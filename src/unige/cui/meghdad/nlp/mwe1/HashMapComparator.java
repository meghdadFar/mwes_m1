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
