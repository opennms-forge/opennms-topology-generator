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

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

public class RandomConnectedPairGeneratorTest {

    @Test
    void shouldRejectListsWithLessThan2Elements() {
        assertThrows(IllegalArgumentException.class, () -> new RandomConnectedPairGenerator<>(null));
        assertThrows(IllegalArgumentException.class, () -> new RandomConnectedPairGenerator<>(Collections.emptyList()));
    }


    @Test
    void shouldRejectListsWichContainsOnlyTheSameElement() {
        List<String> list = Arrays.asList("same", "same", "same");
        assertThrows(IllegalArgumentException.class, () -> new RandomConnectedPairGenerator<>(list));
    }

    @Test
    void shouldNotPairElementsWithItself() {
        List<String> list = Arrays.asList("1", "2", "3");
        RandomConnectedPairGenerator generator = new RandomConnectedPairGenerator<>(list);
        for (int i = 0; i < 10; i++) {
            Pair pair = generator.next();
            assertNotSame(pair.getLeft(), pair.getRight());
        }
    }

}
