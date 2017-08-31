
package magiclib.mouse;

public enum MouseAction
{
    none(-1), down(0), up(1), move(2);

    private int value;

    MouseAction(int value)
    {
        this.value = value;
    }

    public int get()
    {
        return value;
    }
}