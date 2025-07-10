package logic;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Huffman {


    private Node root;
    private Map<Byte, Integer> charFrequencies;
    private Map<Byte, String> huffmanCodes = new HashMap<>();


    public int compress(List<File> inputFiles, File compressedFile, String password) throws IOException {
        fillCharFrequencies(inputFiles);
        if (charFrequencies.isEmpty()) {
            System.out.println("error: empty File");
            return 0;
        }

        root = buildTree();
        generateHuffmanCodes(root, "");

        writeFiles(inputFiles, compressedFile, password);
        return compressionRatio(inputFiles, compressedFile);
    }

    private int compressionRatio(List<File> inputFiles, File compressedFile) {
        long totalOriginalSize = 0;
        for (File f : inputFiles) {
            totalOriginalSize += f.length();
        }

        long compressedSize = compressedFile.length();

        double compressionRatio = (double) compressedSize / totalOriginalSize * 100;
        compressionRatio = Math.floor(compressionRatio);

        return (int) compressionRatio;
    }

    private void fillCharFrequencies(List<File> inputFiles) throws IOException {
        charFrequencies = new HashMap<>();

        for (File inputFile : inputFiles) {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile));
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = bis.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    byte b = buffer[i];
                    charFrequencies.put(b, charFrequencies.getOrDefault(b, 0) + 1);
                }
            }

            bis.close();
        }
    }


    private Node buildTree() {
        Queue<Node> queue = new PriorityQueue<>();
        charFrequencies.forEach((b, f) -> queue.add(new Leaf(b, f)));

        if (queue.size() == 1) {
            queue.add(new Leaf((byte) 0, 0));
        }
        while (queue.size() > 1) {
            queue.add(new Node(queue.poll(), queue.poll()));
        }
        return queue.poll();
    }

    private void generateHuffmanCodes(Node node, String code) {
        if (node instanceof Leaf) {
            if (code.isEmpty()) code = "0";
            huffmanCodes.put(((Leaf) node).getCharacter(), code);
            return;
        }

        generateHuffmanCodes(node.getLeft(), code + "0");
        generateHuffmanCodes(node.getRight(), code + "1");
    }

    private void writeFiles(List<File> inputFiles, File destination, String password) throws IOException {
        int nextStart = writeTree(inputFiles, destination, password);

        for (File inputFile : inputFiles) {
            nextStart = compressFile(inputFile, destination, nextStart);
        }
    }


    private int writeTree(List<File> inputFiles, File destination, String password) throws IOException {
        int passwordLength = 0;
        byte[] passwordBytes = new byte[0];
        if (password != null) {
            passwordLength = password.length();
            passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        }

        byte[] treeBytes = serializeTreeToBytes();
        int treeSize = treeBytes.length;

        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(destination)));

        dos.writeByte(passwordLength);                  // v=password length -- 1  Byte
        if (passwordBytes.length > 0)
            dos.write(passwordBytes);                   // password          -- v  Bytes
        dos.writeInt(treeSize);                         // x= tree length    -- 4  Bytes
        dos.write(treeBytes);                           // tree              -- x  Bytes

        dos.close();

        return 1 + passwordLength + 4 + treeSize;
    }

    private int compressFile(File original, File destination, int fileStart) throws IOException {

        String fileName = original.getName();
        byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        int fileNameLength = fileNameBytes.length;

        RandomAccessFile dos = new RandomAccessFile(destination, "rw");

        dos.seek(fileStart);

        dos.writeByte(fileNameLength);                  // y= name length    -- 1 Byte
        dos.write(fileNameBytes);                       // name              -- y Bytes
        dos.writeByte(0);                            // padBits           -- 1 Byte
        dos.writeInt(0);                             //z= fileData           -- 4 Bytes

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(original));
        int currentByte = 0;
        int bitCount = 0;
        byte[] buffer = new byte[8192];
        int bytesRead;

        int dataLength = 0;

        while ((bytesRead = bis.read(buffer)) != -1) {
            for (int i = 0; i < bytesRead; i++) {
                byte b = buffer[i];
                String code = huffmanCodes.get(b);
                for (char bit : code.toCharArray()) {
                    currentByte = (currentByte << 1) | (bit == '1' ? 1 : 0);
                    bitCount++;
                    if (bitCount == 8) {
                        dos.write(currentByte);//compressed data     -- z Bytes
                        currentByte = 0;
                        bitCount = 0;
                        dataLength++;
                    }
                }
            }
        }
        int padBits = 0;

        if (bitCount > 0) {
            padBits = 8 - bitCount;
            currentByte = currentByte << padBits;
            dos.write(currentByte);
            dataLength++;
        }

        bis.close();

        dos.seek(fileStart + 1 + fileNameLength);
        dos.writeByte(padBits);
        dos.writeInt(dataLength);
        dos.close();


        return fileStart + 1 + fileNameLength + 1 + 4 + dataLength;
    }


    private void decodeTree(File encodedFile, String password) throws IOException {
        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(encodedFile)));

        int passwordLength = dis.readByte();
        if (passwordLength != 0) {
            byte[] passwordBytes = new byte[passwordLength];
            dis.read(passwordBytes);
            if (!Objects.equals(password, new String(passwordBytes)))
                return;
        }
        int treeSize = dis.readInt();
        byte[] treeBytes = new byte[treeSize];
        dis.readFully(treeBytes);
        root = deserializeTreeFromBytes(treeBytes);

        dis.close();
    }

    public Map<String, Integer> getAllCompressedFiles(File encodedFile) throws IOException {
        Map<String, Integer> compressedFiles = new HashMap<>();
        RandomAccessFile raf = new RandomAccessFile(encodedFile, "rw");


        int fileStart = 0;
        fileStart += raf.readByte() + 1;
        raf.seek(fileStart);
        fileStart += raf.readInt() + 4;
        while (fileStart < encodedFile.length()) {
            raf.seek(fileStart);
            int nameLength = raf.readByte();
            byte[] nameBytes = new byte[nameLength];
            raf.read(nameBytes);
            compressedFiles.put(new String(nameBytes), fileStart);
            raf.readByte();
            int dataLength = raf.readInt();
            fileStart += 1 + nameLength + 1 + 4 + dataLength;
        }
        return compressedFiles;
    }


    public boolean decodeAll(File encodedFile, String decodedOutputPath, String password) throws IOException {

        Map<String, Integer> compressedFiles = getAllCompressedFiles(encodedFile);
        boolean result = true;
        for (Map.Entry<String, Integer> entry : compressedFiles.entrySet()) {
           result= decodeFile(encodedFile, entry.getValue(), decodedOutputPath, password);
        }

        return result;
    }

    public boolean decodeFile(File encodedFile, int fileStart, String decodedOutputPath, String password) throws IOException {

        if (root == null)
            decodeTree(encodedFile, password);
        if (root == null) {
            System.out.println("Password is wrong");
            return false;
        }

        Thread thread = new Thread(() -> {
           String fileName;

            try {
               fileName= decodeFile(encodedFile, fileStart, decodedOutputPath, this.root);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            JOptionPane.showMessageDialog(null, fileName+" decoded successfully!");

        });
        thread.start();
        return true;
    }

    private String decodeFile(File encodedFile, int fileStart, String decodedOutputPath, Node root) throws IOException {

        RandomAccessFile dis = new RandomAccessFile(encodedFile, "rw");
        dis.seek(fileStart);


        int fileNameLength = dis.readByte();
        byte[] fileNameBytes = new byte[fileNameLength];
        dis.readFully(fileNameBytes);
        String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);

        int padBits = dis.readByte();
        int dataLength = dis.readInt();


        File decodedFile = new File(decodedOutputPath + fileName);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(decodedFile));

        Node current = root;
        int nextByte;
        long totalBits = dataLength * 8L - padBits;
        long bitsRead = 0;

        for (int k = 0; k < dataLength; k++) {
            nextByte = dis.read();

            for (int i = 7; i >= 0; i--) {
                if (bitsRead >= totalBits) break;

                int bit = (nextByte >> i) & 1;
                current = (bit == 0) ? current.getLeft() : current.getRight();
                bitsRead++;

                if (current instanceof Leaf) {
                    byte b = ((Leaf) current).getCharacter();
                    bos.write(b);
                    current = root;
                }
            }
        }

        boolean last = (dis.read() == -1);

        dis.close();
        bos.close();

        return fileName;
    }

    private Node deserializeTreeFromBytes(byte[] treeBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(treeBytes);
        DataInputStream dis = new DataInputStream(bais);
        return readTree(dis);
    }

    private Node readTree(DataInputStream dis) throws IOException {
        boolean isLeaf = dis.readBoolean();
        if (isLeaf) {
            byte b = dis.readByte();
            return new Leaf(b, 0);
        } else {
            Node left = readTree(dis);
            Node right = readTree(dis);
            return new Node(left, right);
        }
    }

    private byte[] serializeTreeToBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        encodeTree(root, dos);
        dos.flush();
        return baos.toByteArray();
    }

    private void encodeTree(Node node, DataOutputStream dos) throws IOException {
        if (node instanceof Leaf) {
            dos.writeBoolean(true);
            dos.writeByte(((Leaf) node).getCharacter());
        } else {
            dos.writeBoolean(false);
            encodeTree(node.getLeft(), dos);
            encodeTree(node.getRight(), dos);
        }
    }


}