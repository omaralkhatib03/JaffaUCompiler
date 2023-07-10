package symbols;

public class CommonSymbol 
{
    protected String _id;
    protected String _type;
    protected int _offset; // offset from s0
    protected boolean _isGlobalScope;

    public CommonSymbol()
    {
        // nothing assigned here, "for scopes"
    }

    public CommonSymbol(String id, String type, boolean isGlobal)
    {
        this._id = id;
        this._type = type;
        this._isGlobalScope = isGlobal;
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

    public boolean isGlobal()
    {
        return this._isGlobalScope;
    }
}



