package symbols;

public class Variable extends CommonSymbol
{
    public Variable(String name, String type, int offset)
    {
        super(name, type);
        this._offset = offset;
    }
}
