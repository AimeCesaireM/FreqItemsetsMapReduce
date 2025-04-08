import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.StringTokenizer;

public class RoundOneMapper
        extends Mapper<Object, Text, Text, IntWritable> {
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
    private int minlen, maxlen;

    HashSet<String> swSet = new HashSet<>();


    public void setup(Context context) throws IOException {
        Configuration conf = context.getConfiguration();
        minlen = conf.getInt("minlen", 3);
        maxlen = conf.getInt("maxlen", 10);

        URI[] cacheFiles = context.getCacheFiles();
        String swPath = cacheFiles[0].toString();

        File swFile = new File(swPath);
        if (swFile.canRead()) {
            BufferedReader reader = new BufferedReader(new FileReader(swFile));
            String line = reader.readLine();
            swSet.add(line);
        }
    }

    public void map(Object key, Text value, Context context
    ) throws IOException, InterruptedException {
        // map method here
    }
}