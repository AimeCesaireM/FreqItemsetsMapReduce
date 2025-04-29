import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RoundOneMapper extends Mapper<Object, Text, Text, IntWritable> {

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    private int datasetSize;
    private int transactionsPerBlock;
    private double minSupport;
    private double p;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        Configuration conf = context.getConfiguration();
        datasetSize = conf.getInt("dataset_size", 0);
        transactionsPerBlock = conf.getInt("transactions_per_block", 0);
        minSupport = conf.getDouble("min_support", 0);

        // p: the proportion of the dataset seen by this mapper.
        p = transactionsPerBlock / (double) datasetSize;
    }

    @Override
    public void map(Object key, Text value, Context context)
            throws IOException, InterruptedException {
//         Each input record is assumed to be a block (chunk) of baskets,
//         with each basket on a separate line.
//        System.err.println("Round One Mapper Reading Key: " + key);
//        System.err.println("Round One Mapper Reading Value: " + value);
        String block = value.toString().trim();
        if (block.isEmpty()) {
            return;
        }

        // Break the block into lines
        String[] lines = block.split("\n");
        List<Set<String>> baskets = new ArrayList<>();
        // each line is a transaction
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {

                String[] items = line.split("\\s+");
                // Build basket/transaction as a set of items.
                Set<String> basket = new HashSet<>(Arrays.asList(items));
                baskets.add(basket);
            }
        }

        double effectiveMinSupport = Math.max(1, Math.round(p * minSupport));

        Set<Set<String>> frequentItemsets = new APriori(baskets).getFrequentItemSets(effectiveMinSupport);

        for (Set<String> candidate : frequentItemsets) {

            // a better way to iterate through the set and build a string
            String candidateKey = candidate.stream().sorted().collect(Collectors.joining(" "));
            word.set(candidateKey);
//            System.err.println("Round One Mapper Writing Key: " + word);
            context.write(word, one);
        }
    }
}
