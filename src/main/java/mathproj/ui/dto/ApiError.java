package mathproj.ui.dto;

public class ApiError {
    public String error;
    public String message;

    public ApiError(String error, String message) {
        this.error = error;
        this.message = message;
    }
}
