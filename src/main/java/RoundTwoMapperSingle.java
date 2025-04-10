import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

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
    double minSupport;
    List<Set<String>> candidateSets = new ArrayList<>();


    public void setup(Context context) throws IOException {
        Configuration conf = context.getConfiguration();
        datasetSize = Integer.parseInt(conf.get("dataset_size"));
        transactionsPerBlock = Integer.parseInt(conf.get("transactions_per_block"));
        minFreq = Double.parseDouble(conf.get("min_freq"));
        minSupport = Double.parseDouble(conf.get("min_support"));

        URI[] cacheFiles = context.getCacheFiles();
        if (cacheFiles != null && cacheFiles.length > 0) {
            // only my file is in here
            Path candidatePath = new Path(cacheFiles[0].getPath());
            File candidateFile = new File(String.valueOf(candidatePath));
//            System.err.println(candidateFile.getAbsolutePath());
//            System.err.println(Arrays.toString(cacheFiles));
            BufferedReader reader = new BufferedReader(new FileReader(candidateFile));
            String line;
            // Clean up to make sure output isn't coming with set.toString() artifacts
            // Possibly unnecessary now that I changed how to write to the context
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                // Remove any optional square brackets if present.
                if (line.startsWith("[") && line.endsWith("]")) {
                    line = line.substring(1, line.length() - 1).trim();
                }
                // Split candidate items by whitespace.
                String[] items = line.split("\\s+");
                // Create a candidate set and add it to our list.
                Set<String> candidate = new HashSet<>(Arrays.asList(items));
                candidateSets.add(candidate);
            }
            reader.close();
        }

    }

    public void map(Object key, Text value, Context context
    ) throws IOException, InterruptedException {
        // It gets one itemset
        System.err.println("Round Two Mapper Single Reading Key");
        System.err.println("Round Two Mapper Single Reading Value:" + value);
        String[] items = value.toString().split("\\s+");

        Set<String> basket = new HashSet<>(Arrays.asList(items));

//        System.err.println("Round Two Mapper Single candidateSets:" + candidateSets);

        for (Set<String> candidate : candidateSets) {
//            System.err.println("Basket: " + basket);
//            System.err.println("Candidate: " + candidate);
            if (basket.containsAll(candidate)) {

                //Preparing the transaction to be written to the context
                StringBuilder builder = new StringBuilder();

                for (String item : candidate) {
                    builder.append(item);
                    builder.append(" ");
                }
                word.set(builder.toString());
//                System.err.println("Round Two Mapper Single Writing Key:" + word);
                context.write(word, one);
            }
        }
    }
}