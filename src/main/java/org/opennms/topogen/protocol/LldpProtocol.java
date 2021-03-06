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
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.opennms.core.utils.LldpUtils;
import org.opennms.netmgt.model.LldpElement;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.topogen.TopologyGenerator;
import org.opennms.topogen.TopologyPersister;
import org.opennms.topogen.topology.PairGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;

public class LldpProtocol extends Protocol<LldpElement> {
    private final static Logger LOG = LoggerFactory.getLogger(IsIsProtocol.class);
    @Getter
    private TopologyGenerator.Protocol protocol = TopologyGenerator.Protocol.lldp;
    public LldpProtocol(TopologyGenerator.Topology topology, int amountNodes, int amountLinks,
                        int amountElements, int amountSnmpInterfaces, int amountIpInterfaces, TopologyPersister persister){
        super(topology, amountNodes, amountLinks, amountElements, amountSnmpInterfaces, amountIpInterfaces, persister);
    }

    @Override
    public void createAndPersistProtocolSpecificEntities(List<OnmsNode> nodes) throws SQLException {
        List<LldpElement> elements = createElements(nodes);
        persister.persistLldpElements(elements);
        List<LldpLink> links = createLinks(elements);
        persister.persistLldpLinks(links);
    }

    private List<LldpElement> createElements(List<OnmsNode> nodes) {
        ArrayList<LldpElement> elements = new ArrayList<>();
        for (int i = 0; i < amountElements; i++) {
            OnmsNode node = nodes.get(i);
            String lLdpChassisId = "lLdpChassisId"+UUID.randomUUID();
            elements.add(createElement(node, lLdpChassisId));
        }
        return elements;
    }

    private LldpElement createElement(OnmsNode node, String lLdpChassisId) {
        LldpElement element = new LldpElement();
        element.setId(node.getId());
        element.setNode(node);
        element.setLldpChassisId(lLdpChassisId);
        element.setLldpChassisIdSubType(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_CHASSISCOMPONENT);
        element.setLldpNodeLastPollTime(new Date());
        element.setLldpSysname("LldpSysname");
        return element;
    }


    private List<LldpLink> createLinks(List<LldpElement> elements) {
        PairGenerator<LldpElement> pairs = createPairGenerator(elements);
        List<LldpLink> links = new ArrayList<>();
        for (int i = 0; i < amountLinks; i++) {

            // We create 2 links that reference each other, see also LinkdToplologyProvider.match...Links()
            Pair<LldpElement, LldpElement> pair = pairs.next();
            LldpElement sourceElement = pair.getLeft();
            LldpElement targetElement = pair.getRight();

            String portId = UUID.randomUUID().toString();
            String portIdRemote = UUID.randomUUID().toString();
            LldpUtils.LldpPortIdSubType portIdSubType = LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS;
            LldpUtils.LldpPortIdSubType portIdSubTypeRemote = LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS;

            LldpLink sourceLink = createLink(i,
                    sourceElement.getNode(),
                    portId, portIdSubType, portIdRemote, portIdSubTypeRemote, targetElement.getLldpChassisId()
            );
            links.add(sourceLink);
            LldpLink targetLink = createLink(++i,
                    targetElement.getNode(),
                    portIdRemote, portIdSubTypeRemote, portId, portIdSubType, sourceElement.getLldpChassisId()
            );

            links.add(targetLink);
            LOG.debug("Linked node {} with node {}", sourceElement.getNode().getLabel(), targetElement.getNode().getLabel());
        }
        return links;
    }


    private LldpLink createLink(int id, OnmsNode node, String portId, LldpUtils.LldpPortIdSubType portIdSubType
            , String remotePortId, LldpUtils.LldpPortIdSubType remotePortIdSubType, String remoteChassisId) {
        LldpLink link = new LldpLink();
        link.setId(id);
        link.setLldpPortId(portId);
        link.setLldpPortIdSubType(portIdSubType);
        link.setLldpRemPortId(remotePortId);
        link.setLldpRemPortIdSubType(remotePortIdSubType);
        link.setLldpRemChassisId(remoteChassisId);
        link.setNode(node);

        // static attributes:
        link.setLldpRemChassisIdSubType(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_CHASSISCOMPONENT); // shouldn't be relevant for match => set it fixed
        link.setLldpLocalPortNum(123);
        link.setLldpPortIfindex(123);
        link.setLldpLinkLastPollTime(new Date());
        link.setLldpPortDescr("lldpportdescr");
        link.setLldpRemSysname("lldpRemSysname");
        link.setLldpPortDescr("lldpPortDescr");
        link.setLldpRemPortDescr("lldpRemPortDescr");

        return link;
    }
}
