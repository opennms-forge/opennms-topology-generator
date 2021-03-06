/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.topogen.topology;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

/** pairs elements randomly but not the same element to itself */
public class RandomConnectedPairGenerator<E> implements PairGenerator<E>{
    private final List<E> elements;
    private Random random = new Random(42);

    public RandomConnectedPairGenerator(List<E> elements){
        if(elements == null || elements.size()<2){
            throw new IllegalArgumentException("Need at least 2 elements in list to make a pair");
        }
        if(new HashSet<>(elements).size() < elements.size()){
            throw new IllegalArgumentException("List contains at least one duplicate");
        }
        this.elements = elements;
    }

    @Override
    public Pair<E, E> next(){
        E leftElement = getRandomElement(elements);
        E rightElement = getRandomElementButNotSame(elements, leftElement);
        return Pair.of(leftElement, rightElement);
    }

    private E getRandomElementButNotSame(List<E> elements,  E notSame){
        E value = getRandomElement(elements);
        while (value.equals(notSame)){
            value = getRandomElement(elements);
        }
        return value;
    }

    private E getRandomElement(List<E> list) {
        return list.get(random.nextInt(list.size()));
    }
}
