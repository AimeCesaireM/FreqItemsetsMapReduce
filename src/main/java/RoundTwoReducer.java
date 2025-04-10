import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import java.io.IOException;

public class RoundTwoReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    private double minSupport;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        // Retrieve global minimum support from configuration.
        Configuration conf = context.getConfiguration();
        minSupport = conf.getDouble("min_support", 1);
    }

    @Override
    public void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {
//        System.err.println("Round 2 Reducer Reading Key : " + key);
        int sum = 0;
        // Sum the local counts for each candidate itemset.
        for (IntWritable val : values) {
//            System.err.println("\t Round 2 Reducer Value : " + val);
            sum += val.get();
        }
        // Output the candidate only if its total support meets or exceeds the global threshold.
        if (sum >= minSupport) {
//            System.err.println("Round 2 Reducer Single Writing:\t Key : " + key + " Value : " + sum);
            context.write(key, new IntWritable(sum));
        }
    }
}
