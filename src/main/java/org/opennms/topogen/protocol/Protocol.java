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

package org.opennms.topogen.protocol;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.topogen.TopologyGenerator;
import org.opennms.topogen.TopologyPersister;
import org.opennms.topogen.topology.LinkedPairGenerator;
import org.opennms.topogen.topology.PairGenerator;
import org.opennms.topogen.topology.RandomConnectedPairGenerator;
import org.opennms.topogen.topology.UndirectedPairGenerator;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public abstract class Protocol<Element> {

    protected final TopologyGenerator.Topology topology;
    protected final int amountNodes;
    protected final int amountLinks;
    protected final int amountElements;
    protected final TopologyPersister persister;

    public abstract void createAndPersistNetwork() throws SQLException;

    private OnmsMonitoringLocation createMonitoringLocation() {
        OnmsMonitoringLocation location = new OnmsMonitoringLocation();
        location.setLocationName("Default");
        location.setMonitoringArea("localhost");
        return location;
    }

    protected List<OnmsNode> createNodes(int amountNodes) {
        OnmsMonitoringLocation location = createMonitoringLocation();
        ArrayList<OnmsNode> nodes = new ArrayList<>();
        for (int i = 0; i < amountNodes; i++) {
            nodes.add(createNode(i, location));
        }
        return nodes;
    }

    protected OnmsNode createNode(int count, OnmsMonitoringLocation location) {
        OnmsNode node = new OnmsNode();
        node.setId(count); // we assume we have an empty database and can just generate the ids
        node.setLabel("Node" + count);
        node.setLocation(location);
        return node;
    }

    protected PairGenerator<Element> createPairGenerator(List<Element> elements){
        if(TopologyGenerator.Topology.complete == topology){
            return new UndirectedPairGenerator<>(elements);
        } else if(TopologyGenerator.Topology.ring == topology) {
            return new LinkedPairGenerator<>(elements);
        } else if (TopologyGenerator.Topology.random == topology){
            return new RandomConnectedPairGenerator<>(elements);
        } else {
            throw new IllegalArgumentException("unknown topology: "+ topology);
        }
    }
}
