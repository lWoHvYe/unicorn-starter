package com.lwohvye.mapred.wordcount;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

// same with WordCountReducer
public class WordCountCombiner extends Reducer<Text, IntWritable, Text, IntWritable> {

    private final IntWritable outV = new IntWritable();

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Reducer<Text, IntWritable, Text, IntWritable>.Context context) throws IOException, InterruptedException {
        var sum = 0;
        // 累加
        for (var value : values) {
            sum += value.get();
        }
        outV.set(sum);
        // 写出
        context.write(key, outV);
    }
}
