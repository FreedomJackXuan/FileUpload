package org.Bean.breakpoint;

import java.util.LinkedList;
import java.util.List;

//服务器第一次返回的断点续传的信息
public class Breakpoint_Message {
    private String protocol;
    private String support;
    private LinkedList<Bean_File> file;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getSupport() {
        return support;
    }

    public void setSupport(String support) {
        this.support = support;
    }

    public LinkedList<Bean_File> getFile() {
        return file;
    }

    public void setFile(LinkedList<Bean_File> file) {
        this.file = file;
    }
}
