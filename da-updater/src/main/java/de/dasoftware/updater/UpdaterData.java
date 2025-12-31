package de.dasoftware.updater;

/**
 * Local update configuration and installed version information
 * Represents the current state of the application
 *  */
public class UpdaterData {
    
    private String updateUrl;
    private int majorVersion;
    private int minorVersion;
    private int bugfixVersion;
    private boolean autoUpdate;
    private boolean autoClose;
    private boolean terminateApplicationOnUpdate;
    private String updaterTitle;
    private String appTitle;
    
    /**
     * Constructor with default values
     */
    public UpdaterData() {
        this.updateUrl = "";
        this.majorVersion = 0;
        this.minorVersion = 0;
        this.bugfixVersion = 0;
        this.autoUpdate = false;
        this.autoClose = true;
        this.terminateApplicationOnUpdate = false;
        this.updaterTitle = "Update Client";
        this.appTitle = "";
    }
    
    /**
     * Sets version from string format
     * Supports formats: x.x.x or x.x.x.x
     * 
     * @param versionString Version string (e.g., "1.2.3" or "1.2.3.4")
     * @throws IllegalArgumentException If version string format is invalid
     */
    public void setVersionString(String versionString) {
        if (versionString == null || versionString.trim().isEmpty()) {
            throw new IllegalArgumentException("Version string cannot be null or empty");
        }
        
        String[] parts = versionString.split("\\.");
        
        if (parts.length != 3 && parts.length != 4) {
            throw new IllegalArgumentException(
                "Version string must be numeric x.x.x or x.x.x.x");
        }
        
        try {
            this.majorVersion = Integer.parseInt(parts[0]);
            this.minorVersion = Integer.parseInt(parts[1]);
            this.bugfixVersion = Integer.parseInt(parts[2]);
            // parts[3] (build number) is ignored if present
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Version string must contain valid numbers: " + versionString, e);
        }
    }
    
    /**
     * Gets version as formatted string
     * 
     * @return Version string in format x.x.x
     */
    public String getVersionString() {
        return majorVersion + "." + minorVersion + "." + bugfixVersion;
    }
    
    // ========== Getters and Setters ==========
    
    public String getUpdateUrl() {
        return updateUrl;
    }
    
    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }
    
    public int getMajorVersion() {
        return majorVersion;
    }
    
    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }
    
    public int getMinorVersion() {
        return minorVersion;
    }
    
    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }
    
    public int getBugfixVersion() {
        return bugfixVersion;
    }
    
    public void setBugfixVersion(int bugfixVersion) {
        this.bugfixVersion = bugfixVersion;
    }
    
    public boolean isAutoUpdate() {
        return autoUpdate;
    }
    
    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }
    
    public boolean isAutoClose() {
        return autoClose;
    }
    
    public void setAutoClose(boolean autoClose) {
        this.autoClose = autoClose;
    }
    
    public boolean isTerminateApplicationOnUpdate() {
        return terminateApplicationOnUpdate;
    }
    
    public void setTerminateApplicationOnUpdate(boolean terminateApplicationOnUpdate) {
        this.terminateApplicationOnUpdate = terminateApplicationOnUpdate;
    }
    
    public String getUpdaterTitle() {
        return updaterTitle;
    }
    
    public void setUpdaterTitle(String updaterTitle) {
        this.updaterTitle = updaterTitle;
    }
    
    public String getAppTitle() {
        return appTitle;
    }
    
    public void setAppTitle(String appTitle) {
        this.appTitle = appTitle;
    }     
}