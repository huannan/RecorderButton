###一个绚丽的录音按钮，带有水波纹效果，这是从真实项目里面挖下来的。
由于时间匆促，代码还有待重构。

###效果：

![](http://i.imgur.com/pwbTBOA.png)

![](http://i.imgur.com/3de5MiJ.png)

![](http://i.imgur.com/pMsuzE4.gif)


###使用方法：

1. 在布局文件中添加
	
		<com.nan.recordbutton.widget.RecorderButton
		        android:id="@+id/btn_record"
		        android:layout_width="wrap_content"
		        android:layout_height="wrap_content"
		        android:layout_centerInParent="true"
		        android:layout_marginTop="10dp"
		        android:background="@drawable/bg_record"
		        app:bg_normal="@drawable/bg_record"
		        app:bg_recording="@drawable/bg_recording"
		        app:bg_want_cancel="@drawable/bg_record"
		        app:max_record_time="15"
		        app:max_voice_level="150"
		        app:min_record_time="10"
		        app:txt_normal=""
		        app:txt_recording=""
		        app:txt_want_cancel="" />

2. 在Activity中找到RecorderButton以后记得设置监听器RecorderButton.AudioStateRecorderListener，从而实现自己的业务逻辑。具体可以参考项目中的代码。

		private RecorderButton btn_record;
		
		btn_record = (RecorderButton) findViewById(R.id.btn_record);
		btn_record.setAudioStateRecorderListener(new MyRecordListener());

	    class MyRecordListener implements RecorderButton.AudioStateRecorderListener {
	
	        @Override
	        public void onStart(float time) {
	        }
	
	        @Override
	        public void onUpdateTime(float currentTime, float minTime, float maxTime){
	        }
	
	        @Override
	        public void onReturnToRecord() {   
	        }
	
	        @Override
	        public void onWantToCancel() {	            
	        }
	
	        @Override
	        public void onFinish(float seconds, String filePath) {
	        }
	
	        @Override
	        public void onCancel(boolean isTooShort) {	
	        }
	
	        @Override
	        public void onVoiceChange(int voiceLevel) {	
	        }
	    }
