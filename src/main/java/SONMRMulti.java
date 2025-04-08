import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class SONMRMulti {

    public void setup(Mapper.Context context) throws IOException {
        Configuration conf = context.getConfiguration();
//        minlen = conf.getInt("minlen", 3);
//        maxlen = conf.getInt("maxlen", 10);

        URI[] cacheFiles = context.getCacheFiles();
        String swPath = cacheFiles[0].toString();

        File swFile = new File(swPath);
        if (swFile.canRead()) {
            BufferedReader reader = new BufferedReader(new FileReader(swFile));
            String line = reader.readLine();
//            swSet.add(line);
        }
    }


    public static void main(String[] args) throws Exception {
        if (args.length != 6) {
            System.err.println("Error: Exactly 6 arguments are required.");
            System.err.println("Usage: java FrequentItemsetMining <dataset_size> <transactions_per_block> <min_freq> <input_path> <interm_path> <output_path>");
            return;
        }

        int datasetSize, transactionsPerBlock;
        double minFreq;
        Path inputPath, intermPath, outputPath;

        try {
            // Parse and validate arguments
            datasetSize = Integer.parseInt(args[0]);
            transactionsPerBlock = Integer.parseInt(args[1]);
            minFreq = Double.parseDouble(args[2]);
            inputPath = new Path(args[3]);
            intermPath = new Path(args[4]);
            outputPath = new Path(args[5]);

            if (datasetSize <= 0 || transactionsPerBlock <= 0) {
                System.err.println("Error: dataset_size and transactions_per_block must be positive integers.");
                return;
            }

            if (minFreq < 0 || minFreq >= 1) {
                System.err.println("Error: min_freq must be in the range [0, 1).");
                return;
            }


        } catch (NumberFormatException e) {
            System.err.println("Error: dataset_size and transactions_per_block must be integers, min_freq must be a decimal.");
            return;
        }


        Configuration conf = new Configuration();
        conf.setInt("minlen", minlen);
        conf.setInt("maxlen", maxlen);

        Job job = Job.getInstance(conf, "SONMRMulti");
        job.setInputFormatClass(MultiLineInputFormat.class);
        org.apache.hadoop.mapreduce.lib.input.NLineInputFormat.setNumLinesPerSplit(job, transactionsPerBlock); //numLines is one of the command-line args
        job.setJarByClass(SONMRMulti.class);
        job.setMapperClass(RoundOneMapper.class);
        job.setCombinerClass(RoundOneReducer.class); // Not Very Sure here,... coming back to this
        job.setReducerClass(RoundOneReducer.class);

        /*
        * Another round of MapReduce??
        *
        *
        * */


        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, inputPath);
        FileOutputFormat.setOutputPath(job, outputPath);
        job.addCacheFile(swPath.toUri());
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
