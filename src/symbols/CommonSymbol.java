package symbols;

import java.util.HashMap;
import java.util.Map;


public class CommonSymbol 
{
    protected String _id;
    protected String _type;

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
}



