import java.util.*;

public class APriori {

    // a transaction is a set of strings; transactions can look identical;
    // hence an abstract data type  that allows duplicates
    private List<Set<String>> transactionList;

    public APriori(List<Set<String>> transactions) {
        this.transactionList = transactions;
    }

    public Set<Set<String>> getFrequentItemSets (double minSupport){
//        System.err.println("Running getFrequentItemSets");
        int k = 1;
        Set<Set<String>> currentFrequentItemSets = getFrequentOneItemsets(minSupport);

        Set<Set<String>> union = new HashSet<>(currentFrequentItemSets);

        while (!currentFrequentItemSets.isEmpty()){

            List<Set<String>> candidateItems = generateCandidateItemsets(currentFrequentItemSets, k);
            Set<Set<String>> prunedItemsets = pruneItemsetsFromCandidateItemsets(candidateItems, currentFrequentItemSets, k);
            Set<Set<String>> nextFrequentItemsets = getFinalFrequentItemSets(prunedItemsets, minSupport);

            union.addAll(currentFrequentItemSets);

            k++;
            currentFrequentItemSets = nextFrequentItemsets;
        }

        return union;

    }

    private Set<Set<String>> getFrequentOneItemsets (double minSupport){
//        System.err.println("Running getFrequentOneItemsets");

        Map<String, Integer> items = new HashMap<>();

        //get all items
        for (Set<String> transaction : transactionList) {
            for (String item : transaction) {
                items.merge(item, 1, Integer::sum); //lambda replaced with method reference - IntelliJ suggestion
            }
        }

        // of all items, which ones have >= threshold frequency ?
        Set<Set<String>> frequentOneItemsets = new HashSet<>();

        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            if (entry.getValue() >= minSupport) {
                frequentOneItemsets.add(Collections.singleton(entry.getKey()));
            }
        }

        return frequentOneItemsets;

    }

    // generating C_{k+1} by joining itemset-pairs in F_k  --> coming back to this tomorrow as well
    private List<Set<String>> generateCandidateItemsets (Set<Set<String>> previousFrequentItemsets, int k){

//        System.err.println("Running getCandidateItemsets");
        List<Set<String>> candidateItemsets = new ArrayList<>();

        int size = previousFrequentItemsets.size();

        List<List<String>> sortedPreviousFrequentItemsets = new ArrayList<>(size);

        for (Set<String> itemset : previousFrequentItemsets) {
            List<String> sortedList = new ArrayList<>(itemset);
            Collections.sort(sortedList);
            sortedPreviousFrequentItemsets.add(sortedList);
        }

        // this loop should now skip duplicates
        for (int i = 0; i < size; i++) {
            List<String> itemset_i = sortedPreviousFrequentItemsets.get(i);
            for (int j = i + 1; j < size; j++) {
                List<String> itemset_j = sortedPreviousFrequentItemsets.get(j);

                boolean canJoin = true;

                for (int m = 0; m < k-1; m++){
                    if (!itemset_i.get(m).equals(itemset_j.get(m))){
                        canJoin = false;
                        break;
                    }
                }

                if (canJoin){
                        Set<String> candidate = new HashSet<>(itemset_i);
                        candidate.addAll(itemset_j);
                        if (candidate.size() == k + 1){
                            candidateItemsets.add(candidate);
                        }
                }
            }
        }

        return candidateItemsets;
    }

    //prune all candidates that violate downward closure
    private Set<Set<String>> pruneItemsetsFromCandidateItemsets (List<Set<String>> candidateItemsetsList,
                                                                  Set<Set<String>> previousFrequentItemsets, int k){
//        System.err.println("Running getCandidateItemsets");
        if (candidateItemsetsList.isEmpty()){
            return new HashSet<>();
        }
        Set<Set<String>> approvedCandidates = new HashSet<>();

        for (Set<String> candidate : candidateItemsetsList) {
            // Generate all k-item subsets of the candidate -- a tree? stack dfs?
            Set<Set<String>> subsets = generateSubsets(candidate, k);
            boolean allSubsetsFrequent = true;
            // Check that each subset is indeed frequent (exists in Fk).
            for (Set<String> subset : subsets) {
                if (!previousFrequentItemsets.contains(subset)) {
                    allSubsetsFrequent = false;
                    break;
                }
            }
            // Only keep the candidate if all of its k-sized subsets are frequent.
            if (allSubsetsFrequent) {
                approvedCandidates.add(candidate);
            }
        }
        return approvedCandidates;
    }

    private Set<Set<String>> getFinalFrequentItemSets(Set<Set<String>> candidateItemsets, double minSupport) {
        Set<Set<String>> frequentItemsets = new HashSet<>();

        // For each candidate, count the support by scanning all transactions.
        for (Set<String> candidate : candidateItemsets) {
            int count = 0;
            for (Set<String> transaction : transactionList) {
                if (transaction.containsAll(candidate)) {
                    count++;
                }
            }
            // Retain candidate if its frequency is over the threshold.
            if (count >= minSupport) {
                frequentItemsets.add(candidate);
            }
        }
        return frequentItemsets;
    }





    // If a data structure can help, one place will be here.
    private Set<Set<String>> generateSubsets(Set<String> set, int subsetSize) {
        Set<Set<String>> allSubsets = new HashSet<>();
        List<String> list = new ArrayList<>(set);
        generateSubsetsRecursive(list, subsetSize, 0, new HashSet<>(), allSubsets);
        return allSubsets;
    }

    // this recursion...
    private void generateSubsetsRecursive(List<String> list, int subsetSize, int index,
                                          Set<String> current, Set<Set<String>> allSubsets) {
        if (current.size() == subsetSize) {
            allSubsets.add(new HashSet<>(current));
            return;
        }
        if (index >= list.size()) {
            return;
        }
        // Include the element at current index.
        current.add(list.get(index));
        generateSubsetsRecursive(list, subsetSize, index + 1, current, allSubsets);
        // Exclude the element and move on.
        current.remove(list.get(index));
        generateSubsetsRecursive(list, subsetSize, index + 1, current, allSubsets);
    }

}
