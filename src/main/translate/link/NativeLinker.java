package main.translate.link;

import java.io.File;
import java.util.HashMap;

public class NativeLinker {
    private static final File path = new File("linker.dll");

    static {
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
