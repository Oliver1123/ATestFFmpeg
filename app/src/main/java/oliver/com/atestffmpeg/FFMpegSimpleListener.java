package oliver.com.atestffmpeg;

/**
 * Created by oliver on 09/03/16.
 */
public interface FFMpegSimpleListener {
    void onSuccess(String message);
    void onFail(String message);
}
