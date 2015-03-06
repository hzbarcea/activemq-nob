package org.apache.activemq.nob.filestore;

import com.google.common.io.Files;
import org.apache.activemq.nob.api.Broker;
import org.apache.activemq.nob.filestore.exception.FileStoreLoadBrokerException;
import org.apache.activemq.nob.filestore.exception.FileStoreWriteBrokerException;
import org.apache.activemq.nob.persistence.api.BrokerConfigurationServerPersistenceApi;
import org.apache.activemq.nob.persistence.api.BrokerConfigurationUpdatePersistenceApi;
import org.apache.activemq.nob.persistence.api.XBeanContent;
import org.apache.activemq.nob.persistence.api.exception.BrokerConfigException;
import org.apache.activemq.nob.persistence.api.exception.BrokerConfigNotFoundException;
import org.apache.activemq.nob.persistence.api.exception.BrokerConfigPersistenceException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by art on 2/19/15.
 */
public class FileStoreServerPersistenceAdapter implements BrokerConfigurationServerPersistenceApi, BrokerConfigurationUpdatePersistenceApi {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(FileStoreServerPersistenceAdapter.class);
    private Logger LOG = DEFAULT_LOGGER;


    private File location;
    private BrokerFilenameDecoder brokerFilenameDecoder;
    private BrokerFilenameEncoder brokerFilenameEncoder;
    private BrokerMetadataLoader brokerMetadataLoader;
    private BrokerMetadataWriter brokerMetadataWriter;

    private Map<String, BrokerInformation> brokers = new ConcurrentHashMap<String, BrokerInformation>();
    private Map<String, String> aliases = new ConcurrentHashMap<String, String>();

    private boolean brokerListLoadedInd = false;

    public Logger getLOG() {
        return LOG;
    }

    public void setLOG(Logger LOG) {
        this.LOG = LOG;
    }

    public File getLocation() {
        return location;
    }

    public void setLocation(File location) {
        this.location = location;
    }

    public BrokerFilenameDecoder getBrokerFilenameDecoder() {
        return brokerFilenameDecoder;
    }

    public void setBrokerFilenameDecoder(BrokerFilenameDecoder brokerFilenameDecoder) {
        this.brokerFilenameDecoder = brokerFilenameDecoder;
    }

    public BrokerFilenameEncoder getBrokerFilenameEncoder() {
        return brokerFilenameEncoder;
    }

    public void setBrokerFilenameEncoder(BrokerFilenameEncoder brokerFilenameEncoder) {
        this.brokerFilenameEncoder = brokerFilenameEncoder;
    }

    public BrokerMetadataLoader getBrokerMetadataLoader() {
        return brokerMetadataLoader;
    }

    public void setBrokerMetadataLoader(BrokerMetadataLoader brokerMetadataLoader) {
        this.brokerMetadataLoader = brokerMetadataLoader;
    }

    public BrokerMetadataWriter getBrokerMetadataWriter() {
        return brokerMetadataWriter;
    }

    public void setBrokerMetadataWriter(BrokerMetadataWriter brokerMetadataWriter) {
        this.brokerMetadataWriter = brokerMetadataWriter;
    }

    @Override
    public List<Broker> retrieveBrokerList() throws BrokerConfigPersistenceException {
        if ( ! brokerListLoadedInd) {
            refreshBrokerList();
            brokerListLoadedInd = true;
        }

        List<Broker> result = new LinkedList<>();
        for ( BrokerInformation oneBrokerInfo : brokers.values() ) {
            result.add(oneBrokerInfo.metadata);
        }

        return result;
    }

    @Override
    public Broker lookupBroker(String brokerId) {
        BrokerInformation brokerInfo = this.brokers.get(brokerId);

        Broker result = null;
        if ( brokerInfo != null ) {
            result = brokerInfo.metadata;
        }

        return  result;
    }

    @Override
    public XBeanContent getBrokerXbeanConfiguration(String brokerId) throws BrokerConfigPersistenceException {
        BrokerInformation brokerInfo = brokers.get(brokerId);

        XBeanContent result =  null;
        if ( brokerInfo != null ) {
            try {
                String content = Files.toString(brokerInfo.xbeanPath, Charset.forName("UTF-8"));
                result = new XBeanContent(content, brokerInfo.xbeanPath.lastModified());
            } catch ( IOException ioExc ) {
                throw new FileStoreLoadBrokerException("error while loading xbean file for broker", ioExc);
            }
        }

        return result;
    }

    @Override
    public void createNewBroker(Broker newBroker, InputStream xbeanContentSource) throws BrokerConfigPersistenceException {

        BrokerInformation brokerInfo = new BrokerInformation();
        brokerInfo.metadata = newBroker;

        brokerInfo.metadataPath = this.brokerFilenameEncoder.locateBrokerMetadataFilePath(newBroker.getId());
        brokerInfo.xbeanPath = this.brokerFilenameEncoder.locateBrokerXbeanFilePath(newBroker.getId());

        this.brokerMetadataWriter.writeBrokerMetadata(brokerInfo.metadataPath, newBroker);
        try {
            this.writeBrokerXbean(xbeanContentSource, brokerInfo.xbeanPath);
        } catch (IOException ioExc) {
            throw new FileStoreWriteBrokerException("failed to write broker xbean to " + brokerInfo.xbeanPath, ioExc);
        }

        brokers.put(newBroker.getId(), brokerInfo);
    }

    @Override
    public void updateBroker(Broker updateBroker) throws BrokerConfigNotFoundException {
        BrokerInformation brokerInfo = this.brokers.get(updateBroker.getId());
        if (brokerInfo == null) {
            throw new BrokerConfigNotFoundException("no broker " + updateBroker.getId());
        }
        this.brokerMetadataWriter.writeBrokerMetadata(brokerInfo.metadataPath, updateBroker);
    }

    @Override
    public void writeBrokerXbeanConfig(String brokerId, InputStream xbeanConfigSource) throws BrokerConfigException {

        BrokerInformation brokerInfo = this.brokers.get(brokerId);
        if ( brokerInfo != null ) {
            try {
                this.writeBrokerXbean(xbeanConfigSource, brokerInfo.xbeanPath);
            } catch (IOException ioExc) {
                throw new FileStoreWriteBrokerException("failed to write broker xbean configuration", ioExc);
            }
        } else {
            throw new BrokerConfigNotFoundException("broker not found" + brokerId);
        }
    }

    @Override
    public void removeBroker(String brokerId) throws BrokerConfigPersistenceException {
        BrokerInformation brokerInfo = this.brokers.remove(brokerId);

        if ( brokerInfo != null ) {
            if ( ! brokerInfo.metadataPath.delete() ) {
                this.LOG.warn("failed to remove broker metadata file on removal: brokerId={}; path={}",
                        brokerId, brokerInfo.metadataPath);
            }

            if ( ! brokerInfo.xbeanPath.delete() ) {
                this.LOG.warn("failed to remove broker xbean file on removal: brokerId={}; path={}",
                        brokerId, brokerInfo.xbeanPath);
            }
        }
    }

    /**
     * Load the fresh list of brokers now.
     */
    private void refreshBrokerList() {
        Map<String, BrokerInformation> newBrokerMap = new HashMap<>();
        Map<String, File> newBrokerMetadataPathsMap = new HashMap<>();
        Map<String, String> newAliasMap = new HashMap<>();

        // Locate all files that match based on the filename decoder
        File[] brokerDefinitions = location.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                // Only accept if both configuration and metadata files are present
                // This may still be insufficient, as metadata could be incorrect

                return ( ! file.isDirectory() )
                        &&  ( brokerFilenameDecoder.extractIdFromFilename(file) != null )
                        &&  ( brokerFilenameDecoder.getBrokerXbeanFile(file) != null );
            }
        });

         // Load all of the broker definitions from the matching files now.
        for (File def : brokerDefinitions) {
            Broker broker = this.loadBroker(newBrokerMap, newAliasMap, def);

            if ( broker != null ) {
                newBrokerMetadataPathsMap.put(broker.getId(), def);
            }
        }

        brokers = newBrokerMap;
        aliases = newAliasMap;
    }

    /**
     * Load a single broker whose metadata is stored at the given path.
     *
     * @param metadataPath path to the broker's metadata file.
     */
    private Broker loadBroker (Map<String, BrokerInformation> brokerMap, Map<String, String> aliasMap,
                               File metadataPath) {

        File xbeanPath = brokerFilenameDecoder.getBrokerXbeanFile(metadataPath);

        Broker broker = this.brokerMetadataLoader.loadMetadata(metadataPath, xbeanPath);
        if (broker != null) {
            String id = broker.getId();

            BrokerInformation newBrokerInfo = new BrokerInformation();
            newBrokerInfo.metadata = broker;
            newBrokerInfo.metadataPath = metadataPath;
            newBrokerInfo.xbeanPath = xbeanPath;

            BrokerInformation old = brokerMap.put(id, newBrokerInfo);

            if (!id.equals(broker.getName())) {
                aliasMap.put(broker.getName(), id);
            }

            if (old != null) {
                // We avoided synchronizing using a ConcurrentHashMap, but
                // if we replaced an existing value, there is a logic failure somewhere
                LOG.warn("NOB - broker {} already exists. Data may be corrupted.");
            }

            return  broker;
        } else {
            LOG.warn("Broker definition parse error ({}). Ignoring...", metadataPath.getName());
        }

        return  null;
    }

    protected void writeBrokerXbean (InputStream source, File xbeanPath) throws IOException {
        try ( FileOutputStream outputStream = new FileOutputStream(xbeanPath) ) {
            IOUtils.copy(source, outputStream);
        }
    }

    protected class BrokerInformation {
        public Broker metadata;
        public File metadataPath;
        public File xbeanPath;
    }
}
