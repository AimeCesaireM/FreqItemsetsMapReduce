import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class RoundOneReducer extends Reducer<Text, IntWritable, Text, NullWritable> {

    @Override
    public void reduce(Text keys, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {

        String[] itemSetsStrings = keys.toString().split("\n");



        int sum = 0;
        // sum the counts for this candidate key
        for (IntWritable val : values) {
            sum += val.get();
        }
        // use to check that the candidate appeared at least once.
        if (sum > 0) {
            context.write(key, NullWritable.get());
        }
    }
}
