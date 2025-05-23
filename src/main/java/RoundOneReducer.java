import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class RoundOneReducer extends Reducer<Text, IntWritable, Text, NullWritable> {

    @Override
    public void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {
//        System.err.println("Round One Reducer Reading/Writing Key: " + key);
        context.write(key, NullWritable.get());
    }
}
