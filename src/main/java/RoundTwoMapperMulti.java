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
import java.util.stream.Collectors;

/**
 * RoundTwoMapper implements the second map phase of the SON algorithm.
 *
 * This phase takes the candidate itemsets (generated by the first MapReduce job
 * using a simple, randomized algorithm as described in Section 6.4.1 of the textbook)
 * and a portion of the input basket data. For every candidate, this mapper checks
 * if it is a subset of the basket and if so, emits a key–value pair (candidate, 1).
 *
 * The reducer will later sum all these values to obtain the global support for each candidate.
 */
public class RoundTwoMapperMulti extends Mapper<Object, Text, Text, IntWritable> {

    // Constant value for each occurrence of a candidate in a basket.
    private final static IntWritable one = new IntWritable(1);

    // Output key for candidate itemset.
    private Text word = new Text();

    // List of candidate itemsets loaded from the distributed cache.
    // Each candidate is represented as a Set<String>
    private List<Set<String>> candidateItemsets = new ArrayList<>();

    /**
     * Setup: Load the candidate itemsets (output from the first reduce phase)
     * from the distributed cache file.
     *
     * Each candidate is assumed to be stored on one line with items separated
     * by whitespace. (For example: "1 2 4")
     */
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        URI[] cacheFiles = context.getCacheFiles();
        if (cacheFiles != null && cacheFiles.length > 0) {
            // only my file is in here
            Path candidatePath = new Path(cacheFiles[0].getPath());
            File candidateFile = new File(String.valueOf(candidatePath));
//            System.err.println(candidateFile.getAbsolutePath());
//            System.err.println(Arrays.toString(cacheFiles));
            BufferedReader reader = new BufferedReader(new FileReader(candidateFile));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Been having trouble with formatting reads and writes to the context.
                if (line.startsWith("[") && line.endsWith("]")) {
                    line = line.substring(1, line.length() - 1).trim();
                }
                // Split candidate items by whitespace.
                String[] items = line.split("\\s+");
                // Create a candidate set and add it to our list.
                Set<String> candidate = new HashSet<>(Arrays.asList(items));
                candidateItemsets.add(candidate);
            }
            reader.close();
        }
    }

    /**
     * Map: Process each basket (transaction) from the input.
     *
     * For every basket, check each candidate itemset loaded in setup.
     * If the candidate is a subset of the basket, emit the candidate along
     * with a count of 1.
     */
    @Override
    protected void map(Object key, Text value, Context context)
            throws IOException, InterruptedException {
        // Each input record represents a basket (transaction).
//        System.err.println("Round Two Mapper Multi Reading Key");
//        System.err.println("Round Two Mapper Multi Reading Value: " + value);

        String[] lines = value.toString().split("\n");

        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty()) return;

            String[] items = line.split("\\s+");
            Set<String> basket = new HashSet<>(Arrays.asList(items));

            Set<Set<String>> subsets = new HashSet<>();
            subsets.add(basket);

            // For every candidate, check if it is contained in the basket.
            for (Set<String> candidate : candidateItemsets) {
                // so we do not check duplicates
                if (subsets.contains(candidate)){
                    String candidateKey = candidate.stream().sorted().collect(Collectors.joining(" "));
                    word.set(candidateKey);
                    context.write(word, one);
                    return;
                }

                if (basket.containsAll(candidate)) {
                    subsets.add(candidate);
                    String candidateKey = candidate.stream().sorted().collect(Collectors.joining(" "));
                    word.set(candidateKey);
                    context.write(word, one);
                }
            }
        }
    }
}
