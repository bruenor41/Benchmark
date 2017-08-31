
package magiclib.mouse;

public enum MouseButton
{
    disabled(-2), none(-1), left(0), right(1), middle(2), left_plus_right(3);

    private int value;

    MouseButton(int value)
    {
        this.value = value;
    }

    public int get()
    {
        return value;
    }
}