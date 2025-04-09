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

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class SONMRSingle {

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
        conf.setInt("dataset_size", datasetSize);
        conf.setInt("transactions_per_block", transactionsPerBlock);
        conf.setDouble("min_freq", minFreq);

        //Round One

        Job roundOneJob = Job.getInstance(conf, "SONMRSingle");
        roundOneJob.setInputFormatClass(MultiLineInputFormat.class);
        org.apache.hadoop.mapreduce.lib.input.NLineInputFormat.setNumLinesPerSplit(roundOneJob, transactionsPerBlock);
        roundOneJob.setJarByClass(SONMRSingle.class);
        roundOneJob.setMapperClass(RoundOneMapper.class);
        roundOneJob.setCombinerClass(RoundOneReducer.class); // Not Very Sure here,... coming back to this
        roundOneJob.setReducerClass(RoundOneReducer.class);


        roundOneJob.setOutputKeyClass(Text.class);
        roundOneJob.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(roundOneJob, inputPath);
        FileOutputFormat.setOutputPath(roundOneJob, intermPath);

        // Might have to figure out some other stuff here

        // Round Two

        Job roundTwoJob = Job.getInstance(conf, "SONMRSingle");
        roundTwoJob.setInputFormatClass(MultiLineInputFormat.class);
        org.apache.hadoop.mapreduce.lib.input.NLineInputFormat.setNumLinesPerSplit(roundTwoJob, transactionsPerBlock);
        roundTwoJob.setJarByClass(SONMRSingle.class);
        roundTwoJob.setMapperClass(RoundTwoMapperSingle.class);
        roundTwoJob.setCombinerClass(RoundTwoReducer.class); // Not Very Sure here,... coming back to this
        roundTwoJob.setReducerClass(RoundTwoReducer.class);


        roundTwoJob.setOutputKeyClass(Text.class);

        roundTwoJob.setOutputValueClass(NullWritable.class); // per the hint from Moodle


        FileInputFormat.addInputPath(roundTwoJob, inputPath); // Not sure if this is necessary

        String cacheFilePathAsString = intermPath.toString() + "/part-r-00000";
        Path cacheFilePath = new Path(cacheFilePathAsString);
        roundTwoJob.addCacheFile(cacheFilePath.toUri());

        FileOutputFormat.setOutputPath(roundTwoJob, outputPath);



        System.exit(roundOneJob.waitForCompletion(true) && roundTwoJob.waitForCompletion(true) ? 0 : 1);
    }
}
