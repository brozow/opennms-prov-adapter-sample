/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2010-2012 The OpenNMS Group,
 * Inc. OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc. OpenNMS(R)
 * is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. OpenNMS(R) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details. You should have received a copy of the GNU General Public
 * License along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 *******************************************************************************/

package org.opennms.sample.provisioning;

import java.net.InetAddress;
import java.util.Set;

import org.opennms.netmgt.config.SnmpAgentConfigFactory;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.ProvisioningAdapterException;
import org.opennms.netmgt.provision.SimplerQueuedProvisioningAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.util.Assert;

/**
 */
public class SampleProvisioningAdapter extends SimplerQueuedProvisioningAdapter {

    //private static final Logger LOG = LoggerFactory.getLogger(SampleProvisioningAdapter.class);

    private NodeDao m_nodeDao;

    private SnmpAgentConfigFactory m_snmpConfigDao;

    /**
     * Constant <code>NAME="SampleProvisioningAdapter"</code>
     */
    public static final String NAME = "SampleProvisioningAdapter";

    public SampleProvisioningAdapter() {
        super(NAME);
    }

    @Override
    public void doAddNode(int nodeId) throws ProvisioningAdapterException {
        info("doAdd: adding nodeid: {}", nodeId);

        InetAddress ipaddress = getIpAddress(nodeId);

        info("doAdd: ip address for added node {} is {}", nodeId, ipaddress);

    }
    
    @Override
    public void doUpdateNode(int nodeId) throws ProvisioningAdapterException {
        info("doUpdate: updating nodeid: {}", nodeId);

        final InetAddress ipaddress = getIpAddress(nodeId);

        info("doUpdate: ip address for updated node {} is {}", nodeId, ipaddress);
    }

    @Override
    public void doDeleteNode(int nodeId) throws ProvisioningAdapterException {
        info("doDelete: deleted nodeid: {}", nodeId);

        InetAddress ipaddress = getIpAddress(nodeId);

        info("doDelete: ip address for deleted node {} is {}", nodeId, ipaddress);
    }

    @Override
    public void doNotifyConfigChange(final int nodeId) throws ProvisioningAdapterException {
        info("doNodeConfigChanged: nodeid: {}", nodeId);
    }

    private InetAddress getIpAddress(final int nodeId) {
        return m_template.execute(new TransactionCallback<InetAddress>() {
            public InetAddress doInTransaction(TransactionStatus arg0) {

                // Load thenode from the database
                final OnmsNode node = m_nodeDao.get(nodeId);
                Assert.notNull(node, "failed to return node for given nodeId:"+nodeId);
                info("getIpAddress: node: {} Foreign Source: {}", node.getNodeId(), node.getForeignSource());

                // find the primary interface for the node
                final OnmsIpInterface primaryInterface = node.getPrimaryInterface();
                if (primaryInterface != null && primaryInterface.getIpAddress() != null) {
                    return primaryInterface.getIpAddress(); 
                }

                // unable to find the primary interface look thru the interfaces on the node for an ip
                info("getIpAddress: found null SNMP Primary Interface, getting interfaces");
                final Set<OnmsIpInterface> ipInterfaces = node.getIpInterfaces();
                for (final OnmsIpInterface onmsIpInterface : ipInterfaces) {
                    info("getIpAddress: trying Interface with id: {}", onmsIpInterface.getId());
                    if (onmsIpInterface.getIpAddress() != null)  {
                        return onmsIpInterface.getIpAddress();
                    }
                }

                // No interface with valid ip found
                info("getIpAddress: Unable to find interface with ipaddress for node: {}", node.getNodeId());
                return null;
            }
                
        });
    }

    private void info(String msg) {
        System.err.println(msg);
        //LOG.info(msg);
    }

    private void info(String msg, Object arg) {
        System.err.println(String.format(msg.replaceAll("\\{\\}",  "%s"), arg));
        //LOG.info(msg, arg);
    }

    private void info(String msg, Object arg1, Object arg2) {
        System.err.println(String.format(msg.replaceAll("\\{\\}",  "%s"), arg1, arg2));
        //LOG.info(msg, arg1, arg2);
    }

    /**
     * <p>
     * getNodeDao
     * </p>
     * 
     * @return a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    /**
     * <p>
     * setNodeDao
     * </p>
     * 
     * @param dao
     *            a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public void setNodeDao(final NodeDao dao) {
        m_nodeDao = dao;
    }

    /**
     * @return the snmpConfigDao
     */
    public SnmpAgentConfigFactory getSnmpPeerFactory() {
        return m_snmpConfigDao;
    }

    /**
     * @param snmpConfigDao
     *            the snmpConfigDao to set
     */
    public void setSnmpPeerFactory(final SnmpAgentConfigFactory snmpConfigDao) {
        this.m_snmpConfigDao = snmpConfigDao;
    }

}
