package foo.bar;

@com.cmgapps.LogTag
class PackageJava implements LogProvider {

    @Override
    public String logging() {
        return PackageJavaLogTag.LOG_TAG;
    }
}
