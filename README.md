# SON Frequent Itemset Mining with Hadoop MapReduce

## Project Description
This project implements the **SON (Savasere–Omiecinski–Navathe)** algorithm for frequent itemset mining using **Hadoop MapReduce**. It provides two driver variants:

- **SONMRSingle**: Processes one transaction per map invocation, suitable for cases with smaller or streaming-like inputs.
- **SONMRMulti**: Processes a configurable number of transactions per map invocation using a custom `MultiLineInputFormat`, improving performance on large block-based datasets.

Both variants leverage an **APriori**-based candidate generation (`APriori.java`) in the first MapReduce round to identify local frequent itemsets, followed by a global support counting phase in the second round to output the final frequent itemsets.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Installation](#installation)
3. [Running the Project](#running-the-project)
   - [SONMRSingle](#sonmrsingle)
   - [SONMRMulti](#sonmrmmulti)
4. [Usage](#usage)
5. [Project Structure](#project-structure)
6. [Contributing](#contributing)
7. [Credits](#credits)
8. [License](#license)

## Prerequisites
- Java JDK 11 or higher (managed via IntelliJ IDEA or installed natively)
- IntelliJ IDEA (Community or Ultimate) with Maven support
- Apache Hadoop 2.x or higher accessible via your classpath

## Setup and Running
1. **Clone the repository**
   ```bash
   git clone https://github.com/<your-username>/son-frequent-itemset-mining.git
   ```
2. **Import into IntelliJ**
   - Open IntelliJ IDEA.
   - Choose **Open** and select the cloned project folder.
   - IntelliJ will detect the Maven `pom.xml` and import dependencies automatically.
3. **Configure Run Configurations**
   - Create a new **Application** configuration.
   - For the **Main class**, select either `SONMRSingle` or `SONMRMulti`.
   - In **Program arguments**, specify:
     ```
     <dataset_size> <transactions_per_block> <min_freq> <input_path> <interm_path> <output_path>
     ```
4. **Run the Job**
   - Ensure your Hadoop cluster or local Hadoop installation is running.
   - Run the chosen configuration. The console output will display the frequent itemsets and elapsed time.

## Usage
1. **Upload** your transaction dataset to HDFS (each line is a whitespace-separated list of items).
2. **Invoke** one of the driver commands above.
3. **View** the output in `<output_ClassSuffix>`, where each `ClassSuffix` is either `Single` or `Multi`. Each line contains a frequent itemset and its global support count.

### Example
Assuming a dataset of 1,000,000 transactions, 10,000 per block, and a 0.05 minimum frequency:
```bash
hadoop jar son-fim.jar \
  SONMRMulti \
  1000000 \
  10000 \
  0.05 \
  /data/transactions.txt \
  /tmp/son-interm \
  /results/frequent-itemsets
```

## Project Structure
```
├── src/
│   ├── APriori.java              # Serial frequent-itemset mining
│   ├── APrioriTest.java          # Local unit test for APriori
│   ├── RoundOneMapper.java       # Mapper for local candidate generation
│   ├── RoundOneReducer.java      # Reducer for deduplicating candidates
│   ├── RoundTwoMapperSingle.java # Mapper for global support (single)
│   ├── RoundTwoMapperMulti.java  # Mapper for global support (multi)
│   ├── RoundTwoCombiner.java     # Local aggregator for support counts
│   ├── RoundTwoReducer.java      # Reducer for global support thresholding
│   ├── SONMRSingle.java          # Driver for single-transaction variant
│   └── SONMRMulti.java           # Driver for multi-transaction variant
├── pom.xml (optional)            # Maven build configuration
└── README.md                     # Project documentation
```

## Contributing
Contributions and suggestions are welcome! Please:
1. Fork the repository.
2. Create a new branch (`git checkout -b feature-description`).
3. Commit your changes (`git commit -m "Add feature"`).
4. Push to your fork (`git push origin feature-description`).
5. Submit a Pull Request.

## Credits
- **Algorithm design**: R. Agrawal & R. Srikant, "Fast algorithms for mining association rules" (APriori)
- **Hadoop MapReduce framework**
- [Apache Hadoop Documentation](https://hadoop.apache.org/) for setup and configuration
- Project lead: *Aime Cesaire Mugishawayo*
- Contributors: [List collaborators and GitHub profiles]
