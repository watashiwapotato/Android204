package com.tqc.gdd02;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class GDD02 extends Activity {
    public static boolean bIfDebug = false;
    public static String TAG = "HIPPO_DEBUG";

    private TextView mTextView01;
    private Button mButton01, mButton02;
    private ListView mListView01;
    private ArrayList<String> lst = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private static final int API_MSG_PARSE_START = 1001;
    private static final int API_MSG_PARSE_OK = 1002;
    private static final int API_MSG_PARSE_ERROR = 2001;
    private HandlerThread handlerThread;
    private Handler mBackgroundHandler;
    private Handler mForegroundHandler;
    private boolean mPaused = false;
    private String strFileName = "myxml.xml";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        init();
    }

    private void init() {
        mTextView01 = (TextView) findViewById(R.id.main_textView1);
        mButton01 = (Button) findViewById(R.id.main_button1);
        mButton02 = (Button) findViewById(R.id.main_button2);
        mListView01 = (ListView) findViewById(R.id.main_listView1);

        handlerThread = new HandlerThread("BackgroundThread");
        handlerThread.start();
        MyHandlerCallback callback = new MyHandlerCallback();
        mBackgroundHandler = new Handler(handlerThread.getLooper(), callback);
        mForegroundHandler = new Handler(callback);

        mButton01.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                startParse(strFileName);
            }
        });
        mButton02.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView01.setText("解析XML");
                // 按下重設按鈕，清空下方ListView
                // TO DO
                if (lst != null && adapter != null) {
                    lst.clear();
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    class MyHandlerCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(final Message msg) {
            switch (msg.what) {
                case API_MSG_PARSE_START:
                    //  mTextView01顯示解析中，請稍候
                    // TO DO
                    mForegroundHandler.obtainMessage(API_MSG_PARSE_START, strFileName).sendToTarget();
                    mTextView01.setText(R.string.str_parsing);
                    break;
                case API_MSG_PARSE_OK:
                    mTextView01.setText(getString(R.string.str_parsing_ok));
                    if (!mPaused) {
                        try {
                            //  呼叫updateListView()方法，更新ListView內容。
                            // TO DO
                            updateListView((ArrayList<Product>) msg.obj);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, e.toString());
                        }
                    }
                    break;
                case API_MSG_PARSE_ERROR:
                    if (!mPaused) {
                        if (msg.obj != null) {
                            Log.e(TAG, msg.obj.toString());
                        }
                    }
                    break;
            }
            return false;
        }
    }

    private void startParse(final String strFileName) {
        mForegroundHandler.obtainMessage(API_MSG_PARSE_START, strFileName).sendToTarget();
        new Thread(new Runnable() {
            @Override
            public void run() {
                XmlPullParserFactory pullParserFactory;
                try {
                    pullParserFactory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = pullParserFactory.newPullParser();

                    //  透過getAssets()將/assets/myxml.xml檔案載入後，轉型為 InputStream 物件。
                    InputStream in_s = getAssets().open(strFileName); // TO DO
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    parser.setInput(in_s, null);
                    ArrayList<Product> products = parseXML(parser);

                    //  解析完畢，利用執行敘mForegroundHandler傳送訊息Message.what為API_MSG_PARSE_OK，並將products傳入MyHandlerCallback處理。
                    // TO DO
                    mForegroundHandler.obtainMessage(API_MSG_PARSE_OK, products).sendToTarget();
                } catch (XmlPullParserException e) {
                    mForegroundHandler.obtainMessage(API_MSG_PARSE_ERROR, e.toString()).sendToTarget();
                    e.printStackTrace();
                } catch (IOException e) {
                    mForegroundHandler.obtainMessage(API_MSG_PARSE_ERROR, e.toString()).sendToTarget();
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private ArrayList<Product> parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<Product> products = null;
        int eventType = parser.getEventType();
        Product currentProduct = null;

        // parseXML()方法可將傳入之XmlPullParser物件，解析XML文件後回傳自訂ArrayList<Product>物件。
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name = null;
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    // XML文件開始，初始化 products 物件
                    //  TO DO
                    products = new ArrayList<Product>();
                    break;
                case XmlPullParser.START_TAG:
                    // 處理XML TAG標記
                    name = parser.getName();
                    // 利用if-else，解析XML文件的TAG標記
                    // TO DO, Hint: if(name.equalsIgnoreCase(""))
                    if (name.equalsIgnoreCase("product")) {
                        currentProduct = new Product();
                    }else if (name.equalsIgnoreCase("productname")){
                        currentProduct.name = parser.nextText();
                    }else if (name.equalsIgnoreCase("productquantity")){
                        currentProduct.quantity = parser.nextText();
                    }else if (name.equalsIgnoreCase("productcolor")) {
                        currentProduct.color =parser.nextText();
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("product") && currentProduct != null) {
                        products.add(currentProduct);
                    }
            }
            eventType = parser.next();
        }
        // Delay for testing
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }//

        return products;
    }

    private void updateListView(ArrayList<Product> products) {
        lst = new ArrayList<>();
        if (products != null && products.size() > 0) {
            for (int i = 0; i < products.size(); i++) {
                //  自傳入的products物件中，取出產品名稱以及產品顏色，加入至lst物件中
                // TO DO
                Product product= products.get(i);
                String item = product.name + "(" +product.color +")";
                lst.add(item);
            }
            adapter = new ArrayAdapter<String>(GDD02.this, android.R.layout.simple_list_item_1, lst);
            mListView01.setAdapter(adapter);
            //  為ListView設定點選選項時，將選中的品項以Toast訊息方式顯示於畫面中：「你選擇的是:xxx」
            // TO DO
            mListView01.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String item = lst.get(position);
                    Toast.makeText(GDD02.this,"你選擇的是:"+item,Toast.LENGTH_SHORT ).show();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPaused = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBackgroundHandler.removeMessages(API_MSG_PARSE_OK);
        mBackgroundHandler.getLooper().quit();
    }
}
