package com.system.intellignetcable.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.SystemClock;
import android.util.SparseIntArray;
import android.widget.EditText;
import android.widget.Toast;

import com.system.intellignetcable.R;

public class Util {

	public static boolean showWarning(Context context, int resRes) {
		Toast.makeText(context, resRes, Toast.LENGTH_LONG).show();
		return false;
	}

	public static boolean isEtEmpty(EditText editText) {
		String str = editText.getText().toString();
		return str == null || str.equals("");
	}

	public static boolean isLenLegal(EditText editText) {
		if (isEtEmpty(editText))
			return false;
		String str = editText.getText().toString();
		return str != null && str.length() % 2 == 0;
	}

	public static boolean isEtsLegal(EditText[] ets) {
		for (EditText et : ets) {
			if (isLenLegal(et))
				return true;
		}
		return false;
	}

	public static SoundPool sp;
	public static SparseIntArray suondMap;
	public static Context context;

	// ��ʼ��������
	public static void initSoundPool(Context context) {
		Util.context = context;
		sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
		suondMap = new SparseIntArray();
		suondMap.put(1, sp.load(context, R.raw.msg0, 1));
	}

	public static boolean soundfinished;

	// ��������������
	public static void play(int sound, int number) {
		soundfinished = true;
		AudioManager am = (AudioManager) Util.context.getSystemService(Context.AUDIO_SERVICE);

		// ���ص�ǰAudioManager���������ֵ
		float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		sp.play(suondMap.get(sound), // ���ŵ�����Id
				audioCurrentVolume, // ����������
				audioCurrentVolume, // ����������
				1, // ���ȼ���0Ϊ���
				number, // ѭ��������0�޲�ѭ����-1����Զѭ��
				1);// �ط��ٶȣ�ֵ��0.5-2.0֮�䣬1Ϊ�����ٶ�
		SystemClock.sleep(200);
		soundfinished = false;
	}

	// ��ȡ��һ�α����,��Դ��","����
	public static String getPowers(Context context) {
		SharedPreferences shared = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		return shared.getString("power", "rfid power");
	}

	// ������һ�α����
	public static void savePower(Context context, String powers) {
		SharedPreferences shared = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		Editor editor = shared.edit();
		editor.putString("power", powers);
		editor.commit();
	}
	
	/**
	 * �洢�ͻ�ȡ�ز�ʱ��
	 */
	public static int getSavedCarrier(Context context) {
		return context.getSharedPreferences("config", Context.MODE_PRIVATE).getInt("Carrier", 25);
	}
	public static void saveCarrierTime(int carrierTime) {
		SharedPreferences shared = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		Editor editor = shared.edit();
		editor.putInt("Carrier", carrierTime);
		editor.apply();
	}
	
	public static String getSelectedEpc() {
		return context.getSharedPreferences("config", Context.MODE_PRIVATE).getString("epc", "");
	}
	public static void saveSlectedEpc(String epc) {
		SharedPreferences shared = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		Editor editor = shared.edit();
		editor.putString("epc", epc);
		editor.apply();
	}
}
