package org.xbib.objectstorage;

public class ObjectStorageService {

    private final static ObjectStorageService instance = new ObjectStorageService();
    
    private final static ObjectStorageAdapter adapter = 
            ObjectStorageAdapterService.getInstance().getDefaultAdapter();
    
    private ObjectStorageService() {
        
    }
    
    public static ObjectStorageService getObjectStorageService() {
        return instance;
    }

}
