import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class SONMRMulti {

    public static void main(String[] args) throws Exception {
        if (args.length != 6) {
            System.err.println("Error: Exactly 6 arguments are required.");
            System.err.println("Usage: java FrequentItemsetMining <dataset_size> <transactions_per_block> <min_freq> <input_path> <interm_path> <output_path>");
            return;
        }

        int datasetSize, transactionsPerBlock;
        double minFreq;
        double minSupport;
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

        minSupport = (int) (minFreq * datasetSize);


        Configuration conf = new Configuration();
        conf.setInt("dataset_size", datasetSize);
        conf.setInt("transactions_per_block", transactionsPerBlock);
        conf.setDouble("min_freq", minFreq);
        conf.setDouble("min_support", minSupport);

        // Round One

        Job roundOneJob = Job.getInstance(conf, "SONMRMulti Round One");
        roundOneJob.setInputFormatClass(MultiLineInputFormat.class);
        org.apache.hadoop.mapreduce.lib.input.NLineInputFormat.setNumLinesPerSplit(roundOneJob, transactionsPerBlock);
        roundOneJob.setJarByClass(SONMRMulti.class);
        roundOneJob.setMapperClass(RoundOneMapper.class);
        roundOneJob.setReducerClass(RoundOneReducer.class);


        roundOneJob.setOutputKeyClass(Text.class);
        roundOneJob.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(roundOneJob, inputPath);
        FileOutputFormat.setOutputPath(roundOneJob, intermPath);

        double startTime = System.currentTimeMillis();
        roundOneJob.waitForCompletion(false);
        double endTime = System.currentTimeMillis();
        double elapsed_time = endTime - startTime;


        // Round Two

        Job roundTwoJob = Job.getInstance(conf, "SONMRMulti Round Two");

        String cacheFilePathAsString = intermPath + "/part-r-00000"; //intelli-J thinks toString() is redundant
        Path cacheFilePath = new Path(cacheFilePathAsString);
        roundTwoJob.addCacheFile(cacheFilePath.toUri());

        roundTwoJob.setInputFormatClass(MultiLineInputFormat.class);
        org.apache.hadoop.mapreduce.lib.input.NLineInputFormat.setNumLinesPerSplit(roundTwoJob, transactionsPerBlock);
        roundTwoJob.setJarByClass(SONMRMulti.class);
        roundTwoJob.setMapperClass(RoundTwoMapperMulti.class);
        roundTwoJob.setCombinerClass(RoundTwoCombiner.class );
        roundTwoJob.setReducerClass(RoundTwoReducer.class);


        roundTwoJob.setOutputKeyClass(Text.class);
        roundTwoJob.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(roundTwoJob, inputPath);

        FileOutputFormat.setOutputPath(roundTwoJob, outputPath);

        startTime = System.currentTimeMillis();
        roundTwoJob.waitForCompletion(false);
        endTime = System.currentTimeMillis();
        elapsed_time += endTime - startTime;

        System.err.println("Elapsed time: " + elapsed_time + " ms");
        System.exit(0);
    }
}
