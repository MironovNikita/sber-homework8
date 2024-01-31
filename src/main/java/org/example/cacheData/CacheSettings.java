package org.example.cacheData;

public class CacheSettings {
    private final CacheType cacheType;
    private final String fileNamePrefix;
    private final Class[] classes;
    private final boolean zip;
    private final int listSize;

    public CacheSettings(CacheType cacheType, String fileNamePrefix, Class[] identityBy, boolean zip, int listSize) {
        this.cacheType = cacheType;
        this.fileNamePrefix = fileNamePrefix;
        this.classes = identityBy;
        this.zip = zip;
        this.listSize = listSize;
    }

    public CacheType getCacheType() {
        return cacheType;
    }

    public String getFileNamePrefix() {
        return fileNamePrefix;
    }

    public Class[] getIdentityBy() {
        return classes;
    }

    public boolean isZip() {
        return zip;
    }

    public int getListSize() {
        return listSize;
    }
}
