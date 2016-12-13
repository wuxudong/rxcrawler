package com.mrkid.ecommerce.itjuzi;

import java.io.*;

/**
 * User: xudong
 * Date: 13/12/2016
 * Time: 9:51 AM
 */
public class CompanyDumper {

    public static boolean isCompanyDone(long id) {
        final File file = getFile(id);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    private static File getFile(long id) {
        File dir = new File("output/" + id / 1000);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return new File(dir, "com_" + id);
    }


    public static void dumpCompany(long id, String content) throws IOException {
        FileWriter fileWriter = new FileWriter(getFile(id));
        fileWriter.write(content);
        fileWriter.close();
    }
}
