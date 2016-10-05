package oliver.com.atestffmpeg;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by oliver on 08/03/16.
 */
public class FFMpegUtils {

    private static final String TAG = "FFMpegUtils";
    private static final String INTERMEDIATE_FILE_PREFIX = "intermediate";
    private static final ArrayList<FFMpegQueueItem> mQueue = new ArrayList<>();
    public static final String FRAMES_PREFIX = "image_";


    public static void ffmpegSimpleCommand(Context context, String tag, String[] cmd, final FFMpegSimpleListener listener) {
        if (TextUtils.isEmpty(tag)) tag = TAG;
        Log.d(TAG, "ffmpegSimpleCommand cmd " + Arrays.toString(cmd));
        mQueue.add(new FFMpegQueueItem(context, tag, cmd, listener));
        startNextCommand(context);
    }

    private static void startFFMpegCommand(final Context context, String tag, String[] cmd, final FFMpegSimpleListener listener) {
//        Log.d(TAG, "start comand " + cmd);
        FFmpeg ffmpeg = FFmpeg.getInstance(context);
        try {
            Log.d(tag, " cmd: " + Arrays.toString(cmd));
            final String finalTag = tag;
            ffmpeg.execute(cmd, new FFmpegExecuteResponseHandler() {

                @Override
                public void onStart() {
                    Log.d(finalTag, " FFmpeg on start");
                }

                @Override
                public void onProgress(String message) {
//                    Log.d(finalTag, " FFmpeg on progress " + message);
                }

                @Override
                public void onFailure(String message) {
                    Log.d(finalTag, " FFmpeg on failure " + message);
                    startNextCommand(context);
                    if (listener != null)
                        listener.onFail(message);
                }

                @Override
                public void onSuccess(String message) {
                    Log.d(finalTag, " FFmpeg on success " + message);
                    startNextCommand(context);
                    if (listener != null)
                        listener.onSuccess(message);
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "FFmpeg on finish ");
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            Log.d(tag, "Handle if FFmpeg is already running");
            if (listener != null)
                listener.onFail("Exception " + e.getMessage());
        }
    }

    private static void startNextCommand(Context context){
        Log.d(TAG, "===startNextCommand " + mQueue.size());
        if (!FFmpeg.getInstance(context).isFFmpegCommandRunning()) {
            if (mQueue.size() > 0) {
                FFMpegQueueItem item = mQueue.remove(0);
                startFFMpegCommand(item.getContext(), item.getTag(), item.getCommand(), item.getListener());
            }
        }
    }

    public static void concatTranscodedFiles(final Context context, List<String> files, final String outputFile, final FFMpegSimpleListener listener) {
        String[] cmd = generateConcatCommand(files, outputFile);
        ffmpegSimpleCommand(context, "ConcatTranscoded", cmd, listener);
    }

    public static void getFrameImage(final Context context, String inputFile, int frameNum, String outputFile, final FFMpegSimpleListener listener) {
        String[] cmd = generateGetFrameByNumCommand(frameNum, inputFile, outputFile);
        ffmpegSimpleCommand(context, "GetFrame", cmd, listener);
    }

    public static void transCodeFile(Context context, String file, String outputFile, final FFMpegSimpleListener listener) {
        String[] cmd = generateTranscodeCommand(file, outputFile);
        ffmpegSimpleCommand(context, "Transcode", cmd, listener);
    }

    public static void addMoovAtomToVideoFile(Context context, String inputVideoFile, String outputVideoFile, final FFMpegSimpleListener listener) {
        String[] cmd = generateAddMoovAtomToBeginCommand(inputVideoFile, outputVideoFile);
        ffmpegSimpleCommand(context, "AddMoovAtom", cmd, listener);
    }


    public static void trimAudio(Context context, String inputFile, String startTime, String durationTime, String outputFile, final FFMpegSimpleListener listener) {
        String[] cmd = generateAudioTrimCommand(inputFile, startTime, durationTime, outputFile);
        ffmpegSimpleCommand(context, "TrimAudio", cmd, listener);
    }

    public static void replaceAudioInVideo(Context context, String inputVideo, String inputAudio, String outputFile, final FFMpegSimpleListener listener) {
        String[] cmd = generateReplaceAudioVideoCommand(inputVideo, inputAudio, outputFile);
        ffmpegSimpleCommand(context, "replaceAudioInVideo", cmd, listener);
    }

    public static void addAudioToVideo(Context context, String inputVideo, String inputAudio, String outputFile, final FFMpegSimpleListener listener) {
        String[] cmd = generateAddAudioToVideoCommand(inputVideo, inputAudio, outputFile);
        ffmpegSimpleCommand(context, "AddAudioToVideo", cmd, listener);
    }

    public static void extractAudio(Context context, String inputVideo, String outputAudio, final FFMpegSimpleListener listener) {
        String[] cmd = generateExtractSoundCommand(inputVideo, outputAudio);
        ffmpegSimpleCommand(context, "ExtractAudio", cmd, listener);
    }

    public static void mergeTwoAudioFiles(Context context, String inputAudio1, String inputAudio2, String outputAudio, final FFMpegSimpleListener listener) {
        String[] cmd = generateMergeTwoAudioFilesCommand(inputAudio1, inputAudio2, outputAudio);
        ffmpegSimpleCommand(context, "mergeTwoAudioFiles", cmd, listener);
    }

    public static void changeFrameRate(Context context, String inputVideo, String outputVideo, int frameRate, final FFMpegSimpleListener listener) {
        String[] cmd = generateFrameRateCommand(inputVideo, outputVideo, frameRate);
        ffmpegSimpleCommand(context, "ChangeFrameRate", cmd, listener);
    }

    public static void splitVideoToPictures(Context context, String inputFile, String fileNamesPattern, float videoFPS, final FFMpegSimpleListener listener) {
        String[] cmd = generateSplitCommand(inputFile, fileNamesPattern, videoFPS);
        ffmpegSimpleCommand(context, "SplitVideo", cmd, listener);
    }

    public static void splitVideoToPictures(Context context, String inputFile, String fileNamesPattern, final FFMpegSimpleListener listener) {
        String[] cmd = generateSplitCommand(inputFile, fileNamesPattern);
        ffmpegSimpleCommand(context, "SplitVideo", cmd, listener);
    }
    public static void getFileInfo(Context context, String inputFile, final FFMpegSimpleListener listener) {
        String[] cmd = generateGetInfoCommand(inputFile);
        ffmpegSimpleCommand(context, "GetInfo", cmd, listener);
    }

    public static void convertImageToVideo(Context context, String inputImageFile, String outputVideoFile, final FFMpegSimpleListener listener) {
        String[] cmd = generateImageToVideo(inputImageFile, outputVideoFile);
        ffmpegSimpleCommand(context, "ImageToVideo", cmd, listener);
    }

    public static void addWaterMark(Context context, String inputVideoFile, String waterMarkFile, int left, int top, String outputVideoFile, final FFMpegSimpleListener listener) {
        String[] cmd = generateAddWaterMarkCommand(inputVideoFile, waterMarkFile, left, top, outputVideoFile);
        ffmpegSimpleCommand(context, "AddWaterMark", cmd, listener);
    }

    public static void encodeVideoFile(Context context, String inputVideoFile, String outputFile, FFMpegSimpleListener listener) {
        String[] cmd = generateEncodeCommand(inputVideoFile, outputFile);
        ffmpegSimpleCommand(context, "DecodeVideoFile", cmd, listener);
    }

    public static void cleanNoiseFromAudio(Context context, String inputAudioFile, String outputAudioFile, FFMpegSimpleListener listener) {
        String[] cmd = generateCleanNoiseCommand(inputAudioFile, outputAudioFile);
        ffmpegSimpleCommand(context, "CleanNoise", cmd, listener);
    }

    public static void streamToServer(Context context, String inputVideoFile, String serverURL, FFMpegSimpleListener listener) {
        String[] cmd = generateStreamCommand(inputVideoFile, serverURL);
        ffmpegSimpleCommand(context, "StreamToServer", cmd, listener);
    }

    public static void setKeyFrameInterval(Context context, String inputVideoFile, String outputVideoFile, int keyFrameInterval, FFMpegSimpleListener listener) {
        String[] cmd = generateSetKeyFramesCommand(inputVideoFile, outputVideoFile, keyFrameInterval);
        ffmpegSimpleCommand(context, "SetKeyFrames", cmd, listener);
    }


    ///////////////////////////////////GENERATE COMMANDS //////////////////////////////////////////////////////////

    public static String[] generateGetFrameByNumCommand(int frameNum, String file, String outputFile) {
        return new String[]{"-y", "-i", file, "-vf", "select=gte(n\\," + frameNum + ")", "-vframes", "1", outputFile};
    }

    public static String[] generateConcatCommand(List<String> files, String outputFile) {
        ArrayList<String> result = new ArrayList<>();
        result.add("-y");
        result.add("-i");

        StringBuilder commandFiles = new StringBuilder();
        commandFiles.append("concat:");
        for (int i = 0; i < files.size(); i++) {
            commandFiles.append(files.get(i));
            if (i < files.size() - 1) {
                commandFiles.append("|");
            }
        }

        result.add(commandFiles.toString());
        result.add("-c");
        result.add("copy");
        result.add("-bsf:a");
        result.add("aac_adtstoasc");
//        result.add("-movflags");
//        result.add("faststart");
        result.add(outputFile);
        return result.toArray(new String[result.size()]);
    }

    public static String[] generateTranscodeCommand(String fileName, String outputFileName) { // original
        return new String[]{"-y", "-i", fileName,
                "-c", "copy",
                "-bsf:v", "h264_mp4toannexb",
                "-f", "mpegts",
                outputFileName};
    }

    public static String[] generateAddMoovAtomToBeginCommand(String fileName, String outputFileName) {
        return new String[]{"-y", "-i", fileName,
                "-c:v", "copy",
                "-c:a", "copy",
                "-preset", "ultrafast",
                "-strict", "-2",
                "-movflags", "faststart",
                outputFileName};
    }

    public static String[] generateFrameRateCommand(String fileName, String outputFileName, int frameRate) {
        return new String[]{"-y", "-r", String.valueOf(frameRate),
                "-i", fileName, "-r",
                String.valueOf(frameRate),
                "-strict", "-2",
                "-movflags", "faststart",
                outputFileName};
    }

    public static String[] generateAudioTrimCommand(String inputFile, String startTime, String durationTime, String outputFile) {
        return new String[]{"-y",
                "-ss", startTime,
                "-t", durationTime,
                "-i", inputFile,
                "-acodec", "copy",
                outputFile};
    }

    public static String[] generateReplaceAudioVideoCommand(String inputVideo, String inputAudio, String outputFile) {
        return new String[]{"-y", "-i", inputVideo, "-i", inputAudio,
                "-c:v", "copy", "-c:a", "aac",
                "-strict", "experimental",
                "-map", "0:v:0", "-map", "1:a:0",
                outputFile};
    }

    public static String[] generateExtractSoundCommand(String inputVideo, String outputAudio) {
        return new String[]{"-y", "-i", inputVideo,
                "-vn", "-acodec", "copy",
                "-strict", "-2",
                outputAudio};
    }

    public static String[] generateMergeTwoAudioFilesCommand(String inputAudio1, String inputAudio2, String outputAudio) {
        return new String[] {"-y", "-i", inputAudio1, "-i", inputAudio2,
                "-filter_complex", "amerge", "-ac", "2", "-c:a", "aac", "-q:a", "4",
                "-strict", "-2",
                outputAudio};
    }

    public static String[] generateAddAudioToVideoCommand(String inputVideo, String inputAudio, String outputFile) {
        return new String[]{"-y", "-i", inputVideo, "-i", inputAudio,
                "-c:v", "copy", "-c:a", "copy",
                "-shortest",
                "-strict", "experimental",
                outputFile};
    }


    private static String[] generateSplitCommand(String inputFile, String fileNamesPattern, float videoFPS) {
        return new String[]{"-y", "-i", inputFile, "-vf", "fps=" + videoFPS,  fileNamesPattern};
    }

    private static String[] generateSplitCommand(String inputFile, String fileNamesPattern) {
        return new String[]{"-y", "-i", inputFile,  fileNamesPattern};
    }

    private static String[] generateGetInfoCommand(String inputFile) {
        return new String[]{"-y", "-i", inputFile};
    }

    private static String[] generateImageToVideo(String inputImageFile, String outputVideoFile) {
        return new String[]{"-y",
                "-loop", "1",
                "-i", inputImageFile,
                "-c:v", "libx264",
                "-t", "00:00:00.100",
                "-vf", "fps=20",
                "-pix_fmt", "yuv420p",
                "-preset", "ultrafast",
                outputVideoFile};
    }

    private static String[] generateAddWaterMarkCommand(String inputVideoFile, String waterMarkFile,
                                                        int left, int top, String outputVideoFile) {
        return new String[]{"-y", "-i", inputVideoFile, "-i", waterMarkFile,
                "-filter_complex", "overlay=" + left + ":" + top,outputVideoFile};
    }

    private static String[] generateStreamCommand(String inputVideoFile, String serverURL) {
        return new String[]{"-i", inputVideoFile,
                "-movflags", "isml+frag_keyframe",
                "-f", "ismv",
                "-strict", "-2",
                serverURL};
    }


    private static String[] generateEncodeCommand(String inputVideoFile, String outputFile) {
        return new String[]{"-y", "-i", inputVideoFile,
                "-c:v", "libx264", "-f", "mpegts",
                "-movflags", "faststart",
                outputFile};
    }


    private static String[] generateCleanNoiseCommand(String inputAudioFile, String outputAudioFile) {
        return new String[]{"-y", "-i", inputAudioFile,
                "-af", "highpass=f=300, lowpass=f=3000",
                "-preset", "ultrafast",
                "-strict", "-2",
                outputAudioFile};
    }

    private static String[] generateSetKeyFramesCommand(String inputVideoFile, String outputVideoFile, int goalKeyFrameInterval) {
        return new String[]{"-y", "-i", inputVideoFile,
                "-vcodec", "libx264",
                "-x264-params", "keyint=" + goalKeyFrameInterval + ":no-scenecut=1",
                "-acodec", "copy",
                "-preset", "ultrafast",
                outputVideoFile};
    }

    public static void killCurrentProcess(Context context) {
        FFmpeg.getInstance(context).killRunningProcesses();
    }
}
