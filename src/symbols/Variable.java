package symbols;

public class Variable extends CommonSymbol
{
    public Variable(String name, String type, int offset, boolean isGlobal)
    {
        super(name, type, isGlobal);
        this._offset = offset;
    }

}
