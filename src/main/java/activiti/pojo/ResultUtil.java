package activiti.pojo;

public class ResultUtil {

    //当正确时返回的值
    public static Result success(Object data, String message) {
        Result result = new Result();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static Result success(String message) {
        Result result = new Result();
        result.setCode(200);
        result.setMessage(message);
        return result;
    }

    //当错误时返回的值
    public static Result error(int code, String message) {
        Result result = new Result();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
