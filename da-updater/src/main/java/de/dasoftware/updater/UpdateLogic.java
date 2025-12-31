package de.dasoftware.updater;

import java.io.File;
import java.io.IOException;

/**
 * Core update logic for version comparison and update execution
 */
public class UpdateLogic {
    
    public static final String FILENAME = "update_setup.exe";
    
    /**
     * Checks if an update is needed by comparing local and remote versions
     * 
     * @param local Local version data
     * @param remote Remote version data
     * @return true if update is available and newer than local version
     */
    public boolean updateNeeded(UpdaterData local, RemoteUpdateData remote) {
        // Major version check
        if (remote.getMajorVersion() > local.getMajorVersion()) {
            return true;
        }
        
        // Minor version check
        if (remote.getMinorVersion() > local.getMinorVersion() && 
            remote.getMajorVersion() >= local.getMajorVersion()) {
            return true;
        }
        
        // Bugfix version check
        if (remote.getBugfixVersion() > local.getBugfixVersion() && 
            remote.getMinorVersion() >= local.getMinorVersion() && 
            remote.getMajorVersion() >= local.getMajorVersion()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Starts the update setup executable from temp directory
     * 
     * @throws IOException If setup file cannot be started
     */
    public void startUpdateSetup() throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        File setupFile = new File(tempDir, FILENAME);
        
        if (!setupFile.exists()) {
            throw new IOException("Update setup file not found: " + setupFile.getAbsolutePath());
        }
        
        // Start the setup process
        ProcessBuilder pb = new ProcessBuilder(setupFile.getAbsolutePath());
        pb.start();
    }
    
    /**
     * Loads remote update data from URL
     * 
     * @param url URL to update XML file
     * @return RemoteUpdateData object with update information
     * @throws Exception 
     */
    public RemoteUpdateData loadRemoteData(String url) throws Exception {
        RemoteUpdateData data = new RemoteUpdateData();
        
        WebDownloader downloader = new WebDownloader(url);
        String xmlContent = downloader.downloadAsText();
        
        if (xmlContent == null || xmlContent.isEmpty()) {
            return data; // Return empty data
        }
        
        data.readFromXml(xmlContent);
        
        return data;
    }
}