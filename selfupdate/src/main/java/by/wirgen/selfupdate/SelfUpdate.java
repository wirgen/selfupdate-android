package by.wirgen.selfupdate;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public abstract class SelfUpdate {
    private final int TASK_CHECK = 0;
    private final int TASK_UPDATE = 1;

    private final Context context;
    private final String url;
    private final String name;
    private final int version;

    private int lastTask;
    private UpdatesTask updatesTask;

    public SelfUpdate(Context context, String url, String name, int version) {
        this.context = context;
        this.url = url;
        this.name = name;
        this.version = version;

        context.deleteFile("update.apk");
    }

    public void applicationUpdateCheck() {
        lastTask = TASK_CHECK;
        startCheckUpdate();
    }

    public void applicationUpdateApk() {
        lastTask = TASK_UPDATE;
        startCheckUpdate();
    }

    private void startCheckUpdate() {
        if (updatesTask != null) {
            updatesTask.cancel(true);
        }
        updatesTask = new UpdatesTask();
        updatesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }

    private void getResultFromTask(UpdateInfo[] updateInfoArray) {
        if (updateInfoArray != null) {
            for (UpdateInfo updateInfo : updateInfoArray) {
                if (updateInfo.getName().equals(name)) {
                    boolean isNew = updateInfo.getVersion() > version;
                    switch (lastTask) {
                        case TASK_CHECK:
                            updateResult(isNew);
                            break;
                        case TASK_UPDATE:
                            if (isNew) {
                                applicationUpdate(updateInfo.getTitle(), updateInfo.getLink());
                            }
                            break;
                    }
                    break;
                }
            }
        }
    }

    private void applicationUpdate(String title, String url) {
        final String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/update.apk";
        final Uri uri = Uri.parse("file://" + filePath);

        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(title);
        request.setDestinationUri(uri);

        final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);

        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context cntx, Intent intent) {
                Intent install = new Intent(Intent.ACTION_VIEW);
                install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                install.setDataAndType(uri, manager.getMimeTypeForDownloadedFile(downloadId));
                context.startActivity(install);

                context.unregisterReceiver(this);
            }
        };

        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    protected abstract void updateResult(boolean result);

    private class UpdatesTask extends AsyncTask<String, Void, UpdateInfo[]> {

        @Override
        protected UpdateInfo[] doInBackground(String... urls) {
            UpdateInfo[] updateInfoArray = null;

            try {
                URL url = new URL(urls[0]);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new InputSource(url.openStream()));
                doc.getDocumentElement().normalize();

                NodeList nodeList = doc.getElementsByTagName("application");

                updateInfoArray = new UpdateInfo[nodeList.getLength()];

                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    Element fstElmnt = (Element) node;

                    NodeList itemList = fstElmnt.getElementsByTagName("title");
                    Element itemElement = (Element) itemList.item(0);
                    itemList = itemElement.getChildNodes();
                    String title = itemList.item(0).getNodeValue();

                    itemList = fstElmnt.getElementsByTagName("name");
                    itemElement = (Element) itemList.item(0);
                    itemList = itemElement.getChildNodes();
                    String name = itemList.item(0).getNodeValue();

                    itemList = fstElmnt.getElementsByTagName("version");
                    itemElement = (Element) itemList.item(0);
                    itemList = itemElement.getChildNodes();
                    int version = Integer.parseInt(itemList.item(0).getNodeValue());

                    itemList = fstElmnt.getElementsByTagName("link");
                    itemElement = (Element) itemList.item(0);
                    itemList = itemElement.getChildNodes();
                    String link = itemList.item(0).getNodeValue();

                    itemList = fstElmnt.getElementsByTagName("timestamp");
                    itemElement = (Element) itemList.item(0);
                    itemList = itemElement.getChildNodes();
                    int timestamp = Integer.parseInt(itemList.item(0).getNodeValue());

                    updateInfoArray[i] = new UpdateInfo(title, name, version, link, timestamp);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return updateInfoArray;
        }

        @Override
        protected void onPostExecute(UpdateInfo[] updateInfoArray) {
            super.onPostExecute(updateInfoArray);
            getResultFromTask(updateInfoArray);
        }
    }
}
