package edu.caltech.ksigma;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.phidgets.PhidgetException;
import com.phidgets.SpatialPhidget;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.SpatialDataEvent;
import com.phidgets.event.SpatialDataListener;

public class PhidgetDroidKsigmaActivity extends Activity {
    /** Called when the activity is first created. */
	
	//initializations
	OurView v;
	
	public float ch1_data, ch1_data1,ch1_data2;
	public float var=0,var1=0,var2=0;
	boolean flag = false;
	
	private static final String TAG = "PhidgetDroidKsigma";
	private static final float EPSILON = 0.0001f;
	private static final double pickThreshold = 1.5;
	
	private boolean picked;
	
	private SpatialPhidget spatial;
	int i,j,istart,id=0,k=1,x,o=0,temp;
	
	
	float targetDelta = 0.02f;
	float tLTA = 10.0f, tSTA = 0.5f, tGap = 1.0f;
	
	int nSamplesLTA = (int)(tLTA/targetDelta);
	int nSamplesSTA = (int)(tSTA/targetDelta);
	int gapLTA = (int)(tGap/targetDelta);
	
	double NE[], Z[], stdev, ksigma;
	
	double sumLTA = 0, sum2LTA = 0, sumSTA = 0;
	
	TextView txt;
		
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        v= new OurView(this);
        setContentView(v);
        
        com.phidgets.usb.Manager.Initialize(this);
        
        istart = nSamplesLTA + gapLTA;
        
        NE = new double [550];
        Z = new double [550];
        picked = false;
        
        try {
			spatial = new SpatialPhidget();			
			spatial.addAttachListener(new AttachListener() {
				
				@Override
				public void attached(AttachEvent ae) {
					// TODO Auto-generated method stub
					try {
						((SpatialPhidget)ae.getSource()).setDataRate(4);
					} catch (PhidgetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			
			spatial.addSpatialDataListener(new SpatialDataListener() {
				
				@Override
				public void data(SpatialDataEvent sde) {
					// TODO Auto-generated method stub
					
					
					for(j=0;j<sde.getData().length;j++,id++)
					{
						Log.d(TAG, id + " " );
						if(sde.getData()[j].getAcceleration().length>0)
						{
							
							for(i=0;i<sde.getData()[j].getAcceleration().length ;i++){
								{
									ch1_data = 700 - (float)((sde.getData()[j].getAcceleration()[0])*10);
									ch1_data1 = 500 - (float)((sde.getData()[j].getAcceleration()[1])*10);
									ch1_data2 = 300 - (float)((sde.getData()[j].getAcceleration()[2])*10);
									if(id < (nSamplesLTA + gapLTA))
									{
										NE[id] = Math.sqrt(Math.pow(sde.getData()[j].getAcceleration()[0], 2)) + Math.pow(sde.getData()[j].getAcceleration()[1], 2);
										Z[id] = (sde.getData()[j].getAcceleration()[2]);
										//Log.d(TAG, NE[id] + "   " + Z[id]);
									}
									else
										if(id > (nSamplesLTA + gapLTA) )
										{
											for(int s=1;s<550;s++)
											{
												NE[s-1] = NE[s];
												Z[s-1] = Z[s];
												NE[550-1] = Math.sqrt(Math.pow(sde.getData()[j].getAcceleration()[0], 2)) + Math.pow(sde.getData()[j].getAcceleration()[1], 2);
												Z[550-1] = (sde.getData()[j].getAcceleration()[2]);
												
												//Log.d(TAG, "Acc : " + NE[s-1] + Z[s-1] + "");
											}
											
											sumSTA = 0;
											sumLTA = 0;
											sum2LTA = 0;
											stdev = 0;
											ksigma = 0;
											
										
											for(int l = 0; l<(nSamplesLTA); l++)
											{
												/*sumLTA+=Z[l];
												sum2LTA+=Math.pow(Z[l], 2);*/
												sumLTA+=Z[l];
												sum2LTA+=Z[l]*Z[l];
												//Log.d(TAG, "sumlta in for : " + sumLTA +"  "+sum2LTA);
											}
											Log.d(TAG, "sum Lta before : " + sumLTA);
											Log.d(TAG, "sum2lta before : " + sum2LTA);
											sumLTA/=nSamplesLTA;
											sum2LTA/=nSamplesLTA;
											Log.d(TAG, "sum2lta : " + sum2LTA);
											
											if(sum2LTA<=sumLTA*sumLTA)
												stdev = EPSILON;
											else
												stdev = Math.sqrt(sum2LTA - sumLTA*sumLTA);	
											
											Log.d(TAG, "sumLTA is : " + sumLTA);
											
											for(int m = 550 - nSamplesSTA; m < 550; m++)
												sumSTA+=Z[m];
											
											sumSTA/=nSamplesSTA;
											
											Log.d(TAG, "sumSTA is :" +sumSTA);
											Log.d(TAG, "stdev is : "+stdev);
											ksigma = (float)(sumSTA - sumLTA)/stdev;
											
											Log.d(TAG, "metric : " +ksigma);
											
											float pickInterval = 1.f;
											
											if(!picked && ksigma > pickThreshold)
											{
												 temp = id;
												picked = true;	
												/*if(o>6)
												{
													
												}*/
												//else{}
												runOnUiThread(new display(o));
												Log.d(TAG, "Pick");
												
											}
											
											else if(picked && ksigma > pickThreshold){}
											if(picked && (id - temp) > 250 ){
												picked = false;
											}
											
										}
										
									}
								}
							}
						;
						//Log.d(TAG, out);
						
					}
				}
			});			
			spatial.openAny();
		} catch (PhidgetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
    protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		v.pause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		v.resume();
	}
    
    public class OurView extends SurfaceView implements Runnable
    {

		Thread t = null;
		SurfaceHolder holder;
		String accelerometer;
		
		private final int width =805;
		private final int heigth = 280*4;
		private Paint grid_paint = new Paint();
		private Paint cross_paint = new Paint();
		private Paint outline_paint = new Paint();
		private Paint ch_1 = new Paint();
		private Paint ch_2 = new Paint();
		private Paint ch_3 = new Paint();
		
		
		public OurView(Context context) {
			super(context);
			holder = getHolder();
		}

		
		public void callDraw(Canvas canvas)
		{
			grid_paint.setColor(Color.rgb(100, 100, 100));
			cross_paint.setColor(Color.rgb(70, 100, 70));
			outline_paint.setColor(Color.GREEN);
			ch_1.setColor(Color.RED);
			ch_2.setColor(Color.BLUE);
			ch_3.setColor(Color.GREEN);
			for(int vertical = 1; vertical<10; vertical++){
		    	canvas.drawLine(
		    			vertical*(width/10)+1, 1,
		    			vertical*(width/10)+1, heigth+1,
		    			grid_paint);
		    }	    	
		    for(int horizontal = 1; horizontal<10; horizontal++){
		    	canvas.drawLine(
		    			1, horizontal*(heigth/10)+1,
		    			width+1, horizontal*(heigth/10)+1,
		    			grid_paint);
		    }	    
		    
		    
		    canvas.drawLine(0, (heigth/2)+1, width+1, (heigth/2)+1, cross_paint);
			canvas.drawLine((width/2)+1, 0, (width/2)+1, heigth+1, cross_paint);
			
			// draw outline
			canvas.drawLine(0, 0, (width+1), 0, outline_paint);	// top
			canvas.drawLine((width+1), 0, (width+1), (heigth+1), outline_paint); //right
			canvas.drawLine(0, (heigth+1), (width+1), (heigth+1), outline_paint); // bottom
			canvas.drawLine(0, 0, 0, (heigth+1), outline_paint); //left
		}
		
		
		public void run() {
			// TODO Auto-generated method stub
			Log.d(TAG, "picked : "+picked);
			if(picked)
				Toast.makeText(getApplicationContext(), "Pick", Toast.LENGTH_LONG).show();
			
			while(flag==true)
			{
				if(!holder.getSurface().isValid())
				{
					continue;
				}
				Canvas canvas = holder.lockCanvas();
				callDraw(canvas);
				
				
				{	
					if(x+5>(240*3))
						{
							canvas.drawColor(Color.BLACK);
							callDraw(canvas);
							if(x>721)
							x=0;	
							var=0;
							var1=0;
							var2=0;
							x++;
						}
					else
					{
					canvas.drawLine(x, var, x+1, ch1_data, ch_1);
					canvas.drawLine(x, var1, x+1, ch1_data1, ch_2);
					canvas.drawLine(x, var2, x+1, ch1_data2, ch_3);
					var = ch1_data;
					var1 = ch1_data1;
					var2 = ch1_data2;
					x+=1;
					}
					
					
			}
				holder.unlockCanvasAndPost(canvas);
		}
	}
		public void pause()
		{
			flag= false;
			while(true)
			{
				try{
				t.join();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				break;
			}
			t=null;
		}
		public void resume()
		{
			flag= true;
			t=new Thread(this);
			t.start();
			
		}
    	
    }
    protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		com.phidgets.usb.Manager.Uninitialize();
	}
    public class display implements Runnable{

    	int counter;
    	
    	public display(int x){
    		this.counter = x;
    	}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.d(TAG, "flag is : " +flag);
			//if(flag)
			{
			Toast.makeText(getApplicationContext(), "Pick", Toast.LENGTH_SHORT).show();
			/*try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			}
			
		}
    	
    }
    

}