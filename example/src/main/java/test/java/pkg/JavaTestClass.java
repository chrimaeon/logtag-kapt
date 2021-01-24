package test.java.pkg;

import test.pkg.Log;
import timber.log.Timber;
import com.cmgapps.LogTag;

@LogTag
public class JavaTestClass implements Log {
    public void log() {
        Timber.tag(JavaTestClassLogTag.LOG_TAG).d("java debug message");
    }
}
