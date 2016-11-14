package com.nan.recordbutton;

import android.os.Environment;

import java.io.File;

public class Constants {

    public static final class FilePath {
        public static final String RECORD_TEMP_PATH = Environment.getExternalStorageDirectory() + File.separator + "com.sxbb" + File.separator + "recordTemp" + File.separator;
    }

}
