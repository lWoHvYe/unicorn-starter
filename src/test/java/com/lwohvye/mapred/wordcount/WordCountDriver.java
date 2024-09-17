package com.lwohvye.mapred.wordcount;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.springframework.util.StringUtils;

import java.util.Arrays;

public class WordCountDriver {
    // -DA=a -Db=b src/test/resources/word_count src/test/resources/output -Dc=c
    public static void main(String[] args) throws Exception {
        var conf = new Configuration();
        var newArgs = Arrays.stream(args)
                .filter(arg -> !StringUtils.startsWithIgnoreCase(arg, "-"))
                .toArray(String[]::new);
        if (newArgs.length != 2)
            throw new IllegalArgumentException("Wrong number of arguments: " + newArgs.length);
        Tool tool = new WordCountRunner();
        var run = ToolRunner.run(conf, tool, newArgs);
        System.exit(run);
    }
}
