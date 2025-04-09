import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class RoundTwoReducer extends Reducer<Text, IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();
    private double minFreq;
    private int transactionsPerBlock;

    protected void setup(Context context){
        Configuration conf = context.getConfiguration();
        minFreq = Double.parseDouble(conf.get("min_freq"));
        transactionsPerBlock = Integer.parseInt(conf.get("transactions_per_block"));


    }


    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
    ) throws IOException, InterruptedException {


        // we will do something different here...
        int count = 0;

        for (IntWritable val : values) {
            count += val.get();
        }

        double freq = (double) count / transactionsPerBlock;

        if (freq > minFreq) {
            context.write(key, new IntWritable(count));
        }
    }
}