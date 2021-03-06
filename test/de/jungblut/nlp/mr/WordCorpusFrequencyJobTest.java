package de.jungblut.nlp.mr;

import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset.Entry;

import de.jungblut.nlp.StandardTokenizer;
import de.jungblut.nlp.mr.WordCorpusFrequencyJob.DocumentSumReducer;
import de.jungblut.nlp.mr.WordCorpusFrequencyJob.TokenMapper;

public class WordCorpusFrequencyJobTest extends TestCase {

  MapDriver<LongWritable, Text, Text, TextIntPairWritable> mapDriver;
  ReduceDriver<Text, TextIntPairWritable, Text, TextIntIntIntWritable> reduceDriver;
  MapReduceDriver<LongWritable, Text, Text, TextIntPairWritable, Text, TextIntIntIntWritable> mapReduceDriver;

  String toDedup = "this this is a text about how i used lower case and and duplicate words words";
  HashMultiset<String> tokenFrequency = HashMultiset.create(Arrays
      .asList(new StandardTokenizer().tokenize(toDedup)));

  @Override
  @Before
  public void setUp() {
    toDedup = "ID123\t" + toDedup;
    TokenMapper mapper = new TokenMapper();
    DocumentSumReducer reducer = new DocumentSumReducer();
    mapDriver = MapDriver.newMapDriver(mapper);
    reduceDriver = ReduceDriver.newReduceDriver(reducer);
    mapReduceDriver = MapReduceDriver.newMapReduceDriver(mapper, reducer);
  }

  @Test
  public void testMapper() {
    mapDriver.withInput(new LongWritable(), new Text(toDedup));

    for (Entry<String> entry : tokenFrequency.entrySet()) {
      mapDriver.addOutput(
          new Text(entry.getElement()),
          new TextIntPairWritable(new Text("ID123"), new IntWritable(entry
              .getCount())));
    }

    mapDriver.runTest();
  }

  @Test
  public void testReducer() {

    reduceDriver.setInputKey(new Text("this"));

    reduceDriver.setInputValues(Arrays.asList(new TextIntPairWritable(new Text(
        "ID123"), new IntWritable(4))));

    reduceDriver.addOutput(new Text("ID123"), new TextIntIntIntWritable(
        new Text("this"), new IntWritable(1), new IntWritable(4),
        new IntWritable(0)));

    reduceDriver.runTest();
  }
}
