package com.program.dumpFromMongoToES;

import com.program.dumpFromMongoToES.DumpDataToES;

/**
 
 * @author wendy926

 */
/**
 * mode, dbname, dbcollection
 */
final class MainforDump {
    public static void main(String[] args) {
        DumpDataToES dumpdata = null;
        if (args[0] != null || args[0] != "" ||
            args[1] != null || args[1] != "" ||
            args[2] != null || args[2] != "" ||
            args[3] != null || args[3] != "") {
            dumpdata = new DumpDataToES(args[0], args[1], args[2]);
        } else {
            System.out.println("please input : mode dbname dbcollection dumptype");
        }
        dumpdata.dumpDataToES(args[3]);
    }
}

