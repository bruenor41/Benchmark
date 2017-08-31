
package magiclib.IO;

public class UserStorage
{
    public String title;
    public String path;

    public UserStorage(){
        super();
    }

    public UserStorage(String title, String path)
    {
        this.title = title;
        this.path = path;
    }

    public void setStorage(String title, String path)
    {
        this.title = title;
        this.path = path;
    }

    public static UserStorage create(UserStorage storage, String title, String path)
    {
        if (title == null || title.trim().equals("") || path == null || path.trim().equals(""))
        {
            return null;
        }
        else
        {
            if (storage == null)
                return new UserStorage(title, path);

            storage.title = title;
            storage.path = path;

            return storage;
        }
    }
}