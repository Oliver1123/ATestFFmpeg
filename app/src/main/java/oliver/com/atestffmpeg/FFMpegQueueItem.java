package oliver.com.atestffmpeg;

import android.content.Context;

/**
 * Created by oliver on 11.03.16.
 */
public class FFMpegQueueItem {
    private Context mContext;
    private String mTag;
    private String[] mCommandArray;
    private FFMpegSimpleListener mListener;

    public FFMpegQueueItem(Context context, String tag, String[] commandArray, FFMpegSimpleListener listener) {
        mContext = context;
        mTag = tag;
        mCommandArray = commandArray;
        mListener = listener;
    }

    public String[] getCommand() {
        return mCommandArray;
    }
    public Context getContext() {
        return mContext;
    }

    public String getTag() {
        return mTag;
    }

    public FFMpegSimpleListener getListener() {
        return mListener;
    }
}
