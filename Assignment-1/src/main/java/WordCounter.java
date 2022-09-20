import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;



public class WordCounter {

    private static ArrayList<String> list = new ArrayList<>();
	
public void loadStopWords() throws IOException {
    	
    	BufferedReader bufReader = new BufferedReader(new FileReader("./data files/stopwords.txt"));
        

        String line = bufReader.readLine();
        while (line != null) {
          list.add(line);
          line = bufReader.readLine();
        }
        bufReader.close();
    }

  public static class MyMapper 
       extends Mapper<Object, Text, Text, IntWritable>{
    
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text(); 
    private Map<Text, IntWritable> countMap = new HashMap<Text, IntWritable>();
    
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString());
//      System.out.println(value);
      String eachWord=new String();
      
      while (itr.hasMoreTokens()) {
    	eachWord=itr.nextToken().replaceAll("\\p{Punct}", "").toLowerCase();
    	
//    	eachWord.replaceAll("\\p{Punct}", "").toLowerCase().split("\\s+");
//    	System.out.println(eachWord);
    	if(!list.contains(eachWord)) {
            word.set(eachWord);
            context.write(word, one);
    	}
      }
    }
  }
  
  public static class MyReducer 
       extends Reducer<Text,IntWritable,Text,IntWritable> {
	  
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values, 
                       Context context
                       ) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
	  
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    
    Job job = new Job(conf, "word count");
    new WordCounter().loadStopWords();
    job.setJarByClass(WordCounter.class);
    job.setMapperClass(MyMapper.class);
    job.setReducerClass(MyReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
