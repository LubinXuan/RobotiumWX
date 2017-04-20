package me.robin.espressomodule;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by xuanlubin on 2017/4/20.
 */
public class ContactBroadcastReceiver extends BroadcastReceiver {

    private AtomicLong uid = new AtomicLong(0);


    @Override
    public void onReceive(Context context, Intent intent) {
        String date = "**" + new SimpleDateFormat("MMddHHmm", Locale.CHINA).format(new Date());
        try {
            clearOld(context.getContentResolver());
            JSONArray numbers = new JSONArray(intent.getStringExtra("numbers"));
            for (int i = 0; i < numbers.length(); i++) {
                addContact(context.getContentResolver(), date, numbers.getString(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            String reportAction = intent.getStringExtra("action");
            Intent reportIntent = new Intent(reportAction);
            reportIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            Log.i(WxTestEspresso.TAG, "report:" + reportAction);
            context.sendBroadcast(reportIntent);
        }
    }


    private void clearOld(ContentResolver contentResolver) throws Exception {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            // 取得联系人名字 (显示出来的名字)，实际内容在 ContactsContract.Contacts中
            int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            for (cursor.moveToFirst(); (!cursor.isAfterLast()); cursor.moveToNext()) {
                //获取联系人ID
                String contactId = cursor.getString(idIndex);
                String name = cursor.getString(nameIndex);
                if (null != name && name.startsWith("**")) {
                    //remove from contacts
                    Log.i(WxTestEspresso.TAG, "用户要删除" + name);
                } else {
                    Log.i(WxTestEspresso.TAG, "用户不能删除" + name);
                }
            }
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
    }

    private void delete(String contactId, ContentResolver contentResolver) throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        //delete contact
        ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(ContactsContract.RawContacts.CONTACT_ID + "=" + contactId, null)
                .build());
        //delete contact information such as phone number,email
        ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data.CONTACT_ID + "=" + contactId, null)
                .build());
        contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
    }

    private void addContact(ContentResolver contentResolver, String date, String mobile) {
        // 创建一个空的ContentValues
        ContentValues values = new ContentValues();
        // 向RawContacts.CONTENT_URI执行一个空值插入，
        // 目的是获取系统返回的rawContactId
        Uri rawContactUri = contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);
        values.clear();

        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        // 设置内容类型
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        // 设置联系人名字
        values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, date + "_" + uid.incrementAndGet());
        // 向联系人URI添加联系人名字
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, values);
        values.clear();


        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        // 设置联系人的电话号码
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, mobile);
        // 设置电话类型
        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        // 向联系人电话号码URI添加电话号码
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, values);
        values.clear();
    }
}
