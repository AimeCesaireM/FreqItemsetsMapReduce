import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.StringTokenizer;

public class RoundOneMapper
        extends Mapper<Object, Text, Text, IntWritable> {
    private final static IntWritable one = new IntWritable(1);
    private int threshold;
    private Text word = new Text();

    int datasetSize, transactionsPerBlock;
    double minFreq, p;

    HashSet<String> swSet = new HashSet<>();


    public void setup(Context context) throws IOException {
        Configuration conf = context.getConfiguration();
        datasetSize = Integer.parseInt(conf.get("dataset_size"));
        transactionsPerBlock = Integer.parseInt(conf.get("transactions_per_block"));
        minFreq = Double.parseDouble(conf.get("min_freq"));
        p = transactionsPerBlock/(double)datasetSize;

        // something here


        URI[] cacheFiles = context.getCacheFiles();

    }

    public void map(Text key, Text value, Context context
    ) throws IOException, InterruptedException {
        // map method here
        String batch = value.toString();

        List<Set<String>> basketSubset = new ArrayList<>();

        List<String> lines = Arrays.asList(batch.split("\n"));

        for (String line : lines) {
            Set<String> transaction = new HashSet<>(List.of(line.split("")));
            basketSubset.add(transaction);
        }

        Set<Set<String>> frequentItemsets = new APriori(basketSubset).getFrequentItemSets(p * minFreq);
        word.set(frequentItemsets.toString());
        context.write(word, one);


    }
}