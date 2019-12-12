package com.hj.nf.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * Created by snell1 on 2017-03-07.
 */
public class MmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

    }
}











//
////mms receiver
//public class MmsReceiver extends BroadcastReceiver
//{
////    private Context _context;
//
//    @Override
//    public void onReceive(Context $context, final Intent $intent)
//    {
////        _context = $context;
////
////        Runnable runn = new Runnable()
////        {
////            @Override
////            public void run()
////            {
////                parseMMS();
////            }
////        };
////        Handler handler = new Handler();
////        handler.postDelayed(runn, 10000); // 시간이 너무 짧으면 못 가져오는게 있더라
//    }
//
////    private void parseMMS()
////    {
////        ContentResolver contentResolver = _context.getContentResolver();
////        final String[] projection = new String[] { "_id" };
////        Uri uri = Uri.parse("content://mms");
////        Cursor cursor = contentResolver.query(uri, projection, null, null, "_id desc limit 1");
////
////        if (cursor.getCount() == 0)
////        {
////            cursor.close();
////            return;
////        }
////
////        cursor.moveToFirst();
////        String id = cursor.getString(cursor.getColumnIndex("_id"));
////        cursor.close();
////
////        String number = parseNumber(id);
////        String msg = parseMessage(id);
////        Log.d("123mms parseMMS", "|" + number + "|" + msg);
////    }
////
////    private String parseNumber(String $id)
////    {
////        String result = null;
////
////        Uri uri = Uri.parse(MessageFormat.format("content://mms/{0}/addr", $id));
////        String[] projection = new String[] { "address" };
////        String selection = "msg_id = ? and type = 137";// type=137은 발신자
////        String[] selectionArgs = new String[] { $id };
////
////        Cursor cursor = _context.getContentResolver().query(uri, projection, selection, selectionArgs, "_id asc limit 1");
////
////        if (cursor.getCount() == 0)
////        {
////            cursor.close();
////            return result;
////        }
////
////        cursor.moveToFirst();
////        result = cursor.getString(cursor.getColumnIndex("address"));
////        cursor.close();
////
////        return result;
////    }
////
////    private String parseMessage(String $id)
////    {
////        String result = null;
////
////        // 조회에 조건을 넣게되면 가장 마지막 한두개의 mms를 가져오지 않는다.
////        Cursor cursor = _context.getContentResolver().query(Uri.parse("content://mms/part"), new String[] { "mid", "_id", "ct", "_data", "text" }, null, null, null);
////
////        Log.d("123mms parseMessage", "mms 메시지 갯수 : " + cursor.getCount());
////        if (cursor.getCount() == 0)
////        {
////            cursor.close();
////            return result;
////        }
////
////        cursor.moveToFirst();
////        while (!cursor.isAfterLast())
////        {
////            String mid = cursor.getString(cursor.getColumnIndex("mid"));
////            if ($id.equals(mid))
////            {
////                String partId = cursor.getString(cursor.getColumnIndex("_id"));
////                String type = cursor.getString(cursor.getColumnIndex("ct"));
////                if ("text/plain".equals(type))
////                {
////                    String data = cursor.getString(cursor.getColumnIndex("_data"));
////
////                    if (TextUtils.isEmpty(data))
////                        result = cursor.getString(cursor.getColumnIndex("text"));
////                    else
////                        result = parseMessageWithPartId(partId);
////                }
////            }
////            cursor.moveToNext();
////        }
////        cursor.close();
////
////        return result;
////    }
////
////
////    private String parseMessageWithPartId(String $id)
////    {
////        Uri partURI = Uri.parse("content://mms/part/" + $id);
////        InputStream is = null;
////        StringBuilder sb = new StringBuilder();
////        try
////        {
////            is = _context.getContentResolver().openInputStream(partURI);
////            if (is != null)
////            {
////                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
////                BufferedReader reader = new BufferedReader(isr);
////                String temp = reader.readLine();
////                while (!TextUtils.isEmpty(temp))
////                {
////                    sb.append(temp);
////                    temp = reader.readLine();
////                }
////            }
////        }
////        catch (IOException e)
////        {
////            e.printStackTrace();
////        }
////        finally
////        {
////            if (is != null)
////            {
////                try
////                {
////                    is.close();
////                }
////                catch (IOException e)
////                {
////                }
////            }
////        }
////        return sb.toString();
////    }
//}