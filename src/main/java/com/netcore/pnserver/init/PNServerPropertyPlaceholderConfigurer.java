package com.netcore.pnserver.init;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 *
 * @author Hemant Adelkar
 */

public class PNServerPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    String[] files;

    public void setFiles(String[] files) {
        Resource locations[] = new Resource[files.length];
        for(int i=0;i <files.length; i++) {
            String file = files[i];
            if(file.startsWith("-Dcom.netcore.pnserver.propsfile")) {
                file = System.getProperty("com.netcore.pnserver.propsfile");
                if(file == null || file.length()==0) {
                	System.out.println("Using default property file ="+ConfigManager.DEFAULT_PROPS_FILE);
                    file=ConfigManager.DEFAULT_PROPS_FILE;
                }else {
                	System.out.println("Using property file ="+file);
                }
            }
            Resource location = new FileSystemResource(file);
            locations[i]=location;
        }
        setLocations(locations);
    }

    public String[] getFiles() {
        return this.files;
    }
}
