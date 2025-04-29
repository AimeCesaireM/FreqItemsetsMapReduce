import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import java.io.IOException;


public class RoundTwoCombiner extends Reducer<Text, IntWritable, Text, IntWritable> {
    @Override
    protected void reduce(Text key, Iterable<IntWritable> values,
                          Context context) throws IOException, InterruptedException {
//        System.err.println("Round Two Combiner reading key: " + key.toString());
//        System.err.println("Round Two Combiner reading values: ");

        int localSum = 0;

        for (IntWritable val : values) {
//            System.err.println("\t" + val.toString());
            localSum += val.get();
//            System.err.println(" Adding " + localSum);
        }
//        System.err.println("Round Two Combiner localSum: " + localSum);
        context.write(key, new IntWritable(localSum));
    }
}