package org.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.Util.Code_Handle.HandleCode;
import org.Util.Code_Handle.SplitCode;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class Test {
    public static void main(String[] args) throws IOException {

        HashMap<String,ArrayList<String>> hashMap=SplitCode.splitCode("a.deploy(database:mysql)#b.create(mysql,user)","a");
        System.out.println(hashMap.toString());
        HandleCode.judge_Code(hashMap);
    }
}

