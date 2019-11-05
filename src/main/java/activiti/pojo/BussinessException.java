package activiti.pojo;

public class BussinessException extends RuntimeException{

    public BussinessException(String msg){
        super(msg);
    }

    public BussinessException(Throwable throwable) {
        super(throwable);
    }

    public BussinessException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
