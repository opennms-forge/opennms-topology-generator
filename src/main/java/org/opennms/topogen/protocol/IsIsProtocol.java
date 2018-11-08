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
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.opennms.netmgt.model.IsIsElement;
import org.opennms.netmgt.model.IsIsLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.topogen.TopologyGenerator;
import org.opennms.topogen.TopologyPersister;
import org.opennms.topogen.topology.PairGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsIsProtocol extends Protocol<IsIsElement> {
    private final static Logger LOG = LoggerFactory.getLogger(IsIsProtocol.class);

    public IsIsProtocol(TopologyGenerator.Topology topology, int amountNodes, int amountLinks, int amountElements, TopologyPersister persister){
        super(topology, amountNodes, amountLinks, amountElements, persister);
    }

    @Override
    public void createAndPersistNetwork() throws SQLException {

        LOG.info("creating {} topology with {} {}s, {} {}s and {} {}s.",
                this.topology,
                this.amountNodes, OnmsNode.class.getSimpleName() ,
                this.amountElements, IsIsElement.class.getSimpleName(),
                this.amountLinks, IsIsLink.class.getSimpleName());
        List<OnmsNode> nodes = createNodes(amountNodes);
        persister.persistNodes(nodes);
        List<IsIsElement> elements = createElements(nodes);
        persister.persistIsIsElements(elements);
        List<IsIsLink> links = createLinks(elements);
        persister.persistIsIsLinks(links);
    }

    private List<IsIsElement> createElements(List<OnmsNode> nodes) {
        ArrayList<IsIsElement> elements = new ArrayList<>();
        for (int i = 0; i < amountElements; i++) {
            OnmsNode node = nodes.get(i);
            elements.add(createElement(node));
        }
        return elements;
    }

    private IsIsElement createElement(OnmsNode node) {
        IsIsElement element = new IsIsElement();
        element.setId(node.getId()); // we use the same id for simplicity
        element.setNode(node);
        element.setIsisSysID("IsIsElementForNode" + node.getId());
        element.setIsisSysAdminState(IsIsElement.IsisAdminState.on);
        element.setIsisNodeLastPollTime(new Date());
        return element;
    }

    private List<IsIsLink> createLinks(List<IsIsElement> elements) {
        PairGenerator<IsIsElement> pairs = createPairGenerator(elements);
        List<IsIsLink> links = new ArrayList<>();
        int id=0;
        for (int i = 0; i < amountLinks; i++) {

            // We create 2 links that reference each other, see also LinkdToplologyProvider.match...Links()
            Pair<IsIsElement, IsIsElement> pair = pairs.next();
            IsIsElement sourceElement = pair.getLeft();
            IsIsElement targetElement = pair.getRight();
            IsIsLink sourceLink = createLink(id++,
                    sourceElement.getNode(),
                    i, targetElement.getIsisSysID()
            );
            links.add(sourceLink);

            IsIsLink targetLink = createLink(id++,
                    targetElement.getNode(),
                    i,
                    sourceElement.getIsisSysID()
            );
            links.add(targetLink);
            LOG.debug("Linked node {} with node {}", sourceElement.getNode().getLabel(), targetElement.getNode().getLabel());
        }
        return links;
    }

    private IsIsLink createLink(int id, OnmsNode node, Integer isisISAdjIndex, String isisISAdjNeighSysID) {
        IsIsLink link = new IsIsLink();
        link.setId(id);
        link.setIsisISAdjIndex(isisISAdjIndex);
        link.setIsisISAdjNeighSysID(isisISAdjNeighSysID);
        link.setNode(node);
        // static data:
        link.setIsisLinkLastPollTime(new Date());
        link.setIsisCircIndex(3);
        link.setIsisISAdjState(IsIsLink.IsisISAdjState.up);
        link.setIsisISAdjNeighSNPAAddress("isisISAdjNeighSNPAAddress");
        link.setIsisISAdjNeighSysType(IsIsLink.IsisISAdjNeighSysType.l1_IntermediateSystem);
        link.setIsisISAdjNbrExtendedCircID(3);
        link.setIsisCircIfIndex(3);
        link.setIsisCircAdminState(IsIsElement.IsisAdminState.on);
        return link;
    }

}