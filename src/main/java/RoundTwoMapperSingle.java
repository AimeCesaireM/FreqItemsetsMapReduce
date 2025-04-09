import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.yarn.webapp.hamlet2.Hamlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.*;

public class RoundTwoMapperSingle
        extends Mapper<Object, Text, Text, IntWritable> {
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    int datasetSize, transactionsPerBlock;
    double minFreq;
    List<Set<String>> candidateSets = new ArrayList<>();


    public void setup(Context context) throws IOException {
        Configuration conf = context.getConfiguration();
        datasetSize = Integer.parseInt(conf.get("dataset_size"));
        transactionsPerBlock = Integer.parseInt(conf.get("transactions_per_block"));
        minFreq = Double.parseDouble(conf.get("min_freq"));

        URI[] cacheFiles = context.getCacheFiles();
        if (cacheFiles != null) {
            File file = new File(cacheFiles[0].getPath());
            BufferedReader reader = new BufferedReader(new FileReader(file));

            // every line is a transaction
            String line = reader.readLine();
            while (line != null) {
                String[] items = line.split(" ");
                Set<String> candidate = new HashSet<>(Arrays.asList(items));
                candidateSets.add(candidate);
                line = reader.readLine();
            }
            reader.close();

        }

    }

    public void map(Object key, Text value, Context context
    ) throws IOException, InterruptedException {
        String[] items = value.toString().split(" ");
        Set<String > basket = new HashSet<>(Arrays.asList(items));
        // map method here

        for (Set<String> set : candidateSets) {
            if (basket.containsAll(set)){
                word.set(set.toString());
                context.write(word, one);
            }
        }
    }
}