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

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OspfElement;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.topogen.TopologyGenerator;
import org.opennms.topogen.TopologyPersister;
import org.opennms.topogen.topology.PairGenerator;
import org.opennms.topogen.util.InetAddressGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;

public class OspfProtocol extends Protocol<OspfElement> {
    private final static Logger LOG = LoggerFactory.getLogger(OspfProtocol.class);
    @Getter
    private TopologyGenerator.Protocol protocol = TopologyGenerator.Protocol.ospf;
    private InetAddressGenerator inetAddressCreator = new InetAddressGenerator();

    public OspfProtocol(TopologyGenerator.Topology topology, int amountNodes, int amountLinks,
                        int amountElements, int amountSnmpInterfaces, int amountIpInterfaces, TopologyPersister persister){
        super(topology, amountNodes, amountLinks, amountElements, amountSnmpInterfaces, amountIpInterfaces, persister);
    }

    @Override
    public void createAndPersistProtocolSpecificEntities(List<OnmsNode> nodes) throws SQLException {
        List<OspfLink> links = createLinks(nodes);
        persister.persistOspfLinks(links);
    }

    private List<OspfLink> createLinks(List<OnmsNode> nodes) {
        PairGenerator<OnmsNode> pairs = createPairGenerator(nodes);
        List<OspfLink> links = new ArrayList<>();
        for (int i = 0; i < amountLinks; i++) {

            // We create 2 links that reference each other, see also LinkdToplologyProvider.matchCdpLinks()
            Pair<OnmsNode, OnmsNode> pair = pairs.next();
            OnmsNode sourceNode = pair.getLeft();
            OnmsNode targetNode = pair.getRight();
            InetAddress ospfIpAddr = inetAddressCreator.next();
            InetAddress ospfRemIpAddr = inetAddressCreator.next();
            OspfLink sourceLink = createLink(i,
                    sourceNode,
                    ospfIpAddr,
                    ospfRemIpAddr
            );
            links.add(sourceLink);

            OspfLink targetLink = createLink(++i,
                    targetNode,
                    ospfRemIpAddr,
                    ospfIpAddr
            );
            links.add(targetLink);
            LOG.debug("Linked node {} with node {}", sourceNode.getLabel(), targetNode.getLabel());
        }
        return links;
    }

    private OspfLink createLink(int id, OnmsNode node, InetAddress ipAddress, InetAddress remoteAddress) {
        OspfLink link = new OspfLink();
        link.setId(id);
        link.setNode(node);
        link.setOspfIpAddr(ipAddress);
        link.setOspfRemIpAddr(remoteAddress);

        link.setOspfIpMask(this.inetAddressCreator.next());
        link.setOspfAddressLessIndex(3);
        link.setOspfIfIndex(3);
        link.setOspfRemRouterId(this.inetAddressCreator.next());
        link.setOspfRemAddressLessIndex(3);
        link.setOspfLinkLastPollTime(new Date());



        return link;
    }
}
