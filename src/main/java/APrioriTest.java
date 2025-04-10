import java.util.*;

public class APrioriTest {
    public static void main(String[] args) {
        List<Set<String>> transactionList = new ArrayList<>();

        String input = "1 2 3 4\n" +
                "2 5\n" +
                "1 2 4\n" +
                "2 4 5\n";

        String[] transactions = input.split("\n");
        for (String transaction : transactions) {
            Set<String> transactionSet = new HashSet<>(Arrays.asList(transaction.split(" ")));
            transactionList.add(transactionSet);
        }
//        System.out.println(transactionList);

        int minSupport = 2;

        String frequentItemsets = new APriori(transactionList).getFrequentItemSets(minSupport).toString();

        System.out.println(frequentItemsets);



    }
}
