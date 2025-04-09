import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;

public class RoundTwoMapperMulti
        extends Mapper<Object, Text, Text, IntWritable> {
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    int datasetSize, transactionsPerBlock;
    double minFreq;


    public void setup(Context context) throws IOException {
        Configuration conf = context.getConfiguration();
        datasetSize = Integer.parseInt(conf.get("dataset_size"));
        transactionsPerBlock = Integer.parseInt(conf.get("transactions_per_block"));
        minFreq = Double.parseDouble(conf.get("min_freq"));

        // to figure out

        URI[] cacheFiles = context.getCacheFiles();

    }

    public void map(Object key, Text value, Context context
    ) throws IOException, InterruptedException {
        // map method here
    }
}