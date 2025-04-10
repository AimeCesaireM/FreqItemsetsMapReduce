import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.net.URI;
import java.util.*;


public class RoundOneMapper
        extends Mapper<Object, Text, Text, IntWritable> {
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    int datasetSize, transactionsPerBlock;
    double minFreq, p;
    double minSupport;

    HashSet<String> swSet = new HashSet<>();


    public void setup(Context context) throws IOException {
        Configuration conf = context.getConfiguration();
        datasetSize = Integer.parseInt(conf.get("dataset_size"));
        transactionsPerBlock = Integer.parseInt(conf.get("transactions_per_block"));
        minFreq = Double.parseDouble(conf.get("min_freq"));
        p = transactionsPerBlock/(double)datasetSize;
        minSupport = Double.parseDouble(conf.get("min_support"));

        URI[] cacheFiles = context.getCacheFiles();

    }

    public void map(Object key, Text value, Context context
    ) throws IOException, InterruptedException {
        // map method here
        String batch = value.toString();
        System.err.println("batch: " + batch);

        List<Set<String>> basketSubset = new ArrayList<>();

        List<String> lines = Arrays.asList(batch.split("\n"));

        for (String line : lines) {
            Set<String> transaction = new HashSet<>(List.of(line.split(" ")));
            basketSubset.add(transaction);
        }

        double adjustedMinSupport = (p * minSupport);

        String frequentItemsets = new APriori(basketSubset).getFrequentItemSets(adjustedMinSupport);
        word.set(frequentItemsets);
        context.write(word, one);


    }
}