package com.cyberon;

import android.app.Application;
import android.app.Fragment;
import android.os.Environment;

public class DSpotterApplication extends Application {
    public static int m_nCmdBinListIndex = 0;
    public static String m_sCmdFilePath = null;
    public static Fragment m_oFragment = null;
    public static String m_sLicenseFile = Environment.getExternalStorageDirectory()
            .getPath() + "/DCIM/CybLicense.bin";
    public static String m_sServerFile = Environment.getExternalStorageDirectory()
            .getPath() + "/DCIM/CybServer.bin";
    public static String m_sCmdFileDirectoryPath = Environment.getExternalStorageDirectory()
            .getPath() + "/DCIM";
    public static String m_sOnlineTestLogDirectoryPath = Environment.getExternalStorageDirectory()
            .getPath() + "/DCIM/OnlineTestLog";
//    public static int m_nCmdBinListIndex = 0;
//    public static String m_sCmdFilePath = null;
//    public static Fragment m_oFragment = null;
//    public static String m_sLicenseFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/cyberon/DSpotterDemo/CybLicense.bin";
//    public static String m_sServerFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/cyberon/DSpotterDemo/Certificate_PeerGiant.bin";
//    public static String m_sCmdFileDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/cyberon/DSpotterDemo/CmdFile";
//    public static String m_sOnlineTestLogDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/cyberon/DSpotterDemo/OnlineTestLog";
}
