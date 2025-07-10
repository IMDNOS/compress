package logic;

public class Leaf extends Node {
    private final Byte character;

    public Leaf(Byte character,int frequency) {
        super(frequency);
        this.character = character;
    }

    public Byte getCharacter() {
        return character;
    }
}
