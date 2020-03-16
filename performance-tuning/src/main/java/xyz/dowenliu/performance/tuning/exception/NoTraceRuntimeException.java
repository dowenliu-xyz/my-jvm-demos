package xyz.dowenliu.performance.tuning.exception;

/**
 * 不带 StackTrace 的运行时异常
 * <p>create at 2020/3/16</p>
 *
 * @author liufl
 * @since 1.0
 */
public class NoTraceRuntimeException extends RuntimeException {
    private static final long serialVersionUID = -7714529443031399512L;

    public NoTraceRuntimeException() {
        super(null, null, true, false);
    }

    public NoTraceRuntimeException(String message) {
        super(message, null, true, false);
    }

    public NoTraceRuntimeException(String message, Throwable cause) {
        super(message, cause, true, false);
    }

    public NoTraceRuntimeException(Throwable cause) {
        super((cause==null ? null : cause.toString()), cause, true, false);
    }
}
