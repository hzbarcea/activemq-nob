package org.apache.activemq.nob.karaf.command;

import org.apache.activemq.nob.api.Broker;
import org.apache.activemq.nob.persistence.api.BrokerConfigurationServerPersistenceApi;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.ShellTable;

@Command(scope = "nob", name = "list", description = "Lists brokers")
@Service
public class List implements Action {

    @Reference
    BrokerConfigurationServerPersistenceApi serverPersistenceApi;

    @Override
    public Object execute() throws Exception {
        Iterable<Broker> brokerList = this.serverPersistenceApi.retrieveBrokerList();

        ShellTable table = new ShellTable();
        table.column("Entry ID");
        table.column("Broker name");
        table.column("Configuration date");
        table.column("Status");
        table.column("Broker URL");
        for (Broker broker : brokerList) {
            table.addRow().addContent(
                    broker.getId(),
                    broker.getName(),
                    broker.getLastModifiedXbean().getTime(),
                    broker.getStatus(),
                    broker.getBrokerUrl());
        }
        table.print(System.out);

        return null;
    }
}
