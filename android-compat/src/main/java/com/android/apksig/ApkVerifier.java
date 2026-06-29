package com.android.apksig;

import java.io.File;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;

public class ApkVerifier {
    public ApkVerifier(File apkFile) {}
    public ApkVerifier(File apkFile, File certificateFile) {}
    public Result verify() throws IOException { return new Result(); }

    public static class Builder {
        public Builder(File apk) {}
        public Builder(File apk, File certificateFile) {}
        public ApkVerifier build() { return new ApkVerifier(new File(".")); }
    }

    public static class Result {
        public boolean isVerified() { return true; }
        public boolean isVerifiedUsingJarsigner() { return true; }
        public List<SignerCertificate> getSignerCertificates() { return List.of(); }
        public List<Warning> getWarnings() { return List.of(); }
        public List<Error> getErrors() { return List.of(); }

        public static class SignerCertificate {
            public X509Certificate getCertificate() { return null; }
        }
        public static class Warning {}
        public static class Error {}
    }
}
