package symbols;

public class CommonSymbol 
{
    protected String _id;
    protected String _type;
    protected int _offset; // offset from s0

    public CommonSymbol()
    {
        // nothing assigned here, "for scopes"
    }

    public CommonSymbol(String id, String type)
    {
        this._id = id;
        this._type = type;
    }

    public String getId()
    {
        return this._id;
    }

    public String getType()
    {
        return this._type;
    }   
    
    public int getOffset()
    {
        return _offset;
    }
}



