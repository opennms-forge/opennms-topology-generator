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

package org.opennms.topogen;

import static org.kohsuke.args4j.OptionHandlerFilter.ALL;

import java.io.IOException;
import java.sql.SQLException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.opennms.topogen.protocol.CdpProtocol;
import org.opennms.topogen.protocol.IsIsProtocol;
import org.opennms.topogen.protocol.LldpProtocol;
import org.opennms.topogen.protocol.OspfProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Setter;

public class TopologyGenerator {

    private final static Logger LOG = LoggerFactory.getLogger(TopologyGenerator.class);

    public enum Topology{
        ring, random, complete
    }

    public enum Protocol{
        cdp, isis, lldp, ospf
    }

    private TopologyPersister persister;
    @Option(name="--nodes",usage="generate <N> OmnsNodes")
    private int amountNodes = 10;
    @Option(name="--elements",usage="generate <N> CdpElements")
    private int amountElements = -1;
    @Option(name="--links",usage="generate <N> CdpLinks")
    private int amountLinks = -1;
    @Option(name="--topology",usage="type of topology (complete | ring | random)")
    @Setter
    private String topology = "random";
    @Option(name="--protocol",usage="type of protocol (cdp | isis | lldp | ospf)")
    @Setter
    private String protocol = "cdp";
    @Option(name="--delete",usage="delete existing toplogogy (all OnmsNodes, CdpElements and CdpLinks)")
    private boolean deleteExistingTolology = false;

    public TopologyGenerator(TopologyPersister persister) throws IOException {
        this.persister = persister;
    }

    void assertSetup() {
        if(amountElements == -1){
            amountElements = amountNodes;
        }
        if(amountLinks == -1){
            amountLinks = (amountElements * amountElements)-amountElements;
        }
        // do basic checks to get configuration right:
        assertMoreOrEqualsThan("we need at least as many nodes as elements", amountElements, amountNodes);
        assertMoreOrEqualsThan("we need at least 2 nodes", 2, amountNodes);
        assertMoreOrEqualsThan("we need at least 2 elements", 2, amountElements);
        assertMoreOrEqualsThan("we need at least 1 link", 1, amountLinks);

        Topology.valueOf(topology); // check if valid parameter
    }

    private void createNetwork() throws SQLException {
        if(deleteExistingTolology){
            this.persister.deleteTopology();
        }
        getProtocol().createAndPersistNetwork();
    }

    private org.opennms.topogen.protocol.Protocol getProtocol(){
        if(Protocol.cdp.name().equals(this.protocol)){
            return new CdpProtocol( Topology.valueOf(topology),
                    amountNodes, amountLinks, amountElements, persister);
        } else if (Protocol.isis.name().equals(this.protocol)) {
            return new IsIsProtocol( Topology.valueOf(topology),
                    amountNodes, amountLinks, amountElements, persister);
        } else if (Protocol.lldp.name().equals(this.protocol)) {
            return new LldpProtocol( Topology.valueOf(topology),
                    amountNodes, amountLinks, amountElements, persister);
        } else if (Protocol.ospf.name().equals(this.protocol)) {
            return new OspfProtocol( Topology.valueOf(topology),
                    amountNodes, amountLinks, amountElements, persister);
        } else {
            throw new IllegalArgumentException("Don't know this protocol: " + this.protocol);
        }
    }

    private void doMain(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            System.err.println("java TopologyGenerator [options...]");
            parser.printUsage(System.err);
            System.err.println();
            System.err.println("  Example: java TopologyGenerator"+parser.printExample(ALL));
        }
    }

    private static void assertMoreOrEqualsThan(String message, int expected, int actual) {
        if (actual < expected) {
            throw new IllegalArgumentException(message + String.format(" minimum expected=%s but found actual=%s", expected, actual));
        }
    }

    public static void main(String args[]) throws Exception {
        TopologyGenerator generator = new TopologyGenerator(new TopologyPersister());
        generator.doMain(args);
        generator.assertSetup();
        generator.createNetwork();
    }
}