package com.ujuzicode.mobilebluetothprinter;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ujuzicode.mobilebluetothprinter.printer.Global;
import com.ujuzicode.mobilebluetothprinter.printer.PocketPos;
import com.ujuzicode.mobilebluetothprinter.printer.WorkService;
import com.ujuzicode.mobilebluetothprinter.utils.DateUtil;
import com.ujuzicode.mobilebluetothprinter.utils.FontDefine;
import com.ujuzicode.mobilebluetothprinter.utils.Printers;
import com.ujuzicode.mobilebluetothprinter.utils.Util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MobileBluetothPrinter_Main  extends AppCompatActivity implements View.OnClickListener {

    private int  PICK_BT_LIST_REQUEST= 2;

    private int  BLUETOOTH_REQUEST= 3;

    private Button  mPrint, mBarCode;

    private TextView toolbar_title;

    private static Handler mHandler = null;

    private MenuItem activate_item,activated_item;

    private String strTransaction = "435353535435353"; //Sample transaction number

    private static int nBarcodetype, nStartOrgx, nBarcodeWidth = 1,
            nBarcodeHeight = 3, nBarcodeFontType, nBarcodeFontPosition = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_bluetoth_printer_main);
        Toolbar mToolbar = findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar_title = mToolbar.findViewById(R.id.toolbar_title);
        toolbar_title.setText(Html.fromHtml("<font color=\"#FFFFFF\"><b>" + getResources().getString(R.string.app_name) + "</b></font>"));

        Util.InitGlobalString(MobileBluetothPrinter_Main.this);

        if (null == WorkService.workThread) {
            Intent intent = new Intent(this, WorkService.class);
            startService(intent);
        }

        mPrint = findViewById(R.id.btnPrint);
        mBarCode = findViewById(R.id.btnBarCode);
        mPrint.setOnClickListener(this);
        mBarCode.setOnClickListener(this);
        mPrint.setEnabled(false);
        mBarCode.setEnabled(false);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnPrint:
                mHandler = new mHandler(this);
                WorkService.addHandler(mHandler);
                printSampleReceipt();
                break;

            case R.id.btnBarCode:
                mHandler = new mHandler(this);
                WorkService.addHandler(mHandler);
                printSampleBarcode();
                break;
        }

    }


    private void printSampleReceipt() {
        String date = DateUtil.timeMilisToString(System.currentTimeMillis(), "dd-MM-yy / HH:mm");
        String companyNameStr	=
                        "\nCUSTOMER NAME" + "\n"
                        +"PHONE NUMBER"+ "\n";

        String orderStr = "\nORDER ID" +"\n"
                        +"ORDER DATE: "+ date + "\n";

        String titleStr	="SALES"+ "\n\n";

        String headerStr = "Items      Qnt.      UP.     Amt\n";

        StringBuilder contentItem	= new StringBuilder();
        StringBuilder contentSb	= new StringBuilder();
        StringBuilder contentSvdt	= new StringBuilder();


        String itemName = "IPHONE 15 PRO MAX";
        if (itemName.length() > 30){
            List<String> itemNameChunks = splitString(itemName, 30);

            int numChunks = itemNameChunks.size();
            for (int i = 0; i < numChunks; i++) {
                String chunk = itemNameChunks.get(i);
                contentItem.append(chunk).append("\n");
            }

        }else {
            contentItem.append(itemName).append("\n");
        }


        Log.d("Debug", "ContentSb: " + contentSb.toString());

        contentSb.append("3").append("         ");
        contentSb.append("500" + "         ");
        contentSb.append("1500" + "\n\n");


        contentSvdt.append("SUBTOTAL    : " +"518" + "\n");
        contentSvdt.append("VAT    : " +"2.0" + "\n");
        contentSvdt.append("DISCOUNT    : " +"0.0" + "\n");
        contentSvdt.append("TOTAL PRICE    : " +"1500" + "\n\n");


        byte[] companyNameByte	= Printers.printfont(companyNameStr, FontDefine.FONT_32PX,FontDefine.Align_CENTER,(byte)0x1A, PocketPos.LANGUAGE_ENGLISH);

        byte[] orderByte	= Printers.printfont(orderStr, FontDefine.FONT_32PX_UNDERLINE,FontDefine.Align_LEFT,(byte)0x1A, PocketPos.LANGUAGE_ENGLISH);

        byte[] titleByte	= Printers.printfont(titleStr, FontDefine.FONT_32PX_UNDERLINE,FontDefine.Align_CENTER,(byte)0x1A, PocketPos.LANGUAGE_ENGLISH);

        byte[] headerByte = Printers.printfont(headerStr, FontDefine.FONT_32PX_UNDERLINE, FontDefine.Align_LEFT, (byte) 0x1A, PocketPos.LANGUAGE_ENGLISH);

        byte[] contentItem2Byte	= Printers.printfont(contentItem.toString(), FontDefine.FONT_24PX,FontDefine.Align_LEFT,(byte)0x1A, PocketPos.LANGUAGE_ENGLISH);

        byte[] content2Byte	= Printers.printfont(contentSb.toString(), FontDefine.FONT_24PX,FontDefine.Align_RIGHT,(byte)0x1A, PocketPos.LANGUAGE_ENGLISH);

        byte[] contentSp2Byte	= Printers.printfont(contentSvdt.toString(), FontDefine.FONT_24PX,FontDefine.Align_RIGHT,(byte)0x1A, PocketPos.LANGUAGE_ENGLISH);


        //initialize array size
        byte[] totalByte	= new byte[
                companyNameByte.length
                        + orderByte.length
                        + titleByte.length
                        + headerByte.length
                        + contentItem2Byte.length
                        + content2Byte.length
                + contentSp2Byte.length];

        /*
        System.arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
        src − This is the source array.
        srcPos − This is the starting position in the source array.
        dest − This is the destination array.
        destPos − This is the starting position in the destination data.
        length − This is the number of array elements to be copied.
        */

        int offset = 0;
        System.arraycopy(companyNameByte, 0, totalByte, offset, companyNameByte.length);
        offset += companyNameByte.length;

        System.arraycopy(orderByte, 0, totalByte, offset, orderByte.length);
        offset += orderByte.length;

        System.arraycopy(titleByte, 0, totalByte, offset, titleByte.length);
        offset += titleByte.length;

        System.arraycopy(headerByte, 0, totalByte, offset, headerByte.length);
        offset += headerByte.length;

        System.arraycopy(contentItem2Byte, 0, totalByte, offset, contentItem2Byte.length);
        offset += contentItem2Byte.length;

        System.arraycopy(content2Byte, 0, totalByte, offset, content2Byte.length);
        offset += content2Byte.length;

        System.arraycopy(contentSp2Byte, 0, totalByte, offset, contentSp2Byte.length);

        Bundle data = new Bundle();
        data.putByteArray(Global.BYTESPARA1, totalByte);
        data.putInt(Global.INTPARA1, 0);
        data.putInt(Global.INTPARA2, totalByte.length);

        Log.d("Debug", "main data: " + data.toString());

        WorkService.workThread.handleCmd(Global.CMD_POS_WRITE, data);

    }
    // Utility method to split a string into chunks of a given length
    private List<String> splitString(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += chunkSize) {
            chunks.add(text.substring(i, Math.min(i + chunkSize, text.length())));
        }
        return chunks;
    }


    private void printSampleBarcode() {

        int nOrgx = nStartOrgx * 12;
        int nType = 0x41 + nBarcodetype;
        int nWidthX = nBarcodeWidth + 2;
        int nHeight = (nBarcodeHeight + 1) * 24;
        int nHriFontType = nBarcodeFontType;
        int nHriFontPosition = nBarcodeFontPosition;

        Bundle barcode_data = new Bundle();
        barcode_data.putString(Global.STRPARA1, strTransaction);
        barcode_data.putInt(Global.INTPARA1, nOrgx);
        barcode_data.putInt(Global.INTPARA2, nType);
        barcode_data.putInt(Global.INTPARA3, nWidthX);
        barcode_data.putInt(Global.INTPARA4, nHeight);
        barcode_data.putInt(Global.INTPARA5, nHriFontType);
        barcode_data.putInt(Global.INTPARA6, nHriFontPosition);

        WorkService.workThread.handleCmd(Global.CMD_POS_SETBARCODE,barcode_data);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        activate_item = menu.findItem(R.id.action_bluetooth_activate);

        activate_item.setVisible(true);

        activated_item = menu.findItem(R.id.action_bluetooth_deactivate);

        activated_item.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (WorkService.workThread.isConnecting()) {
            Toast.makeText(this, getString(R.string.bluetooth_connecting),
            Toast.LENGTH_SHORT).show();
            return true;
        }
        switch (item.getItemId()) {

            case R.id.action_bluetooth_activate:

                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter .getDefaultAdapter();

                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent , BLUETOOTH_REQUEST);

                }
                else{
                    Intent intent = new Intent(MobileBluetothPrinter_Main.this, MobileBluetothPrinter_PrinterList.class);
                    startActivityForResult(intent, PICK_BT_LIST_REQUEST);
                }


                return  true;

            case R.id.action_bluetooth_deactivate:
                WorkService.workThread.disconnectBt();
                stopService(new Intent(this, WorkService.class));
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_BT_LIST_REQUEST) {

            if (resultCode == RESULT_OK) {
                String sName = data.getStringExtra("sName");
                toolbar_title.setText(Html.fromHtml("<font color=\"#FFFFFF\"><b>MBT Printer (Printer: " + sName + ")</b></font>"));
                mPrint.setEnabled(true);
                mBarCode.setEnabled(true);
                activate_item.setVisible(false);
                activated_item.setVisible(true);

            }

            if (resultCode == RESULT_CANCELED) {
                activate_item.setVisible(true);
                activated_item.setVisible(false);
                Log.i("Printer Name","no device obtained");}

        }

        if (requestCode == BLUETOOTH_REQUEST) {

            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(MobileBluetothPrinter_Main.this, MobileBluetothPrinter_PrinterList.class);
                startActivityForResult(intent, PICK_BT_LIST_REQUEST);
            }

        }
    }



    static class mHandler extends Handler {

        WeakReference<MobileBluetothPrinter_Main> mActivity;

        mHandler(MobileBluetothPrinter_Main activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MobileBluetothPrinter_Main activity = mActivity.get();
            switch (msg.what) {

                case Global.CMD_POS_WRITERESULT: {
                    int result = msg.arg1;
                    Toast.makeText(
                            activity,
                            (result == 1) ? Global.toast_success: Global.toast_fail, Toast.LENGTH_SHORT).show();

                    break;
                }

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WorkService.delHandler(mHandler);
        mHandler = null;
    }
}
