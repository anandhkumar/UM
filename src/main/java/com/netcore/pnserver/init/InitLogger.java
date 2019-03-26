package com.netcore.pnserver.init;

/**
 * @author Hemant Adelkar
 *
 */

import java.util.Properties;
import org.apache.log4j.PropertyConfigurator;

public class InitLogger {

	private static boolean initialized=false;

    public static void initLogger(Properties props) {
        if(initialized) {
            return;
        }
        try  {
            System.out.println("____ configuring logger for PN Server");
            PropertyConfigurator.configure(props);
            System.out.println("____ configured logger for PN Server Done");
            initialized = true;
        }
        catch(Exception ex) {
        	System.out.println("___________");
            ex.printStackTrace();
            System.out.println("___________");
        }
    }
}
