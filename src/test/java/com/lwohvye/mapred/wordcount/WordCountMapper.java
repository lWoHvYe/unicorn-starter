package com.lwohvye.mapred.wordcount;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.StringTokenizer;

public class WordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
    private static final IntWritable one = new IntWritable(1);
    private Text word = new Text();

    @Override
    protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, IntWritable>.Context context) throws IOException, InterruptedException {
        // 内部使用默认分隔符切割delimiter
        var itr = new StringTokenizer(value.toString());
        // 循环写出
        while (itr.hasMoreTokens()) {
            // 封装
            word.set(itr.nextToken());
            // 写出
            context.write(word, one);
        }
    }
}
