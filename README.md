Sample Provisioning Adapter for OpenNMS
=======================================

A Provisioning Adapter allows someone to write a plugin that gets
triggered as opennms addes nodes, updates nodes and deletes nodes.

The code in the sample is an example provisioning adapter that writes
to stderr each time a node gets added, updated or deleted in
opennms. (Note: stderr for opennms is redirected to
logs/daemon/output.log)

The code looks up the 'primary ip address' for each node and logs that
as well.

To use this example code you need to build it using maven and then
copy the generated jar file from the target directory into the
${opennms.home}/lib directory before starting opennms.

OpenNMS will load and use this adapter based on the
META-INF/opennms/provisiond-extensions.xml file which is a Spring
Framework application context file used to create the provisioning
adapter.

