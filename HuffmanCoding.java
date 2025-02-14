import java.io.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;

class HuffmanNode implements Comparable<HuffmanNode> {
    char character;
    int frequency;
    HuffmanNode left, right;

    public HuffmanNode(char character, int frequency) {
        this.character = character;
        this.frequency = frequency;
    }

    @Override
    public int compareTo(HuffmanNode node) {
        return Integer.compare(this.frequency, node.frequency);
    }
}

public class HuffmanCoding {
    private static Map<Character, String> huffmanCodes = new HashMap<>();
    private static Map<String, Character> reverseHuffmanCodes = new HashMap<>();

    public static HuffmanNode buildTree(Map<Character, Integer> frequencyMap) {
        PriorityQueue<HuffmanNode> queue = new PriorityQueue<>();
        
        for (var entry : frequencyMap.entrySet()) {
            queue.add(new HuffmanNode(entry.getKey(), entry.getValue()));
        }
        
        while (queue.size() > 1) {
            HuffmanNode left = queue.poll();
            HuffmanNode right = queue.poll();
            HuffmanNode parent = new HuffmanNode('\0', left.frequency + right.frequency);
            parent.left = left;
            parent.right = right;
            queue.add(parent);
        }
        return queue.poll();
    }

    public static void generateCodes(HuffmanNode root, String code) {
        if (root == null) return;
        if (root.left == null && root.right == null) {
            huffmanCodes.put(root.character, code);
            reverseHuffmanCodes.put(code, root.character);
        }
        generateCodes(root.left, code + "0");
        generateCodes(root.right, code + "1");
    }

    public static String encode(String text) {
        StringBuilder encodedText = new StringBuilder();
        for (char ch : text.toCharArray()) {
            encodedText.append(huffmanCodes.get(ch));
        }
        return encodedText.toString();
    }

    public static String decode(String encodedText) {
        StringBuilder decodedText = new StringBuilder();
        String temp = "";
        for (char ch : encodedText.toCharArray()) {
            temp += ch;
            if (reverseHuffmanCodes.containsKey(temp)) {
                decodedText.append(reverseHuffmanCodes.get(temp));
                temp = "";
            }
        }
        return decodedText.toString();
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: java HuffmanCoding <encode|decode> <input_file> <output_file>");
            return;
        }
        
        String mode = args[0];
        File inputFile = new File(args[1]);
        File outputFile = new File(args[2]);
        
        if (mode.equals("encode")) {
            String text = new String(Files.readAllBytes(inputFile.toPath()));
            Map<Character, Integer> frequencyMap = new HashMap<>();
            for (char ch : text.toCharArray()) {
                frequencyMap.put(ch, frequencyMap.getOrDefault(ch, 0) + 1);
            }
            
            HuffmanNode root = buildTree(frequencyMap);
            generateCodes(root, "");
            String encodedText = encode(text);
            
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile))) {
                oos.writeObject(huffmanCodes);
                oos.writeObject(encodedText);
            }
        } else if (mode.equals("decode")) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(inputFile))) {
                huffmanCodes = (Map<Character, String>) ois.readObject();
                reverseHuffmanCodes.clear();
                for (var entry : huffmanCodes.entrySet()) {
                    reverseHuffmanCodes.put(entry.getValue(), entry.getKey());
                }
                String encodedText = (String) ois.readObject();
                
                String decodedText = decode(encodedText);
                Files.write(outputFile.toPath(), decodedText.getBytes());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid mode! Use encode or decode.");
        }
    }
}
