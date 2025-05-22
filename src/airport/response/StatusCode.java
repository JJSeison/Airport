package airport.response;

/**
 * Códigos de estado inspirados en HTTP para nuestras respuestas
 */
public enum StatusCode {
    OK(200),
    CREATED(201),
    BAD_REQUEST(400),
    NOT_FOUND(404),
    CONFLICT(409),
    INTERNAL_ERROR(500);

    private final int code;

    StatusCode(int code) {
        this.code = code;
    }

    /** Devuelve el código numérico */
    public int getCode() {
        return code;
    }
}
