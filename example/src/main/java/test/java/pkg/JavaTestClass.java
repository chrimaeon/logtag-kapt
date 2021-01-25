package test.java.pkg;

import test.pkg.Log;
import timber.log.Timber;
import com.cmgapps.logtag.annotation.LogTag;

@LogTag
public class JavaTestClass implements Log {
    @Override
    public void log() {
        Timber.tag(JavaTestClassLogTag.LOG_TAG).d("java debug message");
    }
}
