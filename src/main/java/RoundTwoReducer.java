import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class RoundTwoReducer extends Reducer<Text, IntWritable,Text,IntWritable> {
    double minSupport;

    protected void setup(Context context){
        Configuration conf = context.getConfiguration();
        minSupport = Double.parseDouble(conf.get("min_support"));


    }


    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
    ) throws IOException, InterruptedException {


        // we will do something different here...
        int count = 0;

        for (IntWritable val : values) {
            count += val.get();
        }

        if (count > minSupport) {
            context.write(key, new IntWritable(count));
        }
    }
}