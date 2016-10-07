package oliver.com.atestffmpeg;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;


/**
 * Created by oliver on 04/03/16.
 */
public class Utils {
    private static final String TAG = "Utils";
    public static final String SOLO_FILES_DIR = "SOLO";
    public static final String TEMP_FILES_DIR = "tmp";
    public static final String BG_RESOURCES_DIR = "backgrounds";
    private static final SimpleDateFormat mDateFormat = new SimpleDateFormat("ddMMyy_HHmmss", Locale.US);

    public static String getFilePath(Context context, String fileName) {
        String filePath;
        if ((Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) ||
                (!Environment.isExternalStorageRemovable())) {
            filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();
            Log.d(TAG, "External storage: " + filePath);
        } else {
            filePath = context.getFilesDir().getAbsolutePath();
            Log.d(TAG, "Internal storage: " + filePath);
        }
        File file = new File(filePath, fileName);
        return file.getAbsolutePath();
    }

    public static String getFileFromTempDir(Context context, String fileName) {
        File dir = new File(getFilePathFromAppDir(context, TEMP_FILES_DIR));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        Log.d(TAG, "getFileFromTempDir file: " + file.getAbsolutePath());
        return file.getAbsolutePath();
    }

    public static String getFileFromBGResourcesDir(Context context, String fileName) {
        File dir = new File(getFilePathFromAppDir(context, BG_RESOURCES_DIR));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        Log.d(TAG, "getFileFromBGResourcesDir: " + file.getAbsolutePath());
        return file.getAbsolutePath();
    }

    public static String getFilePathFromAppDir(Context context, String fileName) {
        File dir = new File(getFilePath(context, SOLO_FILES_DIR));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        Log.d(TAG, "getFilePathFromAppDir file: " + file.getAbsolutePath());
        return file.getAbsolutePath();
    }


    public static void clearTempDir(Context context) {
        clearDir(context, new File(getFilePathFromAppDir(context, TEMP_FILES_DIR)));
    }

    public static void clearBackgroundsDir(Context context) {
       clearDir(context, new File(getFilePathFromAppDir(context, BG_RESOURCES_DIR)));
    }

    public static void clearDir(Context context, File directory) {
        if (directory == null) return;
        if (directory.isDirectory()) {
            File[] childFiles = directory.listFiles();
            if (childFiles == null) return;
            for (File child : childFiles) {
                Log.d(TAG, "delete " + child.getAbsolutePath());
                if (child.isFile()) {
                    child.delete();
                } else {
                    clearDir(context, child);
                }
            }
        }
    }

    public static String removeExt(String fileName) {
        if (fileName.lastIndexOf(".") != 0) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }

    public static String getFileExtension(String fileName) {
        if (TextUtils.isEmpty(fileName)) return "";
        if (fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return "";
    }


    public static void goToMarket(Context context, String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=" + packageName + "&hl=en"));
        context.startActivity(intent);
    }

    public static boolean isPackageInstalled(String packagename, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    public static String getLastFrame(String fileDir, final String filePrefix) {
        File fileDirectory = new File(fileDir);
        File[] frames = fileDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.startsWith(filePrefix))
                    return true;
                return false;
            }
        });
        if (frames.length == 0) return null;
        for (int i = 0; i < frames.length - 1; i++) {
            frames[i].delete();
        }
        File lastFrame = frames[frames.length - 1];
        Log.d(TAG, "Frames " + frames.length);
        File newFileName = new File(fileDirectory,"LastFrame.png");

        if (newFileName.exists())
            newFileName.delete();

        boolean success = lastFrame.renameTo(newFileName);
        return success ? newFileName.getAbsolutePath() : lastFrame.getAbsolutePath();
    }

    public static String getFirstFrame(String fileDir, final String filePrefix) {
        File fileDirectory = new File(fileDir);
        File[] frames = fileDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.startsWith(filePrefix))
                    return true;
                return false;
            }
        });
        if (frames.length == 0)
            return null;
        File firstFrame = frames[0];
        File newFileName = new File(fileDirectory, "FirstFrame.png");

        if (newFileName.exists())
            newFileName.delete();
        try {
            copy(firstFrame, newFileName);
            return newFileName.getAbsolutePath();
        } catch (IOException e) {
            return firstFrame.getAbsolutePath();
        }
    }

    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static UUID generateUUID() {
        return UUID.randomUUID();
    }


    private static boolean isAvailableConnection(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return (networkInfo != null);
    }

    public static String arrayToString(byte[] array, int start, int end) {
        if (start < 0)
            return "Start " + start +" < 0";
        if (end > array.length)
            return "End (" + end + ") > array.length (" + array.length + ")";
        if (start > end)
            return "Start " + start +" > end (" + end + ")";

        StringBuilder result = new StringBuilder();
        result.append("Array[");
        for (int i = start; i < end; i++) {
            result.append(array[i] + ", ");
        }
        result.append("]");
        return result.toString();
    }

    public static String getDeviceFirm() {
        return Build.MANUFACTURER;
    }



    public static String saveImageToFile(Bitmap bm, String fileName) {

        // write the bytes in file
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(fileName);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }


    /**
     * Detects and toggles immersive mode (also known as "hidey bar" mode).
     */
    public static void toggleHideBar(View decorView) {
        decorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
//                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
//                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
//                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public static int getFrameCount(String message) {
        String startWord = "frame=";
        String endWord = " fps=";
        String frameNum = message.substring(message.lastIndexOf(startWord) + startWord.length(), message.lastIndexOf(endWord));
//        Log.d(TAG, "getFrameCount " + frameNum);
        int framesCount = Integer.parseInt(frameNum.trim());
        Log.d(TAG, "getFrameCount " + framesCount);
        return framesCount;
    }

    public static String getVideoDuration(String message) {
        String startWord = "Duration:";
        String endWord = ", start";
        String videoDuration = message.substring(message.indexOf(startWord) + startWord.length(), message.indexOf(endWord));
//        Log.d(TAG, "getFrameCount " + frameNum);
        Log.d(TAG, "getVideoDuration " + videoDuration);
        return videoDuration.trim();
    }

    public static String timeToString(int startMS) {
        String pattern = "ss.SSS";
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(startMS);
        String result = format.format(calendar.getTime());
        Log.d(TAG, "timeToString ms: " + startMS + " result: " + result);
        return "00:00:" +result;
    }

    public static int stringToMS(String string) {
        if (TextUtils.isEmpty(string))
            return 0;
        try {
            String pattern = "HH:mm:ss.SS";
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
            Date date = dateFormat.parse(string);
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTime(date);
            int ms = calendar.get(Calendar.MILLISECOND) +
                    1000 * calendar.get(Calendar.SECOND);
            Log.d(TAG, "timeToString str: " + string + " result: " + ms);
            return ms;
        } catch (Exception e) {

        }
        return 0;
    }

    public static String extractUnicLines(String str1, String str2, String lineSeparator) {

        ArrayList<String> listLines1 = new ArrayList<>(Arrays.asList(str1.split(lineSeparator)));
        ArrayList<String> listLines2 = new ArrayList<>(Arrays.asList(str2.split(lineSeparator)));
        Set<String> unicLines = new LinkedHashSet<>();

        for (String line : listLines1) {
            if (!TextUtils.isEmpty(line))
                unicLines.add(line);
        }
        for (String line : listLines2) {
            if (!TextUtils.isEmpty(line))
                unicLines.add(line);
        }
        StringBuilder result = new StringBuilder();
        for(String line : unicLines) {
            result.append(line);
            result.append(lineSeparator);
        }

        return result.toString();
    }


    private static final int MIN_LINES_LENGTH = 3;
    public static String extractCommonUnicLines(String str1, String str2, String lineSeparator) {

        ArrayList<String> listLines1 = new ArrayList<>(Arrays.asList(str1.split(lineSeparator)));
        ArrayList<String> listLines2 = new ArrayList<>(Arrays.asList(str2.split(lineSeparator)));

        Set<String> theSameUnicLines = new LinkedHashSet<>();

        for (String line : listLines1) {
            if (!TextUtils.isEmpty(line) &&
                    listLines2.contains(line) &&
                    line.length() > MIN_LINES_LENGTH) {
                theSameUnicLines.add(line);
            }
        }
        for (String line : listLines2) {
            if (!TextUtils.isEmpty(line) &&
                    listLines1.contains(line) &&
                    line.length() > MIN_LINES_LENGTH) {
                theSameUnicLines.add(line);
            }
        }

        StringBuilder result = new StringBuilder();
        for(String line : theSameUnicLines) {
            result.append(line);
            result.append(lineSeparator);
        }

        return result.toString();
    }

    /**
     * cut lines stored in linesToCutStr parameter from sourceStr lines
     * @param sourceStr
     * @param linesToCutStr
     * @param lineSeparator
     * @return
     */
    public static String cutLines(String sourceStr, String linesToCutStr, String lineSeparator) {

        ArrayList<String> sourceLines = new ArrayList<>(Arrays.asList(sourceStr.split(lineSeparator)));
        ArrayList<String> linesToCutList = new ArrayList<>(Arrays.asList(linesToCutStr.split(lineSeparator)));

        ArrayList<String> resultLines = new ArrayList<>();

        for (String line :sourceLines) {
            if (!linesToCutList.contains(line))
                resultLines.add(line);
        }

        StringBuilder result = new StringBuilder();
        for(String line : resultLines) {
            result.append(line);
            result.append(lineSeparator);
        }

        return result.toString();
    }


    public static long getDisplayRefreshNsec(Context context) {
        Display display = ((WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        double displayFps = display.getRefreshRate();
        long refreshNs = Math.round(1000000000L / displayFps);
        Log.d(TAG, "refresh rate is " + displayFps + " fps --> " + refreshNs + " ns");
        return refreshNs;
    }

    public static void setMediaPlayerDataSource(Context context,
                                                MediaPlayer mp, String fileInfo) throws Exception {

        if (fileInfo.startsWith("content://")) {
            try {
                Uri uri = Uri.parse(fileInfo);
                fileInfo = getRealPathFromContentUri(context, uri);
                Log.d(TAG, "getRealPathFromContentUri: " + fileInfo);
            } catch (Exception e) {
            }
        }

        try {
            setMediaPlayerDataSourcePostHoneyComb(context, mp, fileInfo);
        } catch (Exception e) {
            try {
                setMediaPlayerDataSourceUsingFileDescriptor(context, mp,
                        fileInfo);
            } catch (Exception ee) {
                String uri = getVideoUriFromPath(context, fileInfo);
                Log.d(TAG, "getVideoUriFromPath : " + uri);
                mp.reset();
                mp.setDataSource(uri);
            }
        }
    }


    private static void setMediaPlayerDataSourcePostHoneyComb(Context context,
                                                              MediaPlayer mp, String fileInfo) throws Exception {
        mp.reset();
        mp.setDataSource(context, Uri.parse(Uri.encode(fileInfo)));
    }

    private static void setMediaPlayerDataSourceUsingFileDescriptor(
            Context context, MediaPlayer mp, String fileInfo) throws Exception {
        File file = new File(fileInfo);
        FileInputStream inputStream = new FileInputStream(file);
        mp.reset();
        mp.setDataSource(inputStream.getFD());
        inputStream.close();
    }

    private static String getVideoUriFromPath(Context context, String path) {
        Uri uri = MediaStore.Video.Media.getContentUri(path);
        Cursor cursor = context.getContentResolver().query(
                uri, null,
                MediaStore.Video.Media.DATA + "='" + path + "'", null, null);
        cursor.moveToFirst();

        long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID));
        cursor.close();

        if (!uri.toString().endsWith(String.valueOf(id))) {
            return uri + "/" + id;
        }
        return uri.toString();
    }

    public static String getRealPathFromContentUri(Context context,
                                                   Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * @param degrees Amount of clockwise rotation from the device's natural position
     * @return Normalized degrees to just 0, 90, 180, 270
     */
    public static int normalize(int degrees) {
        if (degrees > 315 || degrees <= 45) {
            return 0;
        }

        if (degrees > 45 && degrees <= 135) {
            return 90;
        }

        if (degrees > 135 && degrees <= 225) {
            return 180;
        }

        if (degrees > 225 && degrees <= 315) {
            return 270;
        }
        return 0;
//        throw new RuntimeException("The physics as we know them are no more. Watch out for anomalies.");
    }

    public static Uri rawResToUri(Context context, int resID) {
        return Uri.parse("android.resource://" + context.getPackageName() + "/" + resID);
    }

    public static String drawableResIdToUri(int resId) {
        return "drawable://" + resId;
    }


    public static String moveFileToAppDirectory(Context context, String file) {
        String newFileName = generateFileNameDate("SOLO_", getFileExtension(file));

        File newFile = new File(getFilePathFromAppDir(context, newFileName));
//        File newFile = new File(getFilePath(context, newFileName));
        if (newFile.exists())
            newFile.delete();

        try {
            copy(new File(file), newFile);
            return newFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "moveFileToAppDirectory exception: " + e.getMessage());
        }
        return file;
    }

    public static String generateFileNameDate(String prefix, String ext) {
        return prefix + mDateFormat.format(new Date()) + "." + ext;
//        String milis = String.valueOf(System.currentTimeMillis());
//        return prefix + milis.substring(milis.length() / 2) + "." + ext;
    }

    public static Bitmap overlay(Bitmap source, Bitmap waterMark) {
        Bitmap bmOverlay = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());
        if (source.getWidth() != waterMark.getWidth() ||
                source.getHeight() != waterMark.getHeight()) {
            waterMark = Bitmap.createScaledBitmap(waterMark, source.getWidth(), source.getHeight(), true);
        }
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(source, 0, 0, null);
        canvas.drawBitmap(waterMark, 0, 0, null);
        return bmOverlay;
    }

    public static Bitmap createSnapChatImage(Bitmap source, Bitmap waterMark, float aspectRatio) {
        int outputWidth = source.getWidth();
        int outputHeight = (int)(outputWidth / aspectRatio);

        Log.d(TAG, "output sx: " + outputWidth + " sy: " + outputHeight);


        int waterMarkScaledWidth = outputWidth / 4;
        int waterMarkScaledHeight = (waterMarkScaledWidth * waterMark.getHeight()) / waterMark.getWidth();
        Log.d(TAG, "watermark sx: " + waterMarkScaledWidth + " sy: " + waterMarkScaledHeight);

        Bitmap resizedWaterMark = Bitmap.createScaledBitmap(waterMark, waterMarkScaledWidth, waterMarkScaledHeight, true);

        Bitmap output = Bitmap.createBitmap(outputWidth, outputHeight, source.getConfig());
        Canvas canvas = new Canvas(output);
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(source, 0, outputHeight / 2 - source.getHeight() / 2, null);
        canvas.drawBitmap(resizedWaterMark,
                outputWidth - waterMarkScaledWidth - 25,
                outputHeight / 2 + source.getHeight() / 2 + 25, null);

        resizedWaterMark.recycle();
        resizedWaterMark = null;
        return output;
    }

    /**not used anymore*/
    public static String loadJSONFromAsset(Context context, String fileName) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private static String fileToString(Context context, String path) {
        String result = "";
        try {
            File file = new File(path);
            Uri uri = Uri.fromFile(file);

            InputStream is = context.getContentResolver().openInputStream(uri);
            InputStreamReader inputStreamReader = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String tempString = "";
            StringBuilder stringBuilder = new StringBuilder();
            while ((tempString = bufferedReader.readLine()) != null ) {
                stringBuilder.append(tempString);
            }

            is.close();
            result = stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getFileNameFromURL(String URL) {
        String fileNameFromURL = null;
        int lastIndexOfSlash = URL.lastIndexOf("/");
        if (lastIndexOfSlash != 0) {
            fileNameFromURL = URL.substring(lastIndexOfSlash + 1);
        }
        return fileNameFromURL;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static boolean isValidEmail(CharSequence email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    public static String getAppVersionText(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model.toUpperCase();
        }
        return manufacturer.toUpperCase() + " " + model;
    }

    public static Point getScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static boolean fileExist(String fileName) {
        return fileExist(new File(fileName));
//        return fileExist(new File("fake"));
    }

    public  static boolean fileExist(File file) {
        return file != null && file.exists();
    }

}
