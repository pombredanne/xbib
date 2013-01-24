package org.xbib.objectstorage;

public class ObjectStorageService {

    private final static ObjectStorageService instance = new ObjectStorageService();
        
    private ObjectStorageService() {
        
    }
    
    public static ObjectStorageService getObjectStorageService() {
        return instance;
    }

}
