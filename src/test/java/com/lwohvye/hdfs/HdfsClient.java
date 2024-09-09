package com.lwohvye.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class HdfsClient {

    @Test
    public void testMKDir() throws IOException, URISyntaxException, InterruptedException {
        var uri = new URI("hdfs://namenode:8020/");

        var conf = new Configuration();

        var user = "hadoop";

        try (var fs = FileSystem.get(uri, conf, user)) {
            fs.mkdirs(new Path("/test"));
        }
    }
}
