import java.io.*;
import java.util.*;

class HuffmanNode implements Comparable<HuffmanNode> {
    int frequency;
    char data;
    HuffmanNode left, right;

    public HuffmanNode(char data, int frequency) {
        this.data = data;
        this.frequency = frequency;
        this.left = this.right = null;
    }

    public HuffmanNode(char data, int frequency, HuffmanNode left, HuffmanNode right) {
        this.data = data;
        this.frequency = frequency;
        this.left = left;
        this.right = right;
    }

    @Override
    public int compareTo(HuffmanNode node) {
        return this.frequency - node.frequency;
    }
}

public class HuffmanCoding {
    private static Map<Character, String> huffmanCodes = new HashMap<>();
    private static HuffmanNode root;

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java HuffmanCoding -e|-d inputfile outputfile");
            return;
        }

        String mode = args[0];
        String inputFile = args[1];
        String outputFile = args[2];

        try {
            if (mode.equals("-e")) {
                String data = readFile(inputFile);
                if (data.isEmpty()) {
                    System.out.println("Input file is empty.");
                    return;
                }
                System.out.println("Data to encode: " + data);
                String encodedData = encode(data);
                System.out.println("Encoded data: " + encodedData);
                writeFile(outputFile, encodedData);

                // Сохраняем таблицу кодов в файл
                saveCodesToFile("huffman_codes.txt");
                System.out.println("File encoded successfully.");
            } else if (mode.equals("-d")) {
                // Загружаем таблицу кодов из файла
                try {
                    loadCodesFromFile("huffman_codes.txt");
                } catch (ClassNotFoundException e) {
                    System.out.println("Error: Huffman codes file is corrupted or invalid.");
                    return;
                }

                String encodedData = readFile(inputFile);
                if (encodedData.isEmpty()) {
                    System.out.println("Input file is empty.");
                    return;
                }
                System.out.println("Data to decode: " + encodedData);
                String decodedData = decode(encodedData);
                System.out.println("Decoded data: " + decodedData);
                writeFile(outputFile, decodedData);
                System.out.println("File decoded successfully.");
            } else {
                System.out.println("Invalid mode. Use -e for encode or -d for decode.");
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static String readFile(String fileName) throws IOException {
        StringBuilder data = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                data.append(line);
            }
        }
        return data.toString();
    }

    private static void writeFile(String fileName, String data) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(data);
            writer.flush(); // Обеспечиваем запись данных в файл
        }
    }

    private static String encode(String data) {
        if (data == null || data.isEmpty()) {
            return "";
        }

        Map<Character, Integer> frequencyMap = buildFrequencyMap(data);
        root = buildHuffmanTree(frequencyMap);
        generateCodes(root, "");

        // Если все символы одинаковые, присваиваем код "0"
        if (huffmanCodes.size() == 1) {
            char singleChar = data.charAt(0);
            huffmanCodes.put(singleChar, "0");
        }

        StringBuilder encodedData = new StringBuilder();
        for (char c : data.toCharArray()) {
            encodedData.append(huffmanCodes.get(c));
        }
        return encodedData.toString();
    }

    private static String decode(String encodedData) {
        if (encodedData == null || encodedData.isEmpty()) {
            return "";
        }

        // Если дерево состоит из одного узла, декодируем все символы как root.data
        if (root.left == null && root.right == null) {
            char singleChar = root.data;
            return String.valueOf(singleChar).repeat(encodedData.length());
        }

        StringBuilder decodedData = new StringBuilder();
        HuffmanNode current = root;
        for (char bit : encodedData.toCharArray()) {
            current = (bit == '0') ? current.left : current.right;
            if (current.left == null && current.right == null) {
                decodedData.append(current.data);
                current = root;
            }
        }
        return decodedData.toString();
    }

    private static Map<Character, Integer> buildFrequencyMap(String data) {
        Map<Character, Integer> frequencyMap = new HashMap<>();
        for (char c : data.toCharArray()) {
            frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
        }
        return frequencyMap;
    }

    private static HuffmanNode buildHuffmanTree(Map<Character, Integer> frequencyMap) {
        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();
        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            pq.add(new HuffmanNode(entry.getKey(), entry.getValue()));
        }

        while (pq.size() > 1) {
            HuffmanNode left = pq.poll();
            HuffmanNode right = pq.poll();
            HuffmanNode parent = new HuffmanNode('\0', left.frequency + right.frequency, left, right);
            pq.add(parent);
        }
        return pq.poll();
    }

    private static void generateCodes(HuffmanNode node, String code) {
        if (node == null) {
            return;
        }

        if (node.left == null && node.right == null) {
            huffmanCodes.put(node.data, code);
        }

        generateCodes(node.left, code + "0");
        generateCodes(node.right, code + "1");
    }

    // Сохраняем таблицу кодов в файл
    private static void saveCodesToFile(String fileName) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(huffmanCodes);
        }
    }

    // Загружаем таблицу кодов из файла
    @SuppressWarnings("unchecked")
    private static void loadCodesFromFile(String fileName) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            huffmanCodes = (Map<Character, String>) ois.readObject();
            // Восстанавливаем дерево Хаффмана из таблицы кодов
            root = rebuildHuffmanTree(huffmanCodes);
        }
    }

    // Восстанавливаем дерево Хаффмана из таблицы кодов
    private static HuffmanNode rebuildHuffmanTree(Map<Character, String> codes) {
        HuffmanNode root = new HuffmanNode('\0', 0);
        for (Map.Entry<Character, String> entry : codes.entrySet()) {
            HuffmanNode current = root;
            String code = entry.getValue();
            for (char bit : code.toCharArray()) {
                if (bit == '0') {
                    if (current.left == null) {
                        current.left = new HuffmanNode('\0', 0);
                    }
                    current = current.left;
                } else {
                    if (current.right == null) {
                        current.right = new HuffmanNode('\0', 0);
                    }
                    current = current.right;
                }
            }
            current.data = entry.getKey();
        }
        return root;
    }
}