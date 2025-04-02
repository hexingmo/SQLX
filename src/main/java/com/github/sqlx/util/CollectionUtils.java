/*
 *    Copyright 2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.github.sqlx.util;



import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

/**
 * @author He Xing Mo
 * @since 1.0
 */
public class CollectionUtils {

    private CollectionUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Returns a new collection containing the elements in collection a that are not in collection b.
     * The cardinality of an element e in the returned collection is the same as the cardinality of e in a
     * minus the cardinality of e in b, or zero, whichever is greater.
     *
     * @param <O> the type of object in the returned collection
     * @param a the collection to subtract from, must not be null
     * @param b the collection to subtract, must not be null
     * @return a new collection with the results
     * @throws NullPointerException if either collection is null
     */
    public static <O> Collection<O> subtract(final Iterable<? extends O> a, final Iterable<? extends O> b) {
        if (a == null) {
            throw new NullPointerException("First collection must not be null");
        }
        if (b == null) {
            throw new NullPointerException("Second collection must not be null");
        }
        
        // Create a map to count occurrences of each element in collection b
        Map<Object, Integer> bElementCount = new HashMap<>();
        
        // Count elements in collection b
        for (final O element : b) {
            bElementCount.merge(element, 1, Integer::sum);
        }
        
        // Create result collection
        Collection<O> result = new ArrayList<>();
        
        // Add elements from collection a that are not in b or have higher cardinality in a
        for (final O element : a) {
            Integer countInB = bElementCount.get(element);
            
            if (countInB == null || countInB == 0) {
                // Element not in b or all occurrences already subtracted, add to result
                result.add(element);
            } else {
                // Decrement count in b, don't add to result
                bElementCount.put(element, countInB - 1);
            }
        }
        
        return result;
    }

    /**
     * Returns true if the given Collections contain exactly the same elements with exactly the same cardinalities.
     * That is, if the cardinality of e in a is equal to the cardinality of e in b, for each element e in a or b.
     *
     * @param a the first collection, must not be null
     * @param b the second collection, must not be null
     * @return true if the collections contain the same elements with the same cardinalities
     * @throws NullPointerException if either collection is null
     */
    public static boolean isEqualCollection(final Collection<?> a, final Collection<?> b) {
        if (a == null || b == null) {
            throw new NullPointerException("Collections must not be null");
        }
        
        // Quick check: if sizes are different, collections are definitely not equal
        if (a.size() != b.size()) {
            return false;
        }
        
        // If both collections are the same object, they are definitely equal
        if (a == b) {
            return true;
        }
        
        // Create a map to count occurrences of each element in the first collection
        Map<Object, Integer> cardinalityMap = new HashMap<>();
        
        // Count occurrences of each element in the first collection
        for (Object obj : a) {
            cardinalityMap.merge(obj, 1, Integer::sum);
        }
        
        // Check elements in the second collection, decreasing the corresponding count
        for (Object obj : b) {
            Integer count = cardinalityMap.get(obj);
            
            // If the element doesn't exist in the first collection or its count is already 0, collections are not equal
            if (count == null || count == 0) {
                return false;
            }
            
            // Decrease the count
            cardinalityMap.put(obj, count - 1);
        }
        
        // Check if all counts are 0 (meaning all elements match)
        for (Integer count : cardinalityMap.values()) {
            if (count != 0) {
                return false;
            }
        }
        
        return true;
    }

    public static <T> boolean containsAny(final Collection<?> coll1, @SuppressWarnings("unchecked") final T... coll2) {
        if (coll1.size() < coll2.length) {
            for (final Object aColl1 : coll1) {
                if (ArrayUtils.contains(coll2, aColl1)) {
                    return true;
                }
            }
        } else {
            for (final Object aColl2 : coll2) {
                if (coll1.contains(aColl2)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isNotEmpty(final Collection<?> coll) {
        return !isEmpty(coll);
    }

    public static boolean isEmpty(final Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }
}
