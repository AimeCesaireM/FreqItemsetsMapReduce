import org.xbill.DNS.ZoneMDRecord;

import java.util.*;

public class APriori {

    // a transaction is a set of strings; transactions can look identical;
    // hence an abstract data type  that allows duplicates
    private List<Set<String>> transactionList;
//    private int transactionCount;

    private List<Set<String>> itemSets;



    public APriori(List<Set<String>> transactions) {
        this.transactionList = transactions;
    }

    //APriori algorithm,
    public Set<Set<String>> getFrequentItemSets (int frequencyThreshold){
        int k = 1;
        Set<Set<String>> union = new HashSet<>();

        Set<Set<String>> currentFrequentItemSets = getFrequentOneItemsets(frequencyThreshold);

        while (!currentFrequentItemSets.isEmpty()){
            union.addAll(currentFrequentItemSets);
            List<Set<String>> candidateItems = generateCandidateItemsets(currentFrequentItemSets, k);
            Set<Set<String>> prunedItemsets = pruneItemsetsFromCandidateItemsets(candidateItems, currentFrequentItemSets, k);
            currentFrequentItemSets = getFinalCandidateItemsets(prunedItemsets, frequencyThreshold);

            k++;
        }

        return union;

    }

    private Set<Set<String>> getFrequentOneItemsets (double frequencyThreshold){

        Map<String, Integer> items = new HashMap<>();
        int size = transactionList.size();


        //get all items
        for (Set<String> transaction : transactionList) {
            for (String item : transaction) {
                items.merge(item, 1, Integer::sum); //lambda replaced with method reference - IntelliJ suggestion
            }
        }

        // of all items, which ones have >= threshold frequency ? need to check logic when I'm less sleepy
        Set<Set<String>> frequentOneItemsets = new HashSet<>();
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            double entryFreq = (double) entry.getValue()/size;
            if (entryFreq >= frequencyThreshold) {
                Set<String> itemset = new HashSet<>();
                itemset.add(entry.getKey());
                frequentOneItemsets.add(itemset);
            }
        }

        return frequentOneItemsets;

    }

    // generating C_{k+1} by joining itemset-pairs in F_k  --> coming back to this tomorrow as well
    private List<Set<String>> generateCandidateItemsets (Set<Set<String>> previousFrequentItemsets, int k){

        List<Set<String>> candidateItemsets = new ArrayList<>();

        List<Set<String>> previousFrequentItemsetList = new ArrayList<>(previousFrequentItemsets); //need to convert to a list as I will need to look for subsets in pruning


        // should have no duplicates
        for (int i = 0; i < previousFrequentItemsetList.size(); i++) {
            for (int j = i + 1; j < previousFrequentItemsetList.size(); j++) {
                Set<String> combinedItemset = previousFrequentItemsetList.get(i);
                combinedItemset.addAll(previousFrequentItemsetList.get(j));

                if (combinedItemset.size() == k) {
                    candidateItemsets.add(combinedItemset);
                }

            }
        }

        return candidateItemsets;
    }

    //prune all candidates that violate downward closure
    private Set<Set<String>> pruneItemsetsFromCandidateItemsets (List<Set<String>> candidateItemsetsList,
                                                                  Set<Set<String>> previousFrequentItemsets, int k){

        Set<Set<String>> approvedCandidates = new HashSet<>();
        Set<Set<String>> subSet = new HashSet<>();
        // having an arraylist means I can now loop over the list to study the subsets
        for (int i = 0; i < k; i++){
            subSet.add(candidateItemsetsList.get(i));
            if (previousFrequentItemsets.containsAll(subSet)){
                approvedCandidates.addAll(previousFrequentItemsets);
            }
        }
        return approvedCandidates;
    }

    private Set<Set<String>> getFinalCandidateItemsets (Set<Set<String>> approvedCandidateItemsets, double freqThreshold){
        Set<Set<String>> finalCandidateItemsets = new HashSet<>();
        int size = approvedCandidateItemsets.size();

        Map<Set<String>, Integer> items = new HashMap<>();

        for (Set<String> candidate: approvedCandidateItemsets) {
            items.merge(candidate, 1, Integer::sum); // IntelliJ suggestion ----------------   will come back for debugging if it does not work
        }

        for (Map.Entry<Set<String>, Integer> entry : items.entrySet()) {
            double entryFreq = (double) entry.getValue()/size;
            if (entryFreq >= freqThreshold) {
                finalCandidateItemsets.add(entry.getKey());
            }
        }

        return finalCandidateItemsets;
    }


}
