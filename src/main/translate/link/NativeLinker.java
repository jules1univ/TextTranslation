package main.translate.link;

import java.io.File;
import java.util.HashMap;

public class NativeLinker {

    private static File path = null;

    static {
        if (System.getProperty("os.name").contains("win")) {
            path = new File("linker.dll");
        } else {
            path = new File("linker.so");
        }
        
        if (path.exists()) {
            System.load(path.getAbsolutePath());
        }
    }

    public NativeLinker() {

    }

    public boolean isReady() {
        return path.exists();
    }

    public native void create(String defaultRootPath, String outRootPath, String outLinkPath,
            HashMap<String, String> words);
}
