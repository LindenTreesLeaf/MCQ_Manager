package projet_info0502.Exceptions;

public class MCQManagerException extends RuntimeException{
    private int code = 0;

    public MCQManagerException(String m){ super(m); }
    public MCQManagerException(String m, int c){
        super(m);
        this.code = c;
    }

    public int getCode(){ return this.code; }
}
