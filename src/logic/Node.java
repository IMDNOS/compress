package logic;

public class Node implements Comparable<Node> {

    private final int frequency;
    private Node left;
    private Node right;

    public Node(Node left, Node right) {
        this.frequency = left.getFrequency() + right.getFrequency();
        this.left = left;
        this.right = right;
    }

    public Node(int frequency) {
        this.frequency = frequency;
    }

    public int getFrequency() {
        return frequency;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    @Override
    public int compareTo(Node node) {
        return Integer.compare(frequency, node.getFrequency());
    }

//    @Override
//    public String toString() {
//        String s ="";
//        if (this instanceof Leaf) {
//            s+=1+((Leaf) this).getCharacter();
//        }else {
//            s+=0+"l"+left.toString()+"r"+right.toString();
//        }
//        return s;
//    }
}
