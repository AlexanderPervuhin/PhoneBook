package phonebook;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        long linearSearchTime;
        String phoneBookPath = "C:/directory.txt";
        String findPeoplePath = "C:/find.txt";
        StopWatch.reset();
        long startTime = StopWatch.getTimeOfBegging();
        PhoneBook phoneBook = new PhoneBook();
        phoneBook.readBookFrom(phoneBookPath);
        phoneBook.readSearchFile(findPeoplePath);
        System.out.println("\nStart searching (linear search)...");
        phoneBook.findNumbersLinear();
        StopWatch.setTimeOfBegging(startTime);
        StopWatch.stop();
        linearSearchTime = StopWatch.getTimeElapsedMills();
        SearchResults.setSearchTime(linearSearchTime);
        SearchResults.print();
        System.out.println(" \nStart searching (bubble sort + jump search)...");
        phoneBook.searchJumpingBubble(linearSearchTime);
        SearchResults.print();
        System.out.println("\n Start searching (quick sort + binary search)...");
        phoneBook.QuickSort(0, phoneBook.getLength() - 1);
        phoneBook.findPeopleBinary();
        SearchResults.print();
        System.out.println("\n Start searching (hash table)...");
        HashTable<String> phoneHashTable = phoneBook.bookToHashTable();
        phoneBook.searchHash(phoneHashTable);
        SearchResults.print();

    }

    public static int hashCode(String string) {
        int hash = 0;
        for (int i = 0; i < string.length(); i++) {
            hash = hash * 31 + string.charAt(i);
        }
        return hash;
    }

}

class TableEntry<T> {
    private final int key;
    private final T value;
    private boolean removed;

    TableEntry(int key, T value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }

    public void remove() {
        removed = true;
    }

    public boolean isRemoved() {
        return removed;
    }
}

class HashTable<T> {
    private int size;
    private TableEntry[] table;
    private int fullness = 0;

    public HashTable(int size) {
        this.size = size;
        table = new TableEntry[size];
    }

    public boolean put(int key, T value) {
        if (fullness == size) {
            rehash();
        }
        int idx = findKey(key);
        if (idx == -1) {
            System.out.println("-1");
            return false;
        }
        table[idx] = new TableEntry<>(key, value);
        fullness++;
        return true;
    }

    public T get(int key) {
        int idx = findKey(key);
        if (idx == -1 || table[idx] == null) {
            return null;
        }
        return (T) table[idx].getValue();
    }

    public void remove(int key) {
        int idx = findKey(key);
        if (!(idx == -1 || table[idx] == null)) {
            table[idx].remove();
        }
    }

    private int findKey(int key) {
        int hash = key % size;
        while ((!(table[hash] == null || table[hash].getKey() == key))) {
            hash = (hash + 1) % size;
            if (hash == key % size) {
                return -1;
            }
        }
        return hash;
    }

    private void rehash() {
        TableEntry[] temp = new TableEntry[size];
        fullness = 0;
        int oldSize = size;
        for (int i = 0; i < oldSize; i++) {
            temp[i] = table[i];
        }
        size = size * 2;
        table = new TableEntry[size];
        for (int i = 0; i < oldSize; i++) {
            if (temp[i] != null) {
                this.put(temp[i].getKey(), (T) temp[i].getValue());
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder tableStringBuilder = new StringBuilder();

        for (int i = 0; i < table.length; i++) {
            if (table[i] == null) {
                tableStringBuilder.append(i).append(": null");
            } else {
                tableStringBuilder.append(i + ": key=" + table[i].getKey()
                        + ", value=" + table[i].getValue()
                        + ", removed=" + table[i].isRemoved());
            }
            if (i < table.length - 1) {
                tableStringBuilder.append("\n");
            }
        }
        return tableStringBuilder.toString();
    }

}


class PhoneBook {
    private ArrayList<String> people = new ArrayList<>();
    private ArrayList<Integer> phoneNumbers = new ArrayList<>();
    private ArrayList<String> peopleToSearch = new ArrayList<>();


    int getLength() {
        return people.size();
    }

    void readBookFrom(String filePath) {
        File phoneBookFile = new File(filePath);
        String regex = "^\\s+";
        try {
            Scanner scanner = new Scanner(phoneBookFile);

            while (scanner.hasNext()) {
                phoneNumbers.add(scanner.nextInt());
                people.add(scanner.nextLine().replaceAll(regex, ""));
            }

        } catch (FileNotFoundException e) {
            System.out.println(" No file found: " + filePath);
        }
    }


    private static int hashCode(String string) {
        int length;
        if (string.length() > 5)
            length = 5;
        else length = string.length();
        int hash = 7;
        for (int i = 0; i < length; i++) {
            hash = hash * 31 + string.charAt(i);
        }
        return hash;
    }


    HashTable<String> bookToHashTable() {
        SearchResults.reset();
        StopWatch.reset();
        HashTable<String> hashTable;
        hashTable = new HashTable<>(people.size() * 10);
        for (int i = 0; i < people.size(); i++) {
            int key = hashCode(people.get(i));
            String value = people.get(i) + phoneNumbers.get(i);
            hashTable.put(key, value);
        }
        StopWatch.stop();
        SearchResults.setCreatingTime(StopWatch.getTimeElapsedMills());
        return hashTable;
    }

    void searchHash(HashTable hashTable) {
        StopWatch.reset();
        int quantityFound = 0;
        SearchResults.setPeopleToSearch(peopleToSearch.size());
        for (int i = 0; i < peopleToSearch.size(); i++) {
            int key = hashCode(peopleToSearch.get(i));
            if (hashTable.get(key) != null)
                quantityFound++;
        }
        StopWatch.stop();
        SearchResults.setPeopleFound(quantityFound);
        SearchResults.setSearchTime(StopWatch.getTimeElapsedMills());
    }

    void readSearchFile(String filePath) {
        peopleToSearch.clear();
        File peopleToSearchFile = new File(filePath);

        try {
            Scanner scanner = new Scanner(peopleToSearchFile);

            while (scanner.hasNext()) {
                peopleToSearch.add(scanner.nextLine());
            }

        } catch (FileNotFoundException e) {
            System.out.println(" No file found: " + filePath);
        }

    }

    long findNumbersLinear() {
        StopWatch.reset();
        SearchResults.setPeopleToSearch(peopleToSearch.size());
        int quantityFound = 0;

        for (String toSearch : peopleToSearch) {
            for (String person : people) {
                if (person.equals(toSearch)) {
                    quantityFound += 1;
                    break;
                }
            }
        }
        StopWatch.stop();
        SearchResults.setPeopleFound(quantityFound);
        return StopWatch.getTimeElapsedMills();
    }

    void searchJumpingBubble(long linearSearchTime) {
        SearchResults.reset();
        StopWatch.reset();
        long bubbleSortTime = 0;
        long searchingTime = 0;
        boolean sortrtingIsStoped = false;

        outer:
        for (int i = 0; i < people.size() - 1; i += 1) {
            for (int j = 0; j < people.size() - 1; j += 1) {
                if (people.get(j).compareTo(people.get((j + 1))) < 0) {

                    swapBookEntries(j, j + 1);
                    StopWatch.stop();
                    bubbleSortTime = StopWatch.getTimeElapsedMills();
                    if (bubbleSortTime >= (linearSearchTime * 10)) {
                        searchingTime = (findNumbersLinear());
                        sortrtingIsStoped = true;
                        break outer;
                    }
                }
            }
        }
        if (!sortrtingIsStoped) {
            searchingTime = (findNumbersJump());
            SearchResults.setSortingStopMsg("");
        } else {
            SearchResults.setSortingStopMsg(" - STOPPED, moved to linear search");
        }

        SearchResults.setSearchTime(searchingTime);
        SearchResults.setSortingTime(bubbleSortTime);

    }

    private long findNumbersJump() {
        StopWatch.reset();
        SearchResults.setPeopleToSearch(peopleToSearch.size());
        int quantityFound = 0;
        for (String toSearch : peopleToSearch) {
            if (jumpSearch(toSearch) >= 0) {
                quantityFound += 1;
            }
        }
        StopWatch.stop();
        SearchResults.setPeopleFound(quantityFound);
        return StopWatch.getTimeElapsedMills();
    }


    private int jumpSearch(String target) {
        int currentRight = 0; // right border of the current block
        int prevRight = 0; // right border of the previous block

        if (people.size() == 0) {
            System.out.println(-1);
        }


        /* Calculating the jump length over array elements */
        int jumpLength = (int) Math.sqrt(people.size());

        /* Finding a block where the element may be present */
        while (currentRight < people.size() - 1) {

            /* Calculating the right border of the following block */
            currentRight = Math.min(people.size() - 1, currentRight + jumpLength);

            if (people.get(currentRight).compareTo(target) > 0) {
                break; // Found a block that may contain the target element
            }
            prevRight = currentRight; // update the previous right block border
        }

        /* If the last block is reached and it cannot contain the target value => not found */
        if ((currentRight == (people.size() - 1)) && (people.get(currentRight).compareTo(target)) < 0) {
            System.out.println(-1);
        }
        return backwardSearch(target, prevRight, currentRight);
    }

    private int backwardSearch(String target, int leftExcl, int rightIncl) {
        for (int i = rightIncl; i > leftExcl; i--) {
            if (people.get(i).equals(target)) {
                return i;
            }
        }
        return -1;
    }


    void QuickSort(int left, int right) {
        StopWatch.reset();
        SearchResults.reset();
        QuickSortRecursion(left, right);
        StopWatch.stop();
        SearchResults.setSortingTime(StopWatch.getTimeElapsedMills());
    }

    private void QuickSortRecursion(int left, int right) {

        if (left < right) {
            int pivotIndex = partition(left, right); // the pivot is already on its place
            QuickSortRecursion(left, pivotIndex - 1);  // sort the left subarray
            QuickSortRecursion(pivotIndex + 1, right); // sort the right subarray
        }
    }

    private int partition(int left, int right) {
        String pivot = people.get(right);  // choose the rightmost element as the pivot
        int partitionIndex = left; // the first element greater than the pivot

        /* move large values into the right side of the array */
        for (int i = left; i < right; i++) {
            if (people.get(i).compareTo(pivot) <= 0) { // may be used '<' as well
                swapBookEntries(i, partitionIndex);
                partitionIndex++;
            }
        }

        swapBookEntries(partitionIndex, right); // put the pivot on a suitable position

        return partitionIndex;
    }

    void findPeopleBinary() {
        StopWatch.reset();
        SearchResults.setPeopleToSearch(peopleToSearch.size());
        int peopleFound = 0;
        for (String toSearch : peopleToSearch) {
            if (binarySearch(toSearch, 0, people.size() - 1) >= 0) {
                peopleFound += 1;
            }
        }
        StopWatch.stop();
        SearchResults.setPeopleFound(peopleFound);
        SearchResults.setSearchTime(StopWatch.getTimeElapsedMills());

    }

    private int binarySearch(String elem, int left, int right) {
        while (left <= right) {
            int mid = left + (right - left) / 2; // the index of the middle element

            if (elem.equals(people.get(mid))) {
                return mid; // the element is found, return its index
            } else if (elem.compareTo(people.get(mid)) < 0) {
                right = mid - 1; // go to the left subarray
            } else {
                left = mid + 1;  // go the the right subarray
            }
        }
        return -1; // the element is not found
    }


    private void swapBookEntries(int i, int j) {
        Collections.swap(people, j, i);
        Collections.swap(phoneNumbers, j, i);
    }

}


class SearchResults {
    private static long searchTime = 0;
    private static long sortingTime = 0;
    private static String sortingStopMsg = "";
    private static int peopleFound = 0;
    private static int peopleToSearch = 0;

    static void setCreatingTime(long creatingTime) {
        SearchResults.creatingTime = creatingTime;
    }

    private static long creatingTime = 0;

    static void setSearchTime(long searchTime) {

        SearchResults.searchTime = searchTime;
    }

    static void setSortingTime(long sortingTime) {

        SearchResults.sortingTime = sortingTime;
    }

    static void setSortingStopMsg(String sortingStopMsg) {
        SearchResults.sortingStopMsg = sortingStopMsg;
    }

    static void setPeopleFound(int peopleFound) {
        SearchResults.peopleFound = peopleFound;
    }

    static void setPeopleToSearch(int peopleToSearch) {
        SearchResults.peopleToSearch = peopleToSearch;
    }

    static void reset() {
        searchTime = 0;
        sortingTime = 0;
        sortingStopMsg = "";
        peopleFound = 0;
        peopleToSearch = 0;
        creatingTime = 0;
    }


    static void print() {
        System.out.format("Found %d/%d entries.", peopleFound, peopleToSearch);
        if (sortingTime == 0 && creatingTime == 0) {
            System.out.format("Time taken %s \n", timeToString(searchTime));
        } else {
            System.out.format("Time taken: %s\n", timeToString(searchTime + sortingTime + creatingTime));
            if (creatingTime == 0) {
                System.out.format("Sorting time: %s" + " %s\n", timeToString(sortingTime), sortingStopMsg);
            } else {
                System.out.format("Creating time: %s\n", timeToString(creatingTime));
            }
            System.out.format("Searching time: %s \n", timeToString(searchTime));

        }
    }

    private static String timeToString(long time) {
        return time / (60000) + " min. " + (time % 60000) / 1000 + " sec. " + (time) % 1000 + " ms.";

    }


}


class StopWatch {
    static long getTimeOfBegging() {
        return timeOfBegging;
    }

    private static long timeOfBegging;

    private static long timeElapsedMills;


    static void setTimeOfBegging(long time) {
        timeOfBegging = time;
    }

    static void stop() {
        timeElapsedMills = System.currentTimeMillis() - timeOfBegging;
    }

    static void reset() {
        timeOfBegging = System.currentTimeMillis();
    }

    static long getTimeElapsedMills() {
        return timeElapsedMills;
    }


}














