package airport.response;

/**
 * Envuelve el resultado de una operación, con un StatusCode, mensaje y opcionalmente un payload (data).
 * @param <T> Tipo de dato que devolvemos en caso de éxito.
 */
public class Response<T> {
    private final StatusCode status;
    private final String message;
    private final T data;

    private Response(StatusCode status, String message, T data) {
        this.status  = status;
        this.message = message;
        this.data    = data;
    }

    /** Factory para respuesta exitosa con datos */
    public static <T> Response<T> of(StatusCode status, String message, T data) {
        return new Response<>(status, message, data);
    }

    /** Factory para respuesta de error o sin datos */
    public static <T> Response<T> of(StatusCode status, String message) {
        return new Response<>(status, message, null);
    }

    public StatusCode getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    /** Conveniencia: ¿fue exitosa (200–299)? */
    public boolean isSuccess() {
        int code = status.getCode();
        return code >= 200 && code < 300;
    }
}
