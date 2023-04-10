package com.onedream.yellowknife_compiler;

import java.util.Objects;

public class KeyForClassFile {
    private String packageName;
    private String className;

    public KeyForClassFile(String packageName, String className) {
        this.packageName = packageName;
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyForClassFile that = (KeyForClassFile) o;
        return Objects.equals(packageName, that.packageName) &&
                Objects.equals(className, that.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName, className);
    }

    @Override
    public String toString() {
        return "KeyForClass{" +
                "packageName='" + packageName + '\'' +
                ", className='" + className + '\'' +
                '}';
    }
}
