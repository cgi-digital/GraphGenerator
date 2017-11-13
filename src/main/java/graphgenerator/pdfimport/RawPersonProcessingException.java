package graphgenerator.pdfimport;

public class RawPersonProcessingException extends Exception {
    public RawPersonProcessingException(String m) {
        super(m);
    }

    public RawPersonProcessingException(Throwable t) {
        super(t);
    }
    
    public RawPersonProcessingException(String m, Throwable t) {
        super(m, t);
    }
}